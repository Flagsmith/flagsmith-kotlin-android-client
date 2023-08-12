package com.blu.flagsmith

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FlagsmithRetrofitHelper {
    companion object {
        fun create(
            baseUrl: String,
            environmentKey: String,
            context: Context?,
            cacheConfig: FlagsmithCacheConfigModel,
            requestTimeoutSeconds: Long,
            readTimeoutSeconds: Long,
            writeTimeoutSeconds: Long,
        ): FlagsmithServices {
            fun cacheControlInterceptor(): Interceptor {
                return Interceptor { chain ->
                    val response = chain.proceed(chain.request())
                    response.newBuilder()
                        .header("Cache-Control", "public, max-age=${cacheConfig.cacheTTLSeconds}")
                        .removeHeader("Pragma")
                        .build()
                }
            }

            fun envKeyInterceptor(environmentKey: String): Interceptor {
                return Interceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("X-environment-key", environmentKey)
                        .build()
                    chain.proceed(request)
                }
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(envKeyInterceptor(environmentKey))
                .let { if (cacheConfig.enableCache) it.addNetworkInterceptor(cacheControlInterceptor()) else it }
                .callTimeout(requestTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .cache(
                    if (context != null && cacheConfig.enableCache) okhttp3.Cache(
                        context.cacheDir,
                        cacheConfig.cacheSize
                    ) else null
                )
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(FlagsmithServices::class.java)
        }
    }
}
