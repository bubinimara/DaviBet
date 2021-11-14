package io.github.bubinimara.davibet.data.mapper

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import io.github.bubinimara.davibet.data.model.Tweet


/**
 *
 * Created by Davide Parise on 13/11/21.
 */
object TweetCreator {
    fun createFromJson(jsonObject: JsonObject):Tweet{
        if(jsonObject.has("text")) {
            val text = jsonObject.get("text").asString
            // add all others fields ...
            return Tweet(text,System.currentTimeMillis())
        }
        // not all are tweet.
        throw JsonParseException("The json object is not a Tweet: ${jsonObject.toString()}")
    }
}