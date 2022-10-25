package com.flagsmith.api

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.flagsmith.android.network.AnalyticsTrackFlagAPI
import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IEventUpdate
import org.json.JSONException
import org.json.JSONObject

class FlagsmithAnalytics constructor(
    private val builder: Flagsmith,
    private val context: Context,
    private val flushPeriod: Int,

) {
    private val applicationContext: Context = context.applicationContext
    private val currentEvents = getMap()
    private val timerHandler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            println("Handler called on main thread")
            if (currentEvents.isNotEmpty()) {
                AnalyticsTrackFlagAPI(builder, currentEvents, object : IEventUpdate {
                    override fun success() {
                        resetMap()
                    }
                    override fun failed(exception: Exception) {
                        println("Failed posting analytics - ${exception.localizedMessage}")
                    }
                })
            }
            timerHandler.postDelayed(this, flushPeriod.toLong() * 1000)
        }
    }

    init {
        timerHandler.post(timerRunnable)
    }

    companion object {
        private const val EVENTS_KEY = "events"
    }

    /// Counts the instances of a `Flag` being queried.
    fun trackEvent(flagName: String) {
        val currentFlagCount = currentEvents[flagName] ?: 0
        currentEvents[flagName] = currentFlagCount + 1;

        //update events cache
        setMap(currentEvents)
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
            e.printStackTrace()
        }
        return outputMap
    }

    private fun resetMap() {
        currentEvents.clear()
        setMap(currentEvents)
    }

}
