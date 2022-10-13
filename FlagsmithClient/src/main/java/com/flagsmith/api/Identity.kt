package com.flagsmith.api



import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IIdentity
import com.flagsmith.response.ResponseTraitUpdate
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.flagsmith.interfaces.ITraitUpdate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Identity(builder: Flagsmith, key: String, value: String, finish: ITraitUpdate) {

    var finish: IIdentity
    var key: String
    var value: String
    var builder: Flagsmith

    init {

        this.finish = finish
        this.key = key
        this.value = value
        this.builder = builder

        if (validateData()) {
            startAPI()
        }


    }

    private fun validateData(): Boolean {
        val result = true
        //check identifier null
        if (builder.identity.isNullOrEmpty()) {
            finish.failed("User Identifier must to set in class 'FlagsmithBuilder' first")
            return false
        }

        return result
    }

    private fun startAPI() {
        val url = ApiManager.BaseUrl.Url + "traits/"


        val header = NetworkFlag.getNetworkHeader(builder)

        ApiManager(
            url,
            header,
            getJsonPostBody(),
            object : INetworkListener {
                override fun success(response: String?) {
                    _parse(response!!)
                }

                override fun failed(error: String?) {
                    finish.failed(error!!)
                }

            })
    }

    private fun getJsonPostBody(): String {
        return "{\n" +
                "    \"identity\": {\n" +
                "        \"identifier\": \"" + builder.identity + "\"\n" +
                "    },\n" +
                "    \"trait_key\": \"" + key + "\",\n" +
                "    \"trait_value\": \"" + value + "\"\n" +
                "}"
    }


    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ResponseTraitUpdate>() {}.type
            val responseFromJson: ResponseTraitUpdate = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }


}