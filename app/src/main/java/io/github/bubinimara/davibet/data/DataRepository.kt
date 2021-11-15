package io.github.bubinimara.davibet.data

import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.flow.Flow


/**
 *
 * Created by Davide Parise on 12/11/21.
 */
interface DataRepository {
    /**
     * Streams Tweets
     * @param track Keywords to track. Phrases of keywords are specified by a comma-separated list
     */
    fun getTweets(track:String):Flow<List<Tweet>>
}