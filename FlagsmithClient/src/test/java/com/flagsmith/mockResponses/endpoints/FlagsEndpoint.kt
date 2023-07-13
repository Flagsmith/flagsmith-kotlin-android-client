package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.Flag

object FlagsEndpoint : GetEndpoint<List<Flag>>(
    path = "/flags/",
)