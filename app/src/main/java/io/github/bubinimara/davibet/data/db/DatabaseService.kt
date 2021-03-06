package io.github.bubinimara.davibet.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.bubinimara.davibet.data.model.Tweet


/**
 *
 * Created by Davide Parise
 * 
 */

@Database(entities = [Tweet::class], version = 1, exportSchema = false)
@AutoMigration(from = 0,to = 1)
abstract class DatabaseService: RoomDatabase() {
    abstract fun tweetDat():TweetDao

    companion object{
        private var INSTANCE: DatabaseService ?= null

        fun getDb(context: Context):DatabaseService{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, DatabaseService::class.java,"app_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return
                instance
            }
        }
    }
}