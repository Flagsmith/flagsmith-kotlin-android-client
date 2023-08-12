package com.blu.injection

import com.blu.flagsmith.BluFlagsmith
import com.blu.flagsmith.FlagsmithRemoteDataSource
import com.blu.flagsmith.FlagsDataSource
import com.blu.flagsmith.FlagsmithRetrofitHelper
import org.koin.dsl.module

val DataModule = module {
    single { (bluFlagSmith: BluFlagsmith) ->
        FlagsmithRetrofitHelper.create(
            baseUrl = bluFlagSmith.baseUrl,
            environmentKey = bluFlagSmith.environmentKey,
            context = bluFlagSmith.context,
            cacheConfig = bluFlagSmith.cacheConfig,
            requestTimeoutSeconds = bluFlagSmith.requestTimeoutSeconds,
            readTimeoutSeconds = bluFlagSmith.readTimeoutSeconds,
            writeTimeoutSeconds = bluFlagSmith.writeTimeoutSeconds
        ) }
    factory <FlagsDataSource>{ FlagsmithRemoteDataSource(get(),get()) }
}
