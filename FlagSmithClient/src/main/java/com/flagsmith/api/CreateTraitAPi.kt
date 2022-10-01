package com.flagsmith.api


import com.flagsmith.FlagConstants
import com.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.interfaces.ITraitUpdate
import com.flagsmith.response.ResponseTraitUpdate
import com.flagsmith.android.network.INetworkListener
import com.flagsmith.android.network.NetworkFlagUtils
import com.flagsmith.android.network.OkhttpNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CreateTraitAPi(builder: FlagsmithBuilder, key : String, value : String, finish: ITraitUpdate) {

    var finish : ITraitUpdate
    var key : String
    var value : String
    var builder: FlagsmithBuilder

    init {

        this.finish = finish
        this.key = key
        this.value = value
        this.builder = builder

        if( validateData() ) {
            startAPI()
        }


    }

    private fun validateData() : Boolean {
        val result = true

        //check identifier null
        if( builder.identifierUser.isNullOrEmpty() ) {
            finish.failed( "User Identifier must to set in class 'FlagsmithBuilder' first")
            return false
        }

        return  result
    }

    private fun startAPI() {
        //{{baseUrl}}traits/
        val url = FlagConstants.baseUrl + "traits/"

        val header = NetworkFlagUtils.getNetworkHeader( builder )

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

        })
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
            val type  = object : TypeToken<ResponseTraitUpdate>() {}.type
            val responseFromJson: ResponseTraitUpdate = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }


}