package com.flagsmith.internal;

import com.flagsmith.entities.IdentityFlagsAndTraits;
import okhttp3.Interceptor
import okhttp3.OkHttpClient

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET;
import retrofit2.http.Query;

interface FlagsmithRetrofitService {

    @GET("identities/")
    fun getIdentitiesAndTraits(@Query("identity") identity: String) : Call<IdentityFlagsAndTraits>

    companion object {
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
