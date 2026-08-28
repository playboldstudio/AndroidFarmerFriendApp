package com.example.androidfarmerfriend.data.util

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Invoke a suspend [block] and wrap its result in [UiState], mapping any
 * exception to [UiState.Error]. Shared by the list-backed ViewModels so the
 * fetch → Loading/Success/Error plumbing lives in exactly one place.
 */
suspend fun <T> loadSafely(block: suspend () -> T): UiState<T> = try {
    UiState.Success(block())
} catch (e: Exception) {
    UiState.Error(e.message ?: "Failed to load data")
}

/** Common Success-unwrapping idiom used by derived-list computations. */
fun <T> UiState<List<T>>.orEmpty(): List<T> = (this as? UiState.Success)?.data ?: emptyList()
