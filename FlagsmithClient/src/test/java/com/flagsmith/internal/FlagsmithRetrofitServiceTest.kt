package com.flagsmith.internal

import android.content.Context
import com.flagsmith.FlagsmithCacheConfig
import com.flagsmith.entities.FeatureStatePutBody
import okhttp3.Cache
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FlagsmithRetrofitServiceTest: FlagsmithRetrofitService {

    @GET("environments/{environmentKey}/featurestates/{featureStateId}/")
    fun getFeatureStates(@Header("authorization") authToken:String,
                         @Path("featureStateId") featureStateId: String,
                         @Path("environmentKey") environmentKey: String,
                         @Query("feature_name") featureName: String) : Call<String>

    @PUT("environments/{environmentKey}/featurestates/{featureStateId}/")
    fun setFeatureStates(@Header("authorization") authToken:String,
                         @Path("featureStateId") featureStateId: String,
                         @Path("environmentKey") environmentKey: String,
                         @Body body: FeatureStatePutBody
    ) : Call<Unit>

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T : FlagsmithRetrofitServiceTest> create(
            baseUrl: String,
            environmentKey: String,
            context: Context?,
            cacheConfig: FlagsmithCacheConfig,
            requestTimeoutSeconds: Long,
            readTimeoutSeconds: Long,
            writeTimeoutSeconds: Long,
            timeTracker: FlagsmithEventTimeTracker,
            klass: Class<T>
        ): Pair<FlagsmithRetrofitServiceTest, Cache?> {
            return FlagsmithRetrofitService.create(
                baseUrl = baseUrl,
                environmentKey = environmentKey,
                context = context,
                cacheConfig = cacheConfig,
                requestTimeoutSeconds = requestTimeoutSeconds,
                readTimeoutSeconds = readTimeoutSeconds,
                writeTimeoutSeconds = writeTimeoutSeconds,
                timeTracker = timeTracker,
                klass = klass
            ) as Pair<FlagsmithRetrofitServiceTest, Cache?>
        }
    }
}