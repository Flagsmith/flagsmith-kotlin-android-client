package com.flagsmith.android.network


import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IEventUpdate
import com.flagsmith.interfaces.INetworkListener

import org.json.JSONObject

class AnalyticsTrackFlagAPi(builder: Flagsmith, eventMap: Map<String, Int?>, finish: IEventUpdate) {

    var finish: IEventUpdate
    var eventMap: Map<String, Int?>
    var builder: Flagsmith

    init {

        this.finish = finish
        this.eventMap = eventMap
        this.builder = builder

        if (validateData()) {
            startAPI()
        }
    }

    private fun validateData(): Boolean {
        return true
    }

    private fun startAPI() {
        val url = ApiManager.BaseUrl.Url + "analytics/flags/"

        val header = NetworkFlag.getNetworkHeader(builder)

        ApiManager(
            url,
            header,
            getJsonPostBody(this),
            object : INetworkListener {
                override fun success(response: String?) {
                    _parse()
                }

                override fun failed(error: String?) {
                    finish.failed(error ?: "No-Response-Analytics")
                }
            })
    }


    fun _parse() {
        try {
            finish.success()
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }

    companion object {
        private fun getJsonPostBody(analyticsTrackFlagAPi: AnalyticsTrackFlagAPi): String {
            return JSONObject(analyticsTrackFlagAPi.eventMap).toString()
        }
    }


}