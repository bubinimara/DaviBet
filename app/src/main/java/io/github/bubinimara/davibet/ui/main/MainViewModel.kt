package io.github.bubinimara.davibet.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bubinimara.davibet.Event
import io.github.bubinimara.davibet.R
import io.github.bubinimara.davibet.data.DataRepository
import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.NetworkException
import io.github.bubinimara.davibet.data.network.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    // current text to search
    private var search:String = ""

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
                    if(job == null && search.isNotEmpty())
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

    /**
     * Search for tweet
     */
    fun search(text:String){
        if(text.isEmpty()){
            _eventError.value = Event(R.string.error_search_filed_empty)
            return
        }
        // others checks ...
        search = text
        load()
    }

    /**
     * collect tweets
     */
    private fun load() {
        cancelJobs() // cancel previous job
        job = viewModelScope.launch {
            repository.getTweets(search)
                .catch { e->
                    Log.e(TAG, "load: Error" )
                    when(e){
                        is NetworkException ->{
                            if(e.code == 420) {
                                _eventError.value = Event(R.string.error_net_to_much_call)
                            }else{ // 202
                                _eventError.value = Event(R.string.error_net_no_server_response)
                            }
                        }
                        is HttpException -> _eventError.value = Event(R.string.error_net)
                        else -> _eventError.value = Event(R.string.error_unknown)
                    }
                }
                .collect {
                Log.d(TAG, "load: Received " + it.size)
                _tweets.value = it
            }
        }
    }


}