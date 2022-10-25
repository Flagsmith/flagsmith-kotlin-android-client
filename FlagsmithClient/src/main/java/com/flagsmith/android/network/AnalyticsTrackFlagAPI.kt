package com.flagsmith.android.network

import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IEventUpdate
import com.flagsmith.interfaces.INetworkListener

import org.json.JSONObject

class AnalyticsTrackFlagAPI(builder: Flagsmith, eventMap: Map<String, Int?>, finish: IEventUpdate) {
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
        val url = builder.baseUrl + "analytics/flags/"
        val header = NetworkFlag.getNetworkHeader(builder)

        ApiManager(
            url,
            header,
            getJsonPostBody(this),
            object : INetworkListener {
                override fun success(response: String?) {
                    _parse()
                }

                override fun failed(exception: Exception) {
                    finish.failed(exception)
                }
            })
    }

    fun _parse() {
        try {
            finish.success()
        } catch (e: Exception) {
            finish.failed(e)
        }
    }

    companion object {
        private fun getJsonPostBody(analyticsTrackFlagAPI: AnalyticsTrackFlagAPI): String {
            return JSONObject(analyticsTrackFlagAPI.eventMap).toString()
        }
    }


}