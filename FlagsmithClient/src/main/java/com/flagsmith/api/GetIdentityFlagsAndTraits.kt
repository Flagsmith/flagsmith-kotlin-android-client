package com.flagsmith.api


import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.flagsmith.interfaces.IIdentityFlagsAndTraitsResult
import com.flagsmith.response.IdentityFlagsAndTraits
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder

class GetIdentityFlagsAndTraits(builder: Flagsmith, identity: String, finish: IIdentityFlagsAndTraitsResult) {
    var finish: IIdentityFlagsAndTraitsResult
    var builder: Flagsmith
    var identity: String

    init {
        this.finish = finish
        this.builder = builder
        this.identity = identity

        if (validateData()) {
            startAPI()
        }
    }

    private fun validateData(): Boolean {
        val result = true

        return result
    }

    private fun startAPI() {
        val url = builder.baseUrl + "identities/?identifier=" + URLEncoder.encode(identity, "utf-8")
        ApiManager(url, NetworkFlag.getNetworkHeader(builder), object :
            INetworkListener {
            override fun success(response: String?) {
                _parse(response!!)
            }

            override fun failed(exception: Exception) {
                finish.failed(exception)
            }

        })
    }

    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<IdentityFlagsAndTraits>() {}.type
            val responseFromJson: IdentityFlagsAndTraits = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed(e)
        }
    }


}