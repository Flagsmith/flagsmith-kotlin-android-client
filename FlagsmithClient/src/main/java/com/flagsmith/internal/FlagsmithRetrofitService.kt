package com.flagsmith.internal;

import android.content.Context
import android.util.Log
import com.flagsmith.FlagsmithCacheConfig
import com.flagsmith.entities.FeatureStatePutBody
import com.flagsmith.entities.Flag
import com.flagsmith.entities.IdentityFlagsAndTraits
import com.flagsmith.entities.TraitWithIdentity
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface FlagsmithRetrofitService {

    @GET("identities/")
    fun getIdentityFlagsAndTraits(@Query("identity") identity: String) : Call<IdentityFlagsAndTraits>

    @GET("flags/")
    fun getFlags() : Call<List<Flag>>

    @GET("environments/{environmentKey}/featurestates/{featureStateId}/")
    fun getFeatureStates(@Header("authorization") authToken:String,
                         @Path("featureStateId") featureStateId: String,
                         @Path("environmentKey") environmentKey: String,
                         @Query("feature_name") featureName: String) : Call<String>

    @PUT("environments/{environmentKey}/featurestates/{featureStateId}/")
    fun setFeatureStates(@Header("authorization") authToken:String,
                         @Path("featureStateId") featureStateId: String,
                         @Path("environmentKey") environmentKey: String,
                         @Body body: FeatureStatePutBody) : Call<Unit>

    @POST("traits/")
    fun postTraits(@Body trait: TraitWithIdentity) : Call<TraitWithIdentity>

    @POST("analytics/flags/")
    fun postAnalytics(@Body eventMap: Map<String, Int?>) : Call<Unit>

    companion object {
        fun create(
            baseUrl: String,
            environmentKey: String,
            context: Context?,
            cacheConfig: FlagsmithCacheConfig,
            requestTimeoutSeconds: Long,
            readTimeoutSeconds: Long,
            writeTimeoutSeconds: Long,
            timeTracker: FlagsmithEventTimeTracker,
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
                            .header("Content-Type", "application/json; charset=utf-8")
                            .header("Accept", "application/json")
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
                    val updatedAtString = response.header("x-flagsmith-document-updated-at")
                    Log.i("Flagsmith", "updatedAt: $updatedAtString")

                    // Update in the tracker (Flagsmith class) if we got a new value
                    tracker.lastSeenAt = updatedAtString?.toDoubleOrNull() ?: tracker.lastSeenAt
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

            // Make sure that we start with a clean cache
            client.cache?.evictAll()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return Pair(retrofit.create(FlagsmithRetrofitService::class.java), cache)
        }

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
