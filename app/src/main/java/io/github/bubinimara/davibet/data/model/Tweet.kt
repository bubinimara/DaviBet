package io.github.bubinimara.davibet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Created by Davide Parise
 */
@Entity(tableName = "tweets")
data class Tweet(val text:String,val timestamp:Long = System.currentTimeMillis()){
    @PrimaryKey(autoGenerate = true)
    var tweetId: Int = 0

    override fun toString(): String {
        return text
    }
}
