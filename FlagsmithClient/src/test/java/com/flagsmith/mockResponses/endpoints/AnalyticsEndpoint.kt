package com.flagsmith.mockResponses.endpoints

import com.google.gson.Gson

data class AnalyticsEndpoint(private val eventMap: Map<String, Int?>) :
    PostEndpoint<Unit>(
        path = "/analytics/flags/",
        body = Gson().toJson(eventMap),
    )