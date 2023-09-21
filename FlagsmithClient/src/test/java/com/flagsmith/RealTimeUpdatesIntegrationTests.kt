package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import com.flagsmith.entities.FeatureStatePutBody
import com.flagsmith.internal.FlagsmithEventTimeTracker
import com.flagsmith.internal.FlagsmithRetrofitService
import com.flagsmith.internal.FlagsmithRetrofitServiceTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File

class RealTimeUpdatesIntegrationTests : FlagsmithEventTimeTracker {

    private lateinit var flagsmith: Flagsmith

    private lateinit var retrofitService: FlagsmithRetrofitServiceTest

    // You'll need a valid account to test this
    private val environmentKey = System.getenv("INTEGRATION_TESTS_ENVIRONMENT_KEY")
        ?: throw Exception("INTEGRATION_TESTS_ENVIRONMENT_KEY not set")
    private val apiToken = System.getenv("INTEGRATION_TESTS_API_TOKEN")
        ?: throw Exception("INTEGRATION_TESTS_API_TOKEN not set")
    private val authToken = "Token $apiToken"
    private val featureId = System.getenv("INTEGRATION_TESTS_FEATURE_NAME")
        ?: throw Exception("INTEGRATION_TESTS_FEATURE_NAME not set")
    private val featureStateId = System.getenv("INTEGRATION_TESTS_FEATURE_STATE_ID")
        ?: throw Exception("INTEGRATION_TESTS_FEATURE_STATE_ID not set")

    @Mock
    private lateinit var mockApplicationContext: Context

    @Mock
    private lateinit var mockContextResources: Resources

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    // FlagsmithEventTimeTracker
    override var lastFlagFetchTime: Double = 0.0

    @Before
    fun setup() {
        setupMocks()

        // We need the cache otherwise we'd be getting the new values from the server all the time
        // Rather than seeing the realtime updates
        flagsmith = Flagsmith(
            environmentKey = environmentKey!!,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = true),
            enableRealtimeUpdates = true,
            context = mockApplicationContext,
        )

        val requestTimeoutSeconds: Long = 4L
        val readTimeoutSeconds: Long = 6L
        val writeTimeoutSeconds: Long = 6L

        retrofitService = FlagsmithRetrofitServiceTest.create(
            baseUrl = "https://api.flagsmith.com/api/v1/", environmentKey = environmentKey, context = mockApplicationContext,
            cacheConfig = FlagsmithCacheConfig(enableCache = false),
            timeTracker = this, requestTimeoutSeconds = requestTimeoutSeconds, readTimeoutSeconds = readTimeoutSeconds,
            writeTimeoutSeconds = writeTimeoutSeconds,  klass = FlagsmithRetrofitServiceTest::class.java).first
    }

    @After
    fun tearDown() {
    }

    private fun setupMocks() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(mockApplicationContext.getResources()).thenReturn(mockContextResources)
        Mockito.`when`(
            mockApplicationContext.getSharedPreferences(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(
            mockSharedPreferences
        )
        Mockito.`when`(mockApplicationContext.cacheDir).thenReturn(File("cache"))

        Mockito.`when`(mockContextResources.getString(ArgumentMatchers.anyInt())).thenReturn("mocked string")
        Mockito.`when`(mockContextResources.getStringArray(ArgumentMatchers.anyInt())).thenReturn(
            arrayOf(
                "mocked string 1",
                "mocked string 2"
            )
        )
        Mockito.`when`(mockContextResources.getColor(ArgumentMatchers.anyInt())).thenReturn(Color.BLACK)
        Mockito.`when`(mockContextResources.getBoolean(ArgumentMatchers.anyInt())).thenReturn(false)
        Mockito.`when`(mockContextResources.getDimension(ArgumentMatchers.anyInt())).thenReturn(100f)
        Mockito.`when`(mockContextResources.getIntArray(ArgumentMatchers.anyInt()))
            .thenReturn(intArrayOf(1, 2, 3))
    }

    @Test
    fun testEnvironmentVariablesArentEmpty() {
        Assert.assertTrue(environmentKey.isNotEmpty())
        Assert.assertTrue(apiToken.isNotEmpty())
        Assert.assertTrue(featureId.isNotEmpty())
        Assert.assertTrue(featureStateId.isNotEmpty())
    }

    /// Update after 5 secs, should be done in 60 seconds or fail
    @Test(timeout = 60000)
    fun testGettingFlagsWithRealtimeUpdatesAfterPuttingNewValue() = runBlocking {
        // Get the current value
        val currentFlagValueString =
            flagsmith.getValueForFeatureSync(featureId).getOrThrow() as String?
        println("Type of currentFlagValueDouble: ${currentFlagValueString?.javaClass?.name}")
        Assert.assertNotNull(currentFlagValueString)
        val currentFlagValue: String = currentFlagValueString!!

        // After 5 seconds try to update the value using the retrofit service
        CoroutineScope(Dispatchers.IO).launch {
            // Wait 5 seconds before updating the value
            delay(5000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId, environmentKey!!, FeatureStatePutBody(true, "new-value"))
                .execute()
            if (!response.isSuccessful) println("ERROR response: $response")
            println("Response: $response")
            Assert.assertTrue(response.isSuccessful)
        }

        var newUpdatedFeatureValue: String? = ""
        do {
            newUpdatedFeatureValue = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.featureStateValue as String? ?: ""
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedFeatureValue.isNullOrEmpty() || newUpdatedFeatureValue == currentFlagValueString)

        Assert.assertEquals("new-value", newUpdatedFeatureValue)
    }

    // Update after 65 secs to ensure we've done a reconnect, should be done in 100 seconds or fail
    @Test(timeout = 100000)
    fun testGettingFlagsWithRealtimeUpdatesAfterPuttingNewValueAndReconnect() = runBlocking {
        // Get the current value
        val currentFlagValueString =
            flagsmith.getValueForFeatureSync(featureId).getOrThrow() as String?
        println("Type of currentFlagValueDouble: ${currentFlagValueString?.javaClass?.name}")
        Assert.assertNotNull(currentFlagValueString)

        CoroutineScope(Dispatchers.IO).launch {
            // Wait 65 seconds before updating the value
            // By this time the realtime service will have timed out (30 seconds) and reconnected
            delay(65000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId, environmentKey, FeatureStatePutBody(true, "new-value-after-reconnect"))
                .execute()
            if (!response.isSuccessful) println("ERROR response: $response")
            println("Response: $response")
            Assert.assertTrue(response.isSuccessful)
        }

        var newUpdatedFeatureValue: String? = ""
        do {
            newUpdatedFeatureValue = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.featureStateValue as String? ?: ""
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedFeatureValue.isNullOrEmpty() || newUpdatedFeatureValue == currentFlagValueString)

        Assert.assertEquals("new-value-after-reconnect", newUpdatedFeatureValue)
    }

    @Test(timeout = 60000)
    fun testGettingFlagsWithRealtimeUpdatesViaFlagUpdateFlow() = runBlocking {
        // Get the current value
        val currentFlagValueString =
            flagsmith.getValueForFeatureSync(featureId).getOrThrow() as String?
        Assert.assertNotNull(currentFlagValueString)

        // After 5 seconds try to update the value using the retrofit service
        CoroutineScope(Dispatchers.IO).launch {
            // Wait 5 seconds before updating the value
            delay(5000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId, environmentKey!!, FeatureStatePutBody(true, "new-value-via-flow"))
                .execute()
            if (!response.isSuccessful) println("Response: $response")
            println("Response: $response")
            Assert.assertTrue(response.isSuccessful)
        }

        var newUpdatedFeatureValue: String?
        do {
            // Check the value via the flag update flow, which should be current as the realtime updates come in
            newUpdatedFeatureValue = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.featureStateValue as String? ?: ""
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedFeatureValue.isNullOrEmpty() ||  newUpdatedFeatureValue == currentFlagValueString)

        println("newUpdatedFeatureValue: $newUpdatedFeatureValue")
        Assert.assertEquals("new-value-via-flow", newUpdatedFeatureValue)
    }
}