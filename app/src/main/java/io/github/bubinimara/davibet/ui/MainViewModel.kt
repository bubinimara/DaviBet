package io.github.bubinimara.davibet.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bubinimara.davibet.App
import io.github.bubinimara.davibet.Event
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.db.AppDb
import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.NetworkMonitor
import io.github.bubinimara.davibet.data.network.NetworkServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {
    companion object{
        const val TAG = "MainViewModel"
    }

    // todo:inject with hilt
    private val repository:DataRepositoryImpl

    // current job to fetch tweets
    private var job:Job? = null


    private val _tweets = MutableLiveData<List<Tweet>>()
    val tweets:LiveData<List<Tweet>> = _tweets

    private val _eventConnection = MutableLiveData<Event<Boolean>>()
    val eventConnection:LiveData<Event<Boolean>> = _eventConnection


    init {
        val networkServices = NetworkServices()
        val networkMonitor = App.networkMonitor!!
        val databaseService = App.database!!
        repository = DataRepositoryImpl(networkServices.apiService,databaseService.tweetDat())

        viewModelScope.launch {
            networkMonitor.isAvailable().collect {isConnected->
                _eventConnection.value = Event(isConnected)
                if(isConnected){
                    load()
                }else{
                    cancelJobs()
                }
            }
        }
    }
    private fun cancelJobs(){
        if(job!=null) job!!.cancel()
        job = null
    }
    fun load() {
        cancelJobs() // cancel previous job
        job = viewModelScope.launch {
            repository.getTweets("Brasil").collect {
                Log.d(TAG, "load: Received " + it.size)
                _tweets.value = it
            }
        }
    }
}