package com.flagsmith

data class FlagsmithCacheConfig (
        val enableCache: Boolean = false,
        val cacheTTLSeconds: Long = 3600L, // Default to 1 hour
        val cacheSize: Long = 10L * 1024L * 1024L, // 10 MB
)
