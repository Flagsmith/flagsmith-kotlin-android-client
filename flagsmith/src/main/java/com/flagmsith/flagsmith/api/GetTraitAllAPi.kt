package com.flag.android.flagsmith.api

//import android.content.Context
//import android.util.Log
import com.flag.android.flagsmith.FlagConstants
import com.flag.android.flagsmith.builder.FlagsmithBuilder
import com.flag.android.flagsmith.interfaces.ITraitArrayResult
import com.flag.android.flagsmith.response.ResponseTraits
import com.flag.android.network.INetworkListener
import com.flag.android.network.NetworkFlagUtils
import com.flag.android.network.OkhttpNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GetTraitAllAPi(  builder: FlagsmithBuilder, finish: ITraitArrayResult) {

//    var context : Context;
    var finish : ITraitArrayResult;
    var builder: FlagsmithBuilder;

    init {
//        this.context = context;
        this.finish = finish;
        this.builder = builder;

        if( validateData() ) {
            startAPI();
        }


    }

    private fun validateData() : Boolean {
        var result = true;

        //check identifier null
        if( builder.identifierUser.isNullOrEmpty() ) {
            finish.failed( "User Identifier must to set in class 'FlagsmithBuilder' first")
            return false;
        }

        return  result;
    }

    private fun startAPI() {
        //{{baseUrl}}identities/?identifier={{Identity}}
        val url = FlagConstants.baseUrl + "identities/?identifier=" +  builder.identifierUser;

        OkhttpNetwork( url, NetworkFlagUtils.getNetworkHeader( builder ), object : INetworkListener {
            override fun success(response: String?) {
                _parse(response!!)
            }

            override fun failed(error: String?) {
                finish.failed( error!! )
            }

        });
    }


    fun _parse( json : String   ) {
        try {
            val gson = Gson()
            val type  = object : TypeToken< ResponseTraits >() {}.type
            val responseFromJson: ResponseTraits = gson.fromJson(json, type)
            println(  "parse() - responseFromJson: " + responseFromJson)

            //finish
            finish.success(responseFromJson.traits)
        } catch (e: Exception) {
            finish.failed( "exception: " + e.toString())
        }
    }


}