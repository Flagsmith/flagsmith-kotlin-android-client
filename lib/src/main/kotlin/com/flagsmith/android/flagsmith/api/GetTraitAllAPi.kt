package com.flagsmith.android.flagsmith.api


import com.flagsmith.android.flagsmith.FlagConstants
import com.flagsmith.android.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.android.flagsmith.interfaces.ITraitArrayResult
import com.flagsmith.android.flagsmith.response.ResponseTraits
import com.flagsmith.android.network.INetworkListener
import com.flagsmith.android.network.NetworkFlagUtils
import com.flagsmith.android.network.OkhttpNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GetTraitAllAPi(builder: FlagsmithBuilder, finish: ITraitArrayResult) {

    var finish : ITraitArrayResult;
    var builder: FlagsmithBuilder;

    init {
        this.finish = finish;
        this.builder = builder;

        if( validateData() ) {
            startAPI();
        }


    }

    private fun validateData() : Boolean {
        val result = true;

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

        OkhttpNetwork( url, NetworkFlagUtils.getNetworkHeader( builder ), object :
            INetworkListener {
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
            val type  = object : TypeToken<ResponseTraits>() {}.type
            val responseFromJson: ResponseTraits = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson.traits)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }


}