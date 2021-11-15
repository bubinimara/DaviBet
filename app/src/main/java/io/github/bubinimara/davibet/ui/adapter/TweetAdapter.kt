package io.github.bubinimara.davibet.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.bubinimara.davibet.R
import io.github.bubinimara.davibet.data.model.Tweet
import kotlin.math.log

/**
 *
 * Created by Davide Parise on 14/11/21.
 */
class TweetAdapter: ListAdapter<Tweet, TweetAdapter.Holder>(TweetDiff()) {
    class Holder(view:View): RecyclerView.ViewHolder(view) {
        val text:TextView
        init {
            text = view.findViewById(R.id.tweet_text)
        }

        fun set(tweet: Tweet){
            text.text = tweet.text
        }
    }

    class TweetDiff:DiffUtil.ItemCallback<Tweet>(){
        override fun areItemsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem.tweetId == newItem.tweetId
        }

        override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem == newItem
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_tweet,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.set(getItem(position))
    }

    fun set(tweets: List<Tweet>) {
        submitList(tweets.toList())
    }
}