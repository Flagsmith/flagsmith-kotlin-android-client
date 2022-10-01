package com.flagsmith.android.network

import com.flagsmith.builder.FlagsmithBuilder

object NetworkFlagUtils {


    fun getNetworkHeader( builder: FlagsmithBuilder) : HashMap<String, String> {
        var hashMap : HashMap<String, String>       = HashMap  ()
        hashMap.put( "Authorization", "Token " + builder.tokenApi);
        hashMap.put( "X-Environment-Key", builder.environmentId ?:"");
        return hashMap;
    }


}