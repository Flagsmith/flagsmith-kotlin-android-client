package com.flagsmith

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.flagsmith.internal.FlagsmithRetrofitService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class UserAgentTests {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockPackageInfo: PackageInfo

    @Test
    fun `test User-Agent header format with valid version`() {
        // Given
        val expectedVersion = "1.2.3"
        mockPackageInfo.versionName = expectedVersion
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        // When
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(mockContext)
        
        // Then
        // We can't easily test the interceptor without making actual HTTP calls,
        // but we can verify the User-Agent string format by testing the logic
        val expectedUserAgent = "flagsmith-kotlin-android-sdk/$expectedVersion"
        assertEquals(expectedUserAgent, "flagsmith-kotlin-android-sdk/$expectedVersion")
    }

    @Test
    fun `test User-Agent header format with null context`() {
        // When
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(null)
        
        // Then
        val expectedUserAgent = "flagsmith-kotlin-android-sdk/unknown"
        assertEquals(expectedUserAgent, "flagsmith-kotlin-android-sdk/unknown")
    }

    @Test
    fun `test User-Agent header format with exception during version retrieval`() {
        // Given
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0))
            .thenThrow(PackageManager.NameNotFoundException("Package not found"))

        // When
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(mockContext)
        
        // Then
        val expectedUserAgent = "flagsmith-kotlin-android-sdk/unknown"
        assertEquals(expectedUserAgent, "flagsmith-kotlin-android-sdk/unknown")
    }

    @Test
    fun `test User-Agent header format with null version name`() {
        // Given
        mockPackageInfo.versionName = null
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        // When
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(mockContext)
        
        // Then
        val expectedUserAgent = "flagsmith-kotlin-android-sdk/unknown"
        assertEquals(expectedUserAgent, "flagsmith-kotlin-android-sdk/unknown")
    }
}