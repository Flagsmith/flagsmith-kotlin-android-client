package com.flagsmith.internal;

import android.util.Log
import com.flagsmith.Flagsmith
import com.flagsmith.entities.Flag
import com.flagsmith.entities.IdentityFlagsAndTraits;
import com.flagsmith.entities.TraitWithIdentity
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.put
import com.github.kittinunf.result.isSuccess
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.*

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET;
import retrofit2.http.POST
import retrofit2.http.Query;

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

        fun create(baseUrl: String, environmentKey: String): FlagsmithRetrofitService {
            fun interceptor(environmentKey: String): Interceptor {
                return Interceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("X-environment-key", environmentKey)
                        .build()
                    chain.proceed(request)
                }
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor(environmentKey))
                .callTimeout(REQUEST_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(READ_WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(READ_WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build()


            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(ResultCallAdapterFactory.create())
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

// Convert a Retrofit Call to a Result by extending the Call class, also caching the result
fun <T : Any> Call<T>.cachedEnqueueWithResult(cache: Cache<T>?, result: (Result<T>) -> Unit) {
    val cacheKey = this.request().url().toString()
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                cache?.put(key = cacheKey, putValue = body).also { cacheResult ->
                    if (cacheResult != null) {
                        if (!cacheResult.isSuccess()) {
                            Log.e("Flagsmith", "Failed to cache flags and traits")
                        }
                    }
                }
                result(Result.success(response.body()!!))
            } else {
                // Reuse the onFailure callback to handle non-200 responses and avoid code duplication
                onFailure(call, HttpException(response))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (cache != null) {
                cache.get(key = Flagsmith.DEFAULT_CACHE_KEY).fold(
                    success = { value ->
                        Log.i("Flagsmith", "Using cached result")
                        result(Result.success(value))
                    },
                    failure = { err ->
                        Log.e("Flagsmith", "Failed to get cached result")
                        result(Result.failure(err))
                    }
                )
            } else {
                result(Result.failure(t))
            }
        }
    })
}
