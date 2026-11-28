package com.flagsmith.internal

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class FlagsmithAnalytics constructor(
    private val context: Context,
    private val retrofitService: FlagsmithRetrofitService,
    private val flushPeriod: Int
) {
    private val applicationContext: Context = context.applicationContext
    private val currentEvents = getMap()
    private val timerHandler = Handler(Looper.getMainLooper())
    private var sending = false
    private var flushPending = false
    private var closed = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            flush()
            synchronized(currentEvents) {
                if (!closed) {
                    timerHandler.postDelayed(this, flushPeriod.toLong() * 1000)
                }
            }
        }
    }

    init {
        timerHandler.post(timerRunnable)
    }

    companion object {
        private const val EVENTS_KEY = "events"
    }

    /**
     * Starts an asynchronous send without resetting the periodic timer. While a send is in flight, the
     * counts left after it completes are sent from its completion instead.
     */
    fun flush() {
        val batch = synchronized(currentEvents) {
            if (closed) return
            takeBatch()
        } ?: return
        send(batch)
    }

    // Call holding the lock on currentEvents.
    private fun takeBatch(): Map<String, Int?>? {
        if (sending) {
            flushPending = true
            return null
        }
        if (currentEvents.isEmpty()) return null
        sending = true
        return currentEvents.toMap()
    }

    private fun send(batch: Map<String, Int?>) {
        retrofitService.postAnalytics(batch).enqueueWithResult { result ->
            val next = synchronized(currentEvents) {
                result.onSuccess {
                    // Preserve counts added while this request was in flight.
                    batch.forEach { (flag, count) ->
                        val total = currentEvents[flag] ?: 0
                        val remaining = total - (count ?: 0)
                        if (remaining > 0) currentEvents[flag] = remaining
                        else currentEvents.remove(flag)
                    }
                    setMap(currentEvents)
                }.onFailure { err ->
                    Log.e("FLAGSMITH", "Failed posting analytics", err)
                }
                sending = false
                if (flushPending) {
                    flushPending = false
                    takeBatch()
                } else null
            }
            next?.let(::send)
        }
    }

    fun close() {
        synchronized(currentEvents) {
            closed = true
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    /// Counts the instances of a `Flag` being queried.
    fun trackEvent(flagName: String) {
        synchronized(currentEvents) {
            if (closed) return
            val currentFlagCount = currentEvents[flagName] ?: 0
            currentEvents[flagName] = currentFlagCount + 1

            // Update events cache
            setMap(currentEvents)
        }
    }

    private fun setMap(updateMap: Map<String, Int?>) {
        val pSharedPref: SharedPreferences =
            context.getSharedPreferences(EVENTS_KEY, Context.MODE_PRIVATE)

        val jsonObject = JSONObject(updateMap)
        val jsonString: String = jsonObject.toString()
        pSharedPref.edit()
            .remove(EVENTS_KEY)
            .putString(EVENTS_KEY, jsonString)
            .apply()
    }

    private fun getMap(): MutableMap<String, Int?> {
        val outputMap: MutableMap<String, Int?> = HashMap()
        val pSharedPref: SharedPreferences =
            applicationContext.getSharedPreferences(EVENTS_KEY, Context.MODE_PRIVATE)
        try {
            val jsonString = pSharedPref.getString(EVENTS_KEY, JSONObject().toString())
            if (jsonString != null) {
                val jsonObject = JSONObject(jsonString)
                val keysItr = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = jsonObject.getInt(key)
                    outputMap[key] = value
                }
            }
        } catch (e: JSONException) {
            Log.e("FLAGSMITH", "Exception in getMap Analytics - ${e.stackTraceToString()}")
        }
        return outputMap
    }

}
