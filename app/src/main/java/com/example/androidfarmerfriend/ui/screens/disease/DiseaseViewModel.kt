package com.example.androidfarmerfriend.ui.screens.disease

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiseaseViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _diseasesState = MutableStateFlow<List<Disease>>(emptyList())
    val diseasesState: StateFlow<List<Disease>> = _diseasesState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _diseasesState.value = repository.getDiseases()
    }
}
