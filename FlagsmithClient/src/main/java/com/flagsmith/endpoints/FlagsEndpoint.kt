package com.flagsmith.endpoints

import com.flagsmith.entities.Flag
import com.flagsmith.entities.FlagListDeserializer

object FlagsEndpoint : GetEndpoint<List<Flag>>(
    path = "/flags/",
    deserializer = FlagListDeserializer()
)