package com.flagmsith.flagsmith.api

//import android.content.Context
//import android.util.Log
import com.flag.android.flagsmith.FlagConstants
import com.flag.android.flagsmith.builder.FlagsmithBuilder
import com.flag.android.flagsmith.interfaces.ITraitUpdate
import com.flag.android.flagsmith.response.ResponseTraitUpdate
import com.flag.android.network.INetworkListener
import com.flag.android.network.NetworkFlagUtils
import com.flag.android.network.OkhttpNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CreateTraitAPi(  builder: FlagsmithBuilder, key : String, value : String,  finish: ITraitUpdate) {

//    var context : Context;
    var finish : ITraitUpdate;
    var key : String;
    var value : String;
    var builder: FlagsmithBuilder;

    init {

        this.finish = finish;
        this.key = key;
        this.value = value;
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
        //{{baseUrl}}traits/
        val url = FlagConstants.baseUrl + "traits/";

        val header = NetworkFlagUtils.getNetworkHeader( builder );

        OkhttpNetwork(
             url,
            header,
            getJsonPostBody(),
            object : INetworkListener {
            override fun success(response: String?) {
                _parse(response!!)
            }

            override fun failed(error: String?) {
                finish.failed( error!! )
            }

        });
    }

    private fun getJsonPostBody(): String {


        return "{\n" +
        "    \"identity\": {\n" +
        "        \"identifier\": \""+builder.identifierUser +"\"\n" +
        "    },\n" +
        "    \"trait_key\": \"" + key + "\",\n" +
        "    \"trait_value\": \"" + value + "\"\n" +
        "}"
    }


    fun _parse( json : String   ) {
        try {
            val gson = Gson()
            val type  = object : TypeToken< ResponseTraitUpdate >() {}.type
            val responseFromJson: ResponseTraitUpdate = gson.fromJson(json, type)
            println( "parse() - responseFromJson: " + responseFromJson)

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed( "exception: " + e.toString())
        }
    }


}