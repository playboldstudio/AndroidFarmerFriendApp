package com.example.androidfarmerfriend.data.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type

/**
 * Minimal file-based offline cache for the network-backed lists
 * (Schemes, Disease, CropNotes, Market). Content is stored as JSON so a screen
 * loads instantly and keeps showing the last good data when the network or the
 * upstream API (e.g. Wikipedia) is slow or down.
 *
 * Mirrors the existing [com.example.androidfarmerfriend.data.api.ApiClient]
 * singleton pattern: the app context is captured once via [init].
 */
object OfflineCache {

    private var appContext: Context? = null

    /** Call once from Application.onCreate alongside [com.example.androidfarmerfriend.data.api.ApiClient.init]. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val gson = Gson()

    private fun dir(): File =
        File(appContext?.filesDir ?: throw IllegalStateException("OfflineCache.init not called"), "offline")

    /**
     * Returns a cached value for [key] when present and fresh (≤ [ttlMillis]),
     * otherwise runs [fetcher]. On a fetch failure, falls back to any cached
     * value (even a stale one) so previously-loaded screens never blank out;
     * rethrows only when there is nothing cached at all.
     *
     * [type] is the runtime type of T (e.g. `List<Scheme>::class.java`, or
     * `OfflineCache.listType(Scheme::class.java)` for a list) so Gson can
     * round-trip it correctly.
     */
    suspend fun <T : Any> getOrFetch(
        key: String,
        ttlMillis: Long,
        type: Type,
        fetcher: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        val file = File(dir(), "$key.json")
        val entryType = TypeToken.getParameterized(CachedEntry::class.java, type).type
        val cached = readCached(file, entryType)
        val fresh = cached != null && System.currentTimeMillis() - cached.timestamp <= ttlMillis

        if (fresh) {
            @Suppress("UNCHECKED_CAST")
            cached!!.payload as T
        } else {
            try {
                val value = fetcher()
                write(file, CachedEntry(System.currentTimeMillis(), value), entryType)
                value
            } catch (e: Exception) {
                // Keep serving the last known-good content when available; only
                // surface the error when we have never loaded before.
                if (cached != null) {
                    @Suppress("UNCHECKED_CAST")
                    cached.payload as T
                } else {
                    throw e
                }
            }
        }
    }

    /** Shared [Type] for the generic lists we cache (e.g. `List<Scheme>`). */
    fun listType(element: Type): Type =
        TypeToken.getParameterized(List::class.java, element).type

    private data class CachedEntry<T>(val timestamp: Long = 0L, val payload: T? = null)

    private fun readCached(file: File, entryType: Type): CachedEntry<*>? = try {
        if (!file.exists()) null
        else gson.fromJson(file.readText(), entryType)
    } catch (_: Exception) {
        null
    }

    private fun <T> write(file: File, value: CachedEntry<T>, entryType: Type) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(value, entryType))
        } catch (_: Exception) {
            // Cache is best-effort; a write failure must not break the fetch.
        }
    }
}
