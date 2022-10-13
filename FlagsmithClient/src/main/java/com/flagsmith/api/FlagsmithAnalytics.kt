package com.flagsmith.api

import android.content.Context
import android.content.SharedPreferences
import com.flagsmith.android.network.AnalyticsTrackFlagAPi
import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IEventUpdate
import org.json.JSONException
import org.json.JSONObject

class FlagsmithAnalytics {

    /**
    //EVENTS_Preferences
     */
    private val EVENTS_KEY = "events"

    object AnalyticsConfig {
        /// Indicates if analytics are enabled.
        var enableAnalytics: Boolean = true
    }

    object AnalyticsTrack {

        /// Counts the instances of a `Flag` being queried.
        fun trackEvent( context: Context, builder: Flagsmith, flagName:String) {

            //check disabled
            if(!AnalyticsConfig.enableAnalytics) return

            //get map
            val events = AnalyticsCache.getMap(context )

            //get current
            var current : Int? = events[flagName]
            if( current == null ) {
                current = 0;
            }

            //update events cache
            events[flagName] = current + 1
            AnalyticsCache.setMap( context,   events )

            //call api
            AnalyticsTrackFlagAPi(builder, events, object : IEventUpdate{
                override fun success() {
                }
                override fun failed(str: String) {
                }
            })
        }
    }



    object AnalyticsCache {

        val EVENTS_KEY = "events"
        fun setMap(context: Context, updateMap: Map<String, Int?>) {
            val pSharedPref: SharedPreferences =
                context.getSharedPreferences(EVENTS_KEY, Context.MODE_PRIVATE)

            val jsonObject = JSONObject(updateMap)
            val jsonString: String = jsonObject.toString()
            pSharedPref.edit()
                .remove(EVENTS_KEY)
                .putString(EVENTS_KEY, jsonString)
                .apply()
        }


        fun getMap(context: Context): MutableMap<String, Int?> {
            val outputMap: MutableMap<String, Int?> = HashMap()
            val pSharedPref: SharedPreferences =
                context.getSharedPreferences(EVENTS_KEY, Context.MODE_PRIVATE)
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

    }
}