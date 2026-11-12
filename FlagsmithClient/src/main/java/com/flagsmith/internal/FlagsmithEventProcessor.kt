package com.flagsmith.internal

import android.util.Log
import com.flagsmith.entities.Event
import com.flagsmith.entities.EventBatch
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.random.Random

interface FlagsmithEventsRetrofitService {
    @POST("v1/events")
    fun postEvents(@Body batch: EventBatch): Call<Unit>
}

/**
 * Buffers tracking and exposure events, and sends them to Flagsmith's events API in batches.
 *
 * Batches are sent every [flushPeriodSeconds], when [maxBuffer] events are buffered, and on [flush].
 * Repeated exposures within a batch are sent once. When the buffer is full, the oldest event is dropped.
 *
 * One batch is in flight at a time. A batch is sent up to [MAX_ATTEMPTS] times, each request timing out
 * after [requestTimeoutMillis], backing off from [retryBaseDelayMillis], doubling, with full jitter between
 * attempts. Connection failures, timeouts, 408, 429 and 5xx responses are retried. A batch whose last attempt
 * fails is kept for the next flush, unless that attempt got a 500. A batch the events API rejects with another
 * 4xx is dropped, and a 401 or 403, refusing the environment key, stops event tracking.
 */
class FlagsmithEventProcessor(
    environmentKey: String,
    eventsBaseUrl: String,
    flushPeriodSeconds: Int,
    private val maxBuffer: Int = DEFAULT_MAX_BUFFER,
    private val retryBaseDelayMillis: Long = DEFAULT_RETRY_BASE_DELAY_MILLIS,
    requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    private val random: Random = Random.Default
) {
    private val service: FlagsmithEventsRetrofitService = Retrofit.Builder()
        .baseUrl(if (eventsBaseUrl.endsWith("/")) eventsBaseUrl else "$eventsBaseUrl/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
        .client(
            OkHttpClient.Builder()
                .addInterceptor(FlagsmithRetrofitService.envKeyInterceptor(environmentKey))
                .addInterceptor(FlagsmithRetrofitService.userAgentInterceptor())
                .callTimeout(requestTimeoutMillis, TimeUnit.MILLISECONDS)
                // Failed requests are retried with backoff below, not straight away
                .retryOnConnectionFailure(false)
                .build()
        )
        .build()
        .create(FlagsmithEventsRetrofitService::class.java)

    private enum class Outcome { DELIVERED, DROPPED, KEPT, REFUSED }

    // Guarded by `buffer`.
    private val buffer = ArrayDeque<Event>()
    private val exposureKeys = mutableSetOf<List<Any?>>()
    private var trackedCount = 0L
    private var attemptedCount = 0L
    private var fullBufferFlushPending = false
    private var refused = false
    private var closed = false

    @Volatile
    private var callInFlight: Call<Unit>? = null

    // Sends every batch, so that one is in flight at a time.
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "flagsmith-events").apply { isDaemon = true }
    }

    init {
        if (flushPeriodSeconds > 0) {
            executor.scheduleWithFixedDelay(
                { send() }, flushPeriodSeconds.toLong(), flushPeriodSeconds.toLong(), TimeUnit.SECONDS
            )
        }
    }

    companion object {
        const val FLAG_EXPOSURE_EVENT = "\$flag_exposure"
        const val DEFAULT_MAX_BUFFER = 1000
        const val DEFAULT_RETRY_BASE_DELAY_MILLIS = 1000L
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 3000L
        const val MAX_ATTEMPTS = 3
        private val RETRIED_CLIENT_ERRORS = setOf(408, 429)
    }

    fun trackEvent(
        event: String,
        identifier: String?,
        value: Any?,
        traits: Map<String, Any?>?,
        metadata: Map<String, Any?>?
    ) = buffer(event, null, identifier, value, traits, metadata)

    fun trackExposureEvent(
        featureName: String,
        identifier: String,
        value: Any?,
        traits: Map<String, Any?>?,
        metadata: Map<String, Any?>?
    ) = buffer(FLAG_EXPOSURE_EVENT, featureName, identifier, value, traits, metadata)

    /**
     * Sends the events tracked so far, then calls [completion] once each of them has been delivered, dropped,
     * or kept for the next flush, retries included. Events another send attempted meanwhile are not sent again.
     */
    fun flush(completion: (() -> Unit)? = null) {
        val tracked = synchronized(buffer) { trackedCount.takeUnless { closed } }
        if (tracked == null || !submit { send(tracked); completion?.invoke() }) completion?.invoke()
    }

    /**
     * Stops sending events on a timer, and sends the buffered events, waiting for at most [timeoutMillis].
     * Events not sent by then are lost. Events tracked after closing are ignored.
     */
    fun close(timeoutMillis: Long) {
        synchronized(buffer) {
            if (closed) return
            closed = true
        }
        try {
            executor.submit { send() }.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            Log.w("Flagsmith", "Gave up sending events after ${timeoutMillis}ms while closing")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: ExecutionException) {
            Log.e("Flagsmith", "Failed sending events while closing", e)
        } catch (e: RejectedExecutionException) {
            // Already shut down
        }
        executor.shutdownNow()
        callInFlight?.cancel()
    }

    private fun buffer(
        event: String,
        featureName: String?,
        identifier: String?,
        value: Any?,
        traits: Map<String, Any?>?,
        metadata: Map<String, Any?>?
    ) {
        val stringValue = value?.toString()
        val tracked = synchronized(buffer) {
            if (closed) return
            if (refused) {
                Log.d("Flagsmith", "Not tracking $event: the events API refused the environment key")
                return
            }
            if (event == FLAG_EXPOSURE_EVENT &&
                !exposureKeys.add(listOf(featureName, identifier, stringValue, metadata?.get("experiment_id")))
            ) return
            if (buffer.size >= maxBuffer) {
                val dropped = buffer.removeFirst()
                Log.w("Flagsmith", "Event buffer is full, dropping the oldest ${dropped.event} event")
            }
            buffer.addLast(
                Event(
                    event = event,
                    featureName = featureName,
                    identifier = identifier,
                    value = stringValue,
                    traits = traits,
                    metadata = (metadata ?: emptyMap()) + ("sdk_version" to FlagsmithRetrofitService.SDK_VERSION),
                    timestamp = System.currentTimeMillis()
                )
            )
            trackedCount++
            if (buffer.size < maxBuffer || fullBufferFlushPending) return
            fullBufferFlushPending = true
            trackedCount
        }
        submit {
            synchronized(buffer) { fullBufferFlushPending = false }
            send(tracked)
        }
    }

    /** Runs [task] on the sending thread, returning false if the processor is closed. */
    private fun submit(task: () -> Unit): Boolean = try {
        executor.execute(task)
        true
    } catch (e: RejectedExecutionException) {
        false
    }

    /**
     * Sends the buffered events as one batch, on the sending thread. Given [tracked], does nothing if every
     * event tracked up to it has been attempted already.
     */
    private fun send(tracked: Long? = null) {
        val (events, attempted) = synchronized(buffer) {
            if (buffer.isEmpty() || (tracked != null && attemptedCount >= tracked)) return
            val events = buffer.toList()
            buffer.clear()
            exposureKeys.clear()
            events to trackedCount
        }
        val outcome = try {
            deliver(events)
        } catch (e: RuntimeException) {
            Log.e("Flagsmith", "Failed sending ${events.size} events, dropping them", e)
            Outcome.DROPPED
        }
        synchronized(buffer) {
            attemptedCount = attempted
            when (outcome) {
                Outcome.KEPT -> {
                    val kept = (events + buffer).takeLast(maxBuffer)
                    buffer.clear()
                    buffer.addAll(kept)
                }
                Outcome.REFUSED -> {
                    refused = true
                    buffer.clear()
                    exposureKeys.clear()
                }
                Outcome.DELIVERED, Outcome.DROPPED -> {}
            }
        }
    }

    private fun deliver(events: List<Event>): Outcome {
        val batch = EventBatch(events)
        var status: Int? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            if (attempt > 1) {
                if (executor.isShutdown) {
                    Log.w("Flagsmith", "Not retrying ${events.size} events: the client is closed")
                    return Outcome.KEPT
                }
                val backoffMillis = retryBaseDelayMillis shl (attempt - 2)
                try {
                    Thread.sleep(random.nextLong(backoffMillis + 1))
                } catch (e: InterruptedException) {
                    Log.w("Flagsmith", "Not retrying ${events.size} events: the client is closed")
                    return Outcome.KEPT
                }
            }
            val call = service.postEvents(batch).also { callInFlight = it }
            status = try {
                call.execute().code()
            } catch (e: IOException) {
                Log.w(
                    "Flagsmith",
                    "Failed sending ${events.size} events, attempt $attempt of $MAX_ATTEMPTS - ${e.localizedMessage}"
                )
                null
            } finally {
                callInFlight = null
            }
            if (status == null) continue
            when {
                status in 200..299 -> {
                    Log.i("Flagsmith", "Sent ${events.size} events")
                    return Outcome.DELIVERED
                }
                status == 401 || status == 403 -> {
                    Log.e("Flagsmith", "Events API refused the environment key with $status, stopping event tracking")
                    return Outcome.REFUSED
                }
                status < 500 && status !in RETRIED_CLIENT_ERRORS -> {
                    Log.e("Flagsmith", "Events API rejected ${events.size} events with $status, dropping them")
                    return Outcome.DROPPED
                }
                else -> Log.w(
                    "Flagsmith",
                    "Failed sending ${events.size} events, attempt $attempt of $MAX_ATTEMPTS - status $status"
                )
            }
        }
        if (status == 500) {
            Log.e("Flagsmith", "Dropping ${events.size} events the events API failed with 500")
            return Outcome.DROPPED
        }
        Log.w("Flagsmith", "Keeping ${events.size} events for the next flush")
        return Outcome.KEPT
    }
}
