package io.github.bubinimara.davibet.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bubinimara.davibet.App
import io.github.bubinimara.davibet.Event
import io.github.bubinimara.davibet.R
import io.github.bubinimara.davibet.data.DataRepository
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.db.DatabaseService
import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.NetworkMonitor
import io.github.bubinimara.davibet.data.network.NetworkServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val networkMonitor: NetworkMonitor,
    val repository: DataRepository
) : ViewModel() {
    companion object{
        const val TAG = "MainViewModel"
    }

    // current job to fetch tweets
    private var job:Job? = null


    private val _tweets = MutableLiveData<List<Tweet>>()
    val tweets:LiveData<List<Tweet>> = _tweets

    private val _eventConnection = MutableLiveData<Event<Boolean>>()
    val eventConnection:LiveData<Event<Boolean>> = _eventConnection

    private val _eventError = MutableLiveData<Event<Int>>()
    val eventError:LiveData<Event<Int>> = _eventError


    init {

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
    var search:String = ""

    fun search(text:String){
        if(text.isEmpty()){
            _eventError.value = Event(R.string.error_search_filed_empty)
            return
        }
        // others checks ...
        search = text
        load()
    }
    private fun load() {
        cancelJobs() // cancel previous job
        job = viewModelScope.launch {
            repository.getTweets(search).collect {
                Log.d(TAG, "load: Received " + it.size)
                _tweets.value = it
            }
        }
    }


}