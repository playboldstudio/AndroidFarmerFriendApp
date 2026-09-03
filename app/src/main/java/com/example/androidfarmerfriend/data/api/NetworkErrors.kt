package com.example.androidfarmerfriend.data.api

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * Central home for all network-failure handling in the app:
 *  - [record] sends any failure to Crashlytics (with an origin tag) + logcat,
 *    so the real exception type and stack trace are never lost.
 *  - [friendlyMessage] maps a thrown exception to a human-readable string so
 *    users never see raw developer text like "HTTP 443".
 *
 * Keep every call fire-and-forget: neither helper throws or rethrows, so callers
 * can safely `record` and then fall back to an empty/offline state.
 */
object NetworkErrors {

    private const val TAG = "NetworkErrors"

    /** Record a failure so it reaches Crashlytics with a real stack trace. */
    fun record(origin: String, e: Throwable) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey("origin", origin)
            crashlytics.recordException(e)
        } catch (_: Exception) {
            // Crashlytics itself unavailable (e.g. very early startup) — keep going.
        }
        Log.e(TAG, "$origin failed: ${e.javaClass.simpleName}: ${e.message ?: "(no message)"}", e)
    }

    /** Map a thrown exception to a user-facing, honest message. */
    fun friendlyMessage(e: Throwable): String = when (e) {
        is SocketTimeoutException -> "Server took too long to respond. Please retry."
        is UnknownHostException -> "Can't reach the server. Check your internet connection."
        is ConnectException -> "Could not connect. Check your connection and retry."
        is HttpException -> when (e.code()) {
            404 -> "Requested data not found."
            in 500..599 -> "Server is busy right now. Please try again shortly."
            429 -> "Too many requests. Please try again shortly."
            else -> "Server returned an error (${e.code()})."
        }
        is IOException -> "Network problem. Check your connection and retry."
        else -> e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please retry."
    }
}
