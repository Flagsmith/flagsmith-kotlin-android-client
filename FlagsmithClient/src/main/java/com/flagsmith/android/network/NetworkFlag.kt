package com.flagsmith.android.network

import com.flagsmith.builder.Flagsmith

object NetworkFlag {

    fun getNetworkHeader(builder: Flagsmith): HashMap<String, String> {
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["Authorization"] = "Token " + builder.apiAuthToken;
        hashMap["X-Environment-Key"] = builder.environmentKey ?: "";
        return hashMap;
    }

}