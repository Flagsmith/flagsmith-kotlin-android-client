package com.flagsmith.endpoints

import com.google.gson.Gson

data class AnalyticsEndpoint(private val eventMap: Map<String, Int?>) :
    FlagsmithPostEndpoint(path = "/analytics/flags", body = Gson().toJson(eventMap))