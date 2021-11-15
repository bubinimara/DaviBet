package io.github.bubinimara.davibet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.bubinimara.davibet.data.DataRepository
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.db.DatabaseService
import io.github.bubinimara.davibet.data.network.NetworkMonitor
import io.github.bubinimara.davibet.data.network.NetworkServices
import javax.inject.Singleton


/**
 *
 * Created by Davide Parise on 14/11/21.
 */
@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideDatabaseService(@ApplicationContext context:Context): DatabaseService {
        return DatabaseService.getDb(context)
    }

    @Singleton
    @Provides
    fun provideNetworkMonitor(@ApplicationContext context:Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Singleton
    @Provides
    fun provideNetworkService():NetworkServices{
        return NetworkServices()
    }

    @Singleton
    @Provides
    fun privideDataRepository(networkServices: NetworkServices,databaseService: DatabaseService):DataRepository{
        return DataRepositoryImpl(networkServices.apiService,databaseService.tweetDat())
    }
}