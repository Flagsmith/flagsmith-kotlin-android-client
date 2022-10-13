package com.flagsmith.api


import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.response.ResponseFlagElement
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Flag(builder: Flagsmith, finish: IFlagArrayResult) {


    var finish: IFlagArrayResult
    var builder: Flagsmith

    init {

        this.finish = finish
        this.builder = builder

        startAPI()
    }

    private fun startAPI() {
        val url = ApiManager.BaseUrl.Url + "flags/"

        ApiManager(url, NetworkFlag.getNetworkHeader(builder), object :
            INetworkListener {
            override fun success(response: String?) {
                _parse(response!!, finish)
            }

            override fun failed(error: String?) {
                finish.failed(error!!)
            }

        })
    }


    fun _parse(json: String, finish: IFlagArrayResult) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<ResponseFlagElement>>() {}.type
            val responseFromJson: ArrayList<ResponseFlagElement> = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }


}