package com.location.reminder.sound.di

import android.content.Context
import com.location.reminder.sound.finalCode.repositories.HomeRepository
import com.location.reminder.sound.finalCode.repositories.HomeRepositoryImpl
import com.location.reminder.sound.util.SharedPrefClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideBaseUrl() = "Hi"

    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPrefClient {
        return SharedPrefClient(context)
    }

    @Provides
    fun getHomeRepository(sharedPrefClient: SharedPrefClient): HomeRepository {
        return HomeRepositoryImpl(sharedPrefClient)
    }

}