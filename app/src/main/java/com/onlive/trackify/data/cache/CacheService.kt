package com.onlive.trackify.data.cache

import androidx.collection.LruCache
import java.util.concurrent.TimeUnit

class CacheService {
    companion object {
        private const val DEFAULT_CACHE_SIZE = 100
        private const val DEFAULT_CACHE_TIME_MS = 10 * 60 * 1000L
        private val INSTANCE = CacheService()

        fun getInstance(): CacheService = INSTANCE
    }

    private val memoryCache = LruCache<String, CacheEntry>(DEFAULT_CACHE_SIZE)

    private val listCache = LruCache<String, CacheListEntry>(DEFAULT_CACHE_SIZE)

    fun <T> put(key: String, value: T?, timeToLive: Long = DEFAULT_CACHE_TIME_MS) {
        if (value == null) {
            memoryCache.remove(key)
            return
        }

        val expirationTime = System.currentTimeMillis() + timeToLive
        memoryCache.put(key, CacheEntry(value, expirationTime))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val cacheEntry = memoryCache.get(key) ?: return null

        if (System.currentTimeMillis() > cacheEntry.expirationTime) {
            memoryCache.remove(key)
            return null
        }

        return cacheEntry.value as? T
    }

    fun <T> putList(key: String, list: List<T>?, timeToLive: Long = DEFAULT_CACHE_TIME_MS) {
        if (list == null) {
            listCache.remove(key)
            return
        }

        val expirationTime = System.currentTimeMillis() + timeToLive
        listCache.put(key, CacheListEntry(list, expirationTime))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getList(key: String): List<T>? {
        val cacheEntry = listCache.get(key) ?: return null

        if (System.currentTimeMillis() > cacheEntry.expirationTime) {
            listCache.remove(key)
            return null
        }

        return cacheEntry.list as? List<T>
    }

    fun clearCache() {
        memoryCache.evictAll()
        listCache.evictAll()
    }

    fun clearCache(key: String) {
        memoryCache.remove(key)
        listCache.remove(key)
    }

    private data class CacheEntry(val value: Any, val expirationTime: Long)

    private data class CacheListEntry(val list: List<*>, val expirationTime: Long)
}