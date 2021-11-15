package io.github.bubinimara.davibet.data.db

import androidx.room.*
import io.github.bubinimara.davibet.AppConfig
import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.flow.Flow


/**
 *
 * Created by Davide Parise
 */
@Dao
interface TweetDao {
    companion object{
        private const val TABLE_LIMIT = AppConfig.DB_TABLE_SIZE
        var TWEET_LIFETIME = AppConfig.DB_TWEET_LIFETIME
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweet(tweet: Tweet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweet(tweet: List<Tweet>)

    @Query("select * from tweets order by tweetId desc limit $TABLE_LIMIT" )
    fun getTweets(): Flow<List<Tweet>>

    @Query("delete from tweets")
    fun removeTweets()

    @Query("delete from tweets where timestamp < :max ")
    fun removeExpired(max: Long)

    @Query("DELETE FROM tweets where tweetId NOT IN (SELECT tweetId from tweets ORDER BY tweetId DESC LIMIT $TABLE_LIMIT)")
    fun truncateTable()

    @Transaction
    fun removeExpired(){
        removeExpired(System.currentTimeMillis() - TWEET_LIFETIME)
    }
}