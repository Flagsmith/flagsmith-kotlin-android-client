package com.flagsmith.android.network

import com.flagsmith.android.flagsmith.builder.FlagsmithBuilder

object NetworkFlagUtils {


    fun getNetworkHeader( builder: FlagsmithBuilder) : HashMap<String, String> {
        val hashMap : HashMap<String, String>       = HashMap  ()
        hashMap["Authorization"] = "Token " + builder.tokenApi;
        hashMap["X-Environment-Key"] = builder.environmentId ?:"";
        return hashMap;
    }


}