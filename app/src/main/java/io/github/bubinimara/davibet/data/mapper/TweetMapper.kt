package io.github.bubinimara.davibet.data.mapper

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import io.github.bubinimara.davibet.data.model.Tweet


/**
 *
 * Created by Davide Parise on 13/11/21.
 */
object TweetMapper {
    fun createTweet(jsonObject: JsonObject):Tweet{
        if(jsonObject.has("text")) {
            val text = jsonObject.get("text").asString
            return Tweet(text)
        }
        throw JsonParseException("The json object is not a Tweet: ${jsonObject.toString()}")
    }
}