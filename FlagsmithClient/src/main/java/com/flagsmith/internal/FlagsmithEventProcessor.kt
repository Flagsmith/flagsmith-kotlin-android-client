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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

interface FlagsmithEventsRetrofitService {
    @POST("v1/events")
    fun postEvents(@Body batch: EventBatch): Call<Unit>
}

/**
 * Buffers tracking and exposure events, and sends them to Flagsmith's events API in batches.
 *
 * Batches are sent every [flushPeriodSeconds], when [maxBuffer] events are buffered, and on [flush].
 * Repeated exposures within a batch are sent once. A batch that fails to send is retried once, then dropped.
 */
class FlagsmithEventProcessor(
    environmentKey: String,
    eventsBaseUrl: String,
    flushPeriodSeconds: Int,
    private val maxBuffer: Int = DEFAULT_MAX_BUFFER,
    private val retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS
) {
    private val service: FlagsmithEventsRetrofitService = Retrofit.Builder()
        .baseUrl(if (eventsBaseUrl.endsWith("/")) eventsBaseUrl else "$eventsBaseUrl/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
        .client(
            OkHttpClient.Builder()
                .addInterceptor(FlagsmithRetrofitService.envKeyInterceptor(environmentKey))
                .addInterceptor(FlagsmithRetrofitService.userAgentInterceptor())
                .build()
        )
        .build()
        .create(FlagsmithEventsRetrofitService::class.java)

    private val buffer = mutableListOf<Event>()
    private val exposureKeys = mutableSetOf<List<Any?>>()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "flagsmith-events").apply { isDaemon = true }
    }

    init {
        if (flushPeriodSeconds > 0) {
            executor.scheduleWithFixedDelay(
                ::flush, flushPeriodSeconds.toLong(), flushPeriodSeconds.toLong(), TimeUnit.SECONDS
            )
        }
    }

    companion object {
        const val FLAG_EXPOSURE_EVENT = "\$flag_exposure"
        const val DEFAULT_MAX_BUFFER = 1000
        const val DEFAULT_RETRY_DELAY_MILLIS = 1000L
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

    /** Sends the buffered events, without waiting for the request to complete. */
    fun flush() {
        val events = synchronized(buffer) {
            if (buffer.isEmpty()) return
            buffer.toList().also {
                buffer.clear()
                exposureKeys.clear()
            }
        }
        send(events, retry = true)
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
        val isFull = synchronized(buffer) {
            if (event == FLAG_EXPOSURE_EVENT &&
                !exposureKeys.add(listOf(featureName, identifier, stringValue, metadata?.get("experiment_id")))
            ) return
            buffer.add(
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
            buffer.size >= maxBuffer
        }
        if (isFull) flush()
    }

    private fun send(events: List<Event>, retry: Boolean) {
        service.postEvents(EventBatch(events)).enqueueWithResult { result ->
            result.onSuccess {
                Log.i("Flagsmith", "Sent ${events.size} events")
            }.onFailure { err ->
                if (retry) {
                    Log.w("Flagsmith", "Failed sending ${events.size} events, retrying - ${err.localizedMessage}")
                    executor.schedule({ send(events, retry = false) }, retryDelayMillis, TimeUnit.MILLISECONDS)
                } else {
                    Log.e("Flagsmith", "Failed sending ${events.size} events, dropping them - ${err.localizedMessage}")
                }
            }
        }
    }
}
