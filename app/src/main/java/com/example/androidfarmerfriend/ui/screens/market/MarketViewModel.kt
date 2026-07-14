package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarketViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _cropsState = MutableStateFlow<List<Crop>>(emptyList())
    val cropsState: StateFlow<List<Crop>> = _cropsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _cropsState.value = repository.getCrops()
    }
}
