package io.github.bubinimara.davibet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

/**
 *
 * Created by Davide Parise
 */
@Entity(tableName = "tweets")
data class Tweet(val text:String,val timestamp:Long = System.currentTimeMillis()){
    companion object{
        var MAX_TIME = TimeUnit.SECONDS.toMillis(30)
    }

    @PrimaryKey(autoGenerate = true)
    var tweetId: Int = 0 // or foodId: Int? = null

    override fun toString(): String {
        return text
    }

    fun isAlive():Boolean{
        return (System.currentTimeMillis() - timestamp) < MAX_TIME
    }
}
