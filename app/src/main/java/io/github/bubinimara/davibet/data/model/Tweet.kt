package io.github.bubinimara.davibet.data.model

import java.util.concurrent.TimeUnit


/**
 *
 * Created by Davide Parise on 12/11/21.
 */
data class Tweet(val text:String,private val timestamp:Long = System.currentTimeMillis()){
    companion object{
        var MAX_TIME = TimeUnit.SECONDS.toMillis(30)
    }
    override fun toString(): String {
        return text
    }

    fun isAlive():Boolean{
        return (System.currentTimeMillis() - timestamp) < MAX_TIME
    }
}
