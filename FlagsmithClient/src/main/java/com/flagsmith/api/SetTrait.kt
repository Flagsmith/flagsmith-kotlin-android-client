package com.flagsmith.api

import com.flagsmith.android.network.ApiManager
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.interfaces.ITraitUpdateResult
import com.flagsmith.response.Identity
import com.flagsmith.response.Trait
import com.flagsmith.response.TraitWithIdentity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SetTrait(builder: Flagsmith, trait: Trait, identity: String, finish: ITraitUpdateResult) {
    var finish: ITraitUpdateResult
    var trait: Trait
    var identity: String
    var builder: Flagsmith

    init {
        this.finish = finish
        this.trait = trait
        this.identity = identity
        this.builder = builder

        if (validateData()) {
            startAPI()
        }
    }

    private fun validateData(): Boolean {
        val result = true
        //check identifier null
        if (identity.isEmpty()) {
            finish.failed(kotlin.IllegalStateException("User Identifier must to set in class 'FlagsmithBuilder' first"))
            return false
        }

        return result
    }

    private fun startAPI() {
        val url = builder.baseUrl + "traits/"
        val header = NetworkFlag.getNetworkHeader(builder)

        ApiManager(
            url,
            header,
            getJsonPostBody(),
            object : INetworkListener {
                override fun success(response: String?) {
                    _parse(response!!)
                }

                override fun failed(exception: Exception) {
                    finish.failed(exception)
                }

            })
    }

    private fun getJsonPostBody(): String {
        val traitWithIdentity = TraitWithIdentity(key = trait.key, value = trait.value, identity = Identity(identity))
        val gson = Gson();
        return gson.toJson(traitWithIdentity)
    }

    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<TraitWithIdentity>() {}.type
            val responseFromJson: TraitWithIdentity = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed(e)
        }
    }


}