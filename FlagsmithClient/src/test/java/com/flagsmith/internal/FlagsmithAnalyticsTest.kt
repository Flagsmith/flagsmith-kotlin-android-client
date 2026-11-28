package com.flagsmith.internal

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class FlagsmithAnalyticsTest {
    @Test
    fun backgroundAndTimerFlushDoNotDuplicateInFlightCounts() =
        mockConstruction(JSONObject::class.java) { json, _ ->
            whenever(json.toString()).thenReturn("{}")
        }.use {
            val context = mock<Context>()
            val preferences = mock<SharedPreferences>()
            val editor = mock<SharedPreferences.Editor>()
            whenever(context.applicationContext).thenReturn(context)
            whenever(context.getSharedPreferences(any(), any()))
                .thenReturn(preferences)
            whenever(preferences.edit()).thenReturn(editor)
            whenever(editor.remove(any())).thenReturn(editor)
            whenever(editor.putString(any(), any())).thenReturn(editor)
            val service = mock<FlagsmithRetrofitService>()
            val call = mock<Call<Unit>>()
            whenever(service.postAnalytics(any())).thenReturn(call)
            val analytics = FlagsmithAnalytics(context, service, 10)

            analytics.trackEvent("banner")
            analytics.flush()
            analytics.trackEvent("banner")
            analytics.flush()

            val batches = argumentCaptor<Map<String, Int?>>()
            verify(service).postAnalytics(batches.capture())
            assertEquals(mapOf("banner" to 1), batches.firstValue)
            val callbacks = argumentCaptor<Callback<Unit>>()
            verify(call).enqueue(callbacks.capture())
            callbacks.firstValue.onResponse(call, Response.success(Unit))

            // A new count recorded during the first request must still be sent.
            val secondCall = mock<Call<Unit>>()
            whenever(service.postAnalytics(any())).thenReturn(secondCall)
            analytics.flush()
            val secondCallback = argumentCaptor<Callback<Unit>>()
            verify(secondCall).enqueue(secondCallback.capture())
            secondCallback.firstValue.onFailure(secondCall, IOException("offline"))

            // Failure retains that count for a later flush.
            val thirdCall = mock<Call<Unit>>()
            whenever(service.postAnalytics(any())).thenReturn(thirdCall)
            analytics.flush()
            val allBatches = argumentCaptor<Map<String, Int?>>()
            verify(service, times(3)).postAnalytics(allBatches.capture())
            assertEquals(List(3) { mapOf("banner" to 1) }, allBatches.allValues)
            analytics.close()
        }
}
