package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import com.flagsmith.entities.FeatureStatePutBody
import com.flagsmith.entities.Flag
import com.flagsmith.internal.FlagsmithEventTimeTracker
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

        // We need the cache configured in the integration tests, otherwise we'd be getting
        // the new values from the server all the time rather than seeing the values from the realtime update stream
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
        val currentFlags = flagsmith.getFeatureFlagsSync().getOrThrow()

        // Find our flag
        val currentFlag: Flag = currentFlags.first { flag -> flag.feature.name == featureId }

        // After 5 seconds try to update the enabled status to the opposite value using the retrofit service
        CoroutineScope(Dispatchers.IO).launch {
            // Wait 5 seconds before updating the value
            delay(5000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId,
                    environmentKey!!, FeatureStatePutBody(!currentFlag.enabled, "new-value"))
                .execute()

            Assert.assertTrue("Response should be successful: $response", response.isSuccessful)
        }

        var newUpdatedEnabledStatus: Boolean? = null
        do {
            newUpdatedEnabledStatus = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.enabled
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedEnabledStatus == null || newUpdatedEnabledStatus == currentFlag.enabled)

        Assert.assertEquals("Enabled status should be swapped", !currentFlag.enabled, newUpdatedEnabledStatus)

        // Now we need to make sure that the feature is enabled as it'll cause issues with some of the other tests
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitService
                .setFeatureStates(authToken, featureStateId,
                    environmentKey!!, FeatureStatePutBody(true, "new-value"))
                .execute()

            Assert.assertTrue("Response should be successful: $response", response.isSuccessful)
        }
        return@runBlocking
    }

    // Update after 35 secs to ensure we've done a reconnect, should be done in 60 seconds or fail
    // Though 120 seconds sounds like a long time it can take a while for the infrastructure to let
    // us know that the value has changed. The test still finishes as soon as the value is updated
    // so will be as quick as the infrastructure allows.
    @Test(timeout = 120_000)
    fun testGettingFlagsWithRealtimeUpdatesAfterPuttingNewValueAndReconnect() = runBlocking {
        val expectedNewValue = "new-value-after-reconnect"
        // Get the current value
        val currentFlagValueString =
            flagsmith.getValueForFeatureSync(featureId).getOrThrow() as String?
        Assert.assertNotNull(currentFlagValueString)

        CoroutineScope(Dispatchers.IO).launch {
            // By this time the realtime service will have timed out (30 seconds) and reconnected
            // So try again 5 seconds later
            delay(35000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId, environmentKey, FeatureStatePutBody(true, expectedNewValue))
                .execute()

            Assert.assertTrue("Response should be successful: $response", response.isSuccessful)
        }

        var newUpdatedFeatureValue: String? = ""
        do {
            newUpdatedFeatureValue = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.featureStateValue as String? ?: ""
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedFeatureValue.isNullOrEmpty() || newUpdatedFeatureValue == currentFlagValueString)

        Assert.assertEquals(expectedNewValue, newUpdatedFeatureValue)

        // Now get the flag again using the normal API and check the value is the same
        val newUpdatedFeatureValueFromApi = flagsmith.getValueForFeatureSync(featureId).getOrThrow() as String?
        Assert.assertEquals(expectedNewValue, newUpdatedFeatureValueFromApi)
    }

    @Test(timeout = 120_000)
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

            Assert.assertTrue("Response should be successful: $response", response.isSuccessful)
        }

        var newUpdatedFeatureValue: String?
        do {
            // Check the value via the flag update flow, which should be current as the realtime updates come in
            newUpdatedFeatureValue = flagsmith.flagUpdateFlow.value
                .find { flag -> flag.feature.name == featureId }?.featureStateValue as String? ?: ""
            delay(300L) // Delay a little while to give the CPU some time back
        } while (newUpdatedFeatureValue.isNullOrEmpty() ||  newUpdatedFeatureValue == currentFlagValueString)

        Assert.assertEquals("new-value-via-flow", newUpdatedFeatureValue)
    }
}
