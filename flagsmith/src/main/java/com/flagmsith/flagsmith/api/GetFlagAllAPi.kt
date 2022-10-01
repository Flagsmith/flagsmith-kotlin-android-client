package com.flag.android.flagsmith.api


import com.flag.android.flagsmith.FlagConstants
import com.flag.android.flagsmith.builder.FlagsmithBuilder
import com.flag.android.flagsmith.interfaces.IFlagArrayResult
import com.flag.android.flagsmith.response.ResponseFlagElement
import com.flag.android.network.INetworkListener
import com.flag.android.network.NetworkFlagUtils
import com.flag.android.network.OkhttpNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GetFlagAllAPi( builder: FlagsmithBuilder, finish: IFlagArrayResult) {


    var finish : IFlagArrayResult;
    var builder: FlagsmithBuilder;

    init {

        this.finish = finish;
        this.builder = builder;

        startAPI();
    }

    private fun startAPI() {
        val url = FlagConstants.baseUrl + "flags/";

        OkhttpNetwork( url, NetworkFlagUtils.getNetworkHeader( builder ), object : INetworkListener {
            override fun success(response: String?) {
                _parse(response!!, finish)
            }

            override fun failed(error: String?) {
                finish.failed( error!! )
            }

        });
    }


    fun _parse( json : String , finish : IFlagArrayResult  ) {
        try {
            val gson = Gson()
            val type  = object : TypeToken<ArrayList<ResponseFlagElement>>() {}.type
            val responseFromJson: ArrayList<ResponseFlagElement> = gson.fromJson(json, type)
            println( "parse() - responseFromJson: " + responseFromJson)

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed( "exception: " + e.toString())
        }
    }


}