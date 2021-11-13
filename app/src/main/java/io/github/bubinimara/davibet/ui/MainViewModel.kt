package io.github.bubinimara.davibet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bubinimara.davibet.data.DataRepository
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.network.NetworkServices
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    companion object{
        const val TAG = "MainViewModel"
    }
    val repository:DataRepositoryImpl
    init {
        val ns:NetworkServices = NetworkServices()
        repository = DataRepositoryImpl(ns.apiService)
    }
    fun load() {
        Log.d(TAG, "load: ")
       /// repository.justStream()
        val l = viewModelScope.launch {
            repository.courinteCheck().collect {
                Log.d(TAG, "load: $it")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ")
    }
}