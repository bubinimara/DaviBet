package io.github.bubinimara.davibet.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.flow.Flow


/**
 *
 * Created by Davide Parise
 */
@Dao
interface TweetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweet(tweet: Tweet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweet(tweet: List<Tweet>)

    @Query("select * from tweets order by tweetId desc limit 50")
    fun getTweets(): Flow<List<Tweet>>
}