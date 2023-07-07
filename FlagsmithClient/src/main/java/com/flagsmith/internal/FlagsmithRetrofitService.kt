package com.flagsmith.internal;

import android.content.Context
import android.util.Log
import com.flagsmith.entities.Flag
import com.flagsmith.entities.IdentityFlagsAndTraits
import com.flagsmith.entities.TraitWithIdentity
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
    fun getIdentityFlagsAndTraits(@Query("identity") identity: String) : Call<IdentityFlagsAndTraits>

    @GET("flags/")
    fun getFlags() : Call<List<Flag>>

    @POST("traits/")
    fun postTraits(@Body trait: TraitWithIdentity) : Call<TraitWithIdentity>

    @POST("analytics/flags/")
    fun postAnalytics(@Body eventMap: Map<String, Int?>) : Call<TraitWithIdentity>

    companion object {
        //TODO: Consider these might be fine for server side, but a bit short for mobile
        private const val REQUEST_TIMEOUT_SECONDS = 2L
        private const val READ_WRITE_TIMEOUT_SECONDS = 5L
        private const val cacheSize = 10L * 1024L * 1024L // 10 MB

        fun create(
            baseUrl: String,
            environmentKey: String,
            ttlSeconds: Long,
            context: Context?,
            enableCache: Boolean
        ): FlagsmithRetrofitService {
            fun cacheControlInterceptor(ttlSeconds: Long?): Interceptor {
                return Interceptor { chain ->
                    val response = chain.proceed(chain.request())
                    val maxAge = ttlSeconds
                    response.newBuilder()
                        .header("Cache-Control", "public, max-age=$maxAge")
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
                .addNetworkInterceptor(cacheControlInterceptor(ttlSeconds))
                .callTimeout(REQUEST_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(READ_WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(READ_WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .cache(if (context != null && enableCache) okhttp3.Cache(context.cacheDir, cacheSize) else null)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(FlagsmithRetrofitService::class.java)
        }
    }
}

// Convert a Retrofit Call to a Result by extending the Call class
fun <T> Call<T>.enqueueWithResult(result: (Result<T>) -> Unit) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful && response.body() != null) {
                result(Result.success(response.body()!!))
            } else {
                result(Result.failure(HttpException(response)))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            result(Result.failure(t))
        }
    })
}

