package com.flagsmith

data class FlagsmithCacheConfig (
        val enableCache: Boolean = true,
        val cacheTTLSeconds: Long = 3600L, // Default to 1 hour
        val cacheSize: Long = 10L * 1024L * 1024L, // 10 MB
        val requestTimeoutSeconds: Long = 4L,
        val readAndWriteTimeoutSeconds: Long = 6L,
)
