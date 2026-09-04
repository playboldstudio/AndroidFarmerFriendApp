package com.example.androidfarmerfriend.ui.screens.schemes

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.loadSafely
import com.example.androidfarmerfriend.data.util.orEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SchemesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmerRepository()
    private val _state = MutableStateFlow(SchemesState())
    val state: StateFlow<SchemesState> = _state.asStateFlow()

    private val prefs = application.getSharedPreferences("scheme_prefs", Context.MODE_PRIVATE)

    private var currentLanguage: Language = Language.ENGLISH

    /**
     * Saved scheme identities, keyed by their source URL (stable across fetches
     * for a given language). Scheme.id is an unstable positional index, so it is
     * never used as a bookmark key.
     */
    private val savedUrls: MutableSet<String> =
        prefs.getStringSet(KEY_SAVED, emptySet())?.toMutableSet() ?: mutableSetOf()

    init {
        _state.value = rerender(_state.value.copy(savedUrls = savedUrls.toSet()))
    }

    fun loadData(language: Language = currentLanguage) {
        currentLanguage = language
        _state.value = rerender(_state.value.copy(schemesState = UiState.Loading, searchQuery = ""))
        viewModelScope.launch {
            _state.value = rerender(
                _state.value.copy(schemesState = loadSafely { repository.getSchemes(language) })
            )
        }
    }

    fun onEvent(event: SchemeEvent) {
        when (event) {
            is SchemeEvent.SelectFilter -> {
                _state.value = rerender(
                    _state.value.copy(selectedFilter = event.filter, searchQuery = "")
                )
            }
            is SchemeEvent.Search -> {
                _state.value = rerender(_state.value.copy(searchQuery = event.query))
            }
            is SchemeEvent.ToggleSave -> toggleSave(event.url)
            is SchemeEvent.Retry -> loadData()
        }
    }

    private fun toggleSave(url: String) {
        if (url.isBlank()) return
        if (savedUrls.add(url)) {
            prefs.edit().putStringSet(KEY_SAVED, savedUrls).apply()
        } else {
            savedUrls.remove(url)
            prefs.edit().putStringSet(KEY_SAVED, savedUrls).apply()
        }
        _state.value = rerender(_state.value.copy(savedUrls = savedUrls.toSet()))
    }

    /** Recompute the derived list once per state change instead of per read. */
    private fun rerender(s: SchemesState): SchemesState = s.copy(filteredSchemes = computeFiltered(s))

    private fun computeFiltered(s: SchemesState): List<Scheme> {
        val data = s.schemesState.orEmpty()
        // Apply the Saved filter first, then the search query on top.
        val filtered = when (s.selectedFilter) {
            SchemeFilterType.ALL -> data
            SchemeFilterType.SAVED -> data.filter { it.sourceUrl in s.savedUrls }
        }
        val query = s.searchQuery.trim().lowercase()
        return if (query.isEmpty()) filtered
        else filtered.filter {
            it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
        }
    }

    companion object {
        private const val KEY_SAVED = "saved_scheme_urls"
    }
}
