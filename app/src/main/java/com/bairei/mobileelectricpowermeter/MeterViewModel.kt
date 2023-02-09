package com.bairei.mobileelectricpowermeter

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bairei.mobileelectricpowermeter.data.Meter
import com.bairei.mobileelectricpowermeter.data.MeterRepository
import kotlinx.coroutines.launch

class MeterViewModel(private val meterRepository: MeterRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val latestMeters: LiveData<List<Meter>> = meterRepository.latest30MeterReadings.asLiveData()

    val allMeters: LiveData<List<Meter>> = meterRepository.allMeterReadings.asLiveData()

    // Launching a new coroutine to insert the data in a non-blocking way
    fun insert(vararg meter: Meter) = viewModelScope.launch {
        meterRepository.insert(*meter)
    }

    fun delete(meter: Meter) = viewModelScope.launch {
        meterRepository.delete(meter)
    }
}

class MeterViewModelFactory(private val repository: MeterRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeterViewModel::class.java)) {
            return MeterViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel class: ${modelClass.name}")
    }
}
