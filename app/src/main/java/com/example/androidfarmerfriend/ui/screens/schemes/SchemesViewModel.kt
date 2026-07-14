package com.example.androidfarmerfriend.ui.screens.schemes

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SchemesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _schemesState = MutableStateFlow<List<Scheme>>(emptyList())
    val schemesState: StateFlow<List<Scheme>> = _schemesState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _schemesState.value = repository.getSchemes()
    }
}
