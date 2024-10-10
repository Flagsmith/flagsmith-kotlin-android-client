package com.flagsmith.internal;

import android.content.Context
import android.util.Log
import com.flagsmith.FlagsmithCacheConfig
import com.flagsmith.entities.Flag
import com.flagsmith.entities.IdentityAndTraits
import com.flagsmith.entities.IdentityFlagsAndTraits
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FlagsmithRetrofitService {

    @GET("identities/")
    fun getIdentityFlagsAndTraits(@Query("identifier") identity: String, @Query("transient") transient: Boolean = false) : Call<IdentityFlagsAndTraits>

    @GET("flags/")
    fun getFlags() : Call<List<Flag>>

    // todo: rename this function
    @POST("identities/")
    fun postTraits(@Body identity: IdentityAndTraits) : Call<IdentityFlagsAndTraits>

    @POST("analytics/flags/")
    fun postAnalytics(@Body eventMap: Map<String, Int?>) : Call<Unit>

    companion object {
        private const val UPDATED_AT_HEADER = "x-flagsmith-document-updated-at"
        private const val ACCEPT_HEADER_VALUE = "application/json"
        private const val CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8"

        fun <T : FlagsmithRetrofitService> create(
            baseUrl: String,
            environmentKey: String,
            context: Context?,
            cacheConfig: FlagsmithCacheConfig,
            requestTimeoutSeconds: Long,
            readTimeoutSeconds: Long,
            writeTimeoutSeconds: Long,
            timeTracker: FlagsmithEventTimeTracker,
            klass: Class<T>
        ): Pair<FlagsmithRetrofitService, Cache?> {
            fun cacheControlInterceptor(): Interceptor {
                return Interceptor { chain ->
                    val response = chain.proceed(chain.request())
                    response.newBuilder()
                        .header("Cache-Control", "public, max-age=${cacheConfig.cacheTTLSeconds}")
                        .removeHeader("Pragma")
                        .build()
                }
            }

            fun jsonContentTypeInterceptor(): Interceptor {
                return Interceptor { chain ->
                    val request = chain.request()
                    if (chain.request().method == "POST" || chain.request().method == "PUT" || chain.request().method == "PATCH") {
                        val newRequest = request.newBuilder()
                            .header("Content-Type", CONTENT_TYPE_HEADER_VALUE)
                            .header("Accept", ACCEPT_HEADER_VALUE)
                            .build()
                        chain.proceed(newRequest)
                    } else {
                        chain.proceed(request)
                    }
                }
            }

            fun updatedAtInterceptor(tracker: FlagsmithEventTimeTracker): Interceptor {
                return Interceptor { chain ->
                    val response = chain.proceed(chain.request())
                    val updatedAtString = response.header(UPDATED_AT_HEADER)
                    Log.i("Flagsmith", "updatedAt: $updatedAtString")

                    // Update in the tracker (Flagsmith class) if we got a new value
                    tracker.lastFlagFetchTime = updatedAtString?.toDoubleOrNull() ?: tracker.lastFlagFetchTime
                    return@Interceptor response
                }
            }

            val cache = if (context != null && cacheConfig.enableCache) Cache(context.cacheDir, cacheConfig.cacheSize) else null

            val client = OkHttpClient.Builder()
                .addInterceptor(envKeyInterceptor(environmentKey))
                .addInterceptor(updatedAtInterceptor(timeTracker))
                .addInterceptor(jsonContentTypeInterceptor())
                .let { if (cacheConfig.enableCache) it.addNetworkInterceptor(cacheControlInterceptor()) else it }
                .callTimeout(requestTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .cache(cache)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return Pair(retrofit.create(klass), cache)
        }

        // This is used by both the FlagsmithRetrofitService and the FlagsmithEventService
        fun envKeyInterceptor(environmentKey: String): Interceptor {
            return Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-environment-key", environmentKey)
                    .build()
                chain.proceed(request)
            }
        }
    }
}

// Convert a Retrofit Call to a standard Kotlin Result by extending the Call class
// This avoids having to use the suspend keyword in the FlagsmithClient to break the API
// And also avoids a lot of code duplication
fun <T> Call<T>.enqueueWithResult(defaults: T? = null, result: (Result<T>) -> Unit) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful && response.body() != null) {
                result(Result.success(response.body()!!))
            } else {
                onFailure(call, HttpException(response))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            // If we've got defaults to return, return them
            if (defaults != null) {
                result(Result.success(defaults))
            } else {
                result(Result.failure(t))
            }
        }
    })
}
