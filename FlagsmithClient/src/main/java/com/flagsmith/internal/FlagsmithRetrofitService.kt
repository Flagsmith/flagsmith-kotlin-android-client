package com.flagsmith.internal;

import com.flagsmith.entities.Flag
import com.flagsmith.entities.IdentityFlagsAndTraits;
import com.flagsmith.entities.TraitWithIdentity
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

    companion object {
        private const val REQUEST_TIMEOUT_SECONDS = 2L
        private const val READ_WRITE_TIMEOUT_SECONDS = 2L

        fun create(baseUrl: String, environmentKey: String): FlagsmithRetrofitService {
            fun interceptor(environmentKey: String) : Interceptor {
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

}
