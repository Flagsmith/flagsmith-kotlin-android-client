package com.flagsmith.internal

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class FlagsmithAnalyticsTest {
    private lateinit var jsonObjects: MockedConstruction<JSONObject>
    private val service = mock<FlagsmithRetrofitService>()
    private val calls = mutableListOf<Call<Unit>>()
    private val batches = mutableListOf<Map<String, Int?>>()

    @Before
    fun setUp() {
        jsonObjects = mockConstruction(JSONObject::class.java) { json, _ ->
            whenever(json.toString()).thenReturn("{}")
        }
        whenever(service.postAnalytics(any())).thenAnswer { invocation ->
            batches.add(invocation.getArgument<Map<String, Int?>>(0).toMap())
            mock<Call<Unit>>().also { calls.add(it) }
        }
    }

    @After
    fun tearDown() {
        jsonObjects.close()
    }

    @Test
    fun flushDuringASendDoesNotSendTheInFlightCountsAgain() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")
        analytics.flush()

        // When
        analytics.trackEvent("banner")
        analytics.flush()

        // Then
        assertEquals(listOf(mapOf("banner" to 1)), batches)
    }

    @Test
    fun flushDuringASendSendsTheRemainingCountsWhenTheSendCompletes() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")
        analytics.flush()
        analytics.trackEvent("banner")
        analytics.flush()

        // When
        complete(0, succeeded = true)

        // Then
        assertEquals(List(2) { mapOf("banner" to 1) }, batches)
    }

    @Test
    fun flushDuringASendSendsNothingMoreWhenNoCountsRemain() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")
        analytics.flush()
        analytics.flush()

        // When
        complete(0, succeeded = true)

        // Then
        assertEquals(listOf(mapOf("banner" to 1)), batches)
    }

    @Test
    fun failedSendKeepsItsCountsForTheNextFlush() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")
        analytics.flush()
        complete(0, succeeded = false)

        // When
        analytics.flush()

        // Then
        assertEquals(List(2) { mapOf("banner" to 1) }, batches)
    }

    @Test
    fun closeStartsASendOfTheBufferedCounts() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")

        // When
        analytics.close()

        // Then
        assertEquals(listOf(mapOf("banner" to 1)), batches)
        verify(calls[0]).enqueue(any())
    }

    @Test
    fun closeDuringASendSendsTheRemainingCountsWhenTheSendCompletes() {
        // Given
        val analytics = createAnalytics()
        analytics.trackEvent("banner")
        analytics.flush()
        analytics.trackEvent("banner")

        // When
        analytics.close()
        complete(0, succeeded = true)

        // Then
        assertEquals(List(2) { mapOf("banner" to 1) }, batches)
    }

    @Test
    fun closeIgnoresLaterCountsAndFlushes() {
        // Given
        val analytics = createAnalytics()
        analytics.close()

        // When
        analytics.trackEvent("banner")
        analytics.flush()

        // Then
        assertEquals(emptyList<Map<String, Int?>>(), batches)
    }

    private fun createAnalytics(): FlagsmithAnalytics {
        val context = mock<Context>()
        val preferences = mock<SharedPreferences>()
        val editor = mock<SharedPreferences.Editor>()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)
        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        return FlagsmithAnalytics(context, service, 10)
    }

    private fun complete(index: Int, succeeded: Boolean) {
        val call = calls[index]
        val callback = argumentCaptor<Callback<Unit>>()
        verify(call).enqueue(callback.capture())
        if (succeeded) callback.firstValue.onResponse(call, Response.success(Unit))
        else callback.firstValue.onFailure(call, IOException("offline"))
    }
}
