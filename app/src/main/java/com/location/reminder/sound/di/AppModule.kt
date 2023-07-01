package com.location.reminder.sound.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.location.reminder.sound.BuildConfig
import com.location.reminder.sound.db.AppDatabase
import com.location.reminder.sound.db.TaskDao
import com.location.reminder.sound.repositories.HomeRepository
import com.location.reminder.sound.repositories.HomeRepositoryImpl
import com.location.reminder.sound.location.LocationClient
import com.location.reminder.sound.location.LocationClientImpl
import com.location.reminder.sound.network.PlacesApi
import com.location.reminder.sound.network.RetrofitUtil
import com.location.reminder.sound.util.SharedPrefClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

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
    fun getHomeRepository(
        sharedPrefClient: SharedPrefClient,
        database: TaskDao
    ): HomeRepository {
        return HomeRepositoryImpl(sharedPrefClient, database)
    }

    @Provides
    fun getLocationClient(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationClient {
        return LocationClientImpl(context, fusedLocationProviderClient)
    }

    @Provides
    fun getFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun getDataBase(@ApplicationContext context: Context): TaskDao {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "tasks"
        ).fallbackToDestructiveMigration().build().taskDao()

    }

    @Provides
    fun getRetrofit(): Retrofit {
        return RetrofitUtil.getInstance();
    }

    @Provides
    fun getPlacesApi(retrofit: Retrofit): PlacesApi {
        return retrofit.create(PlacesApi::class.java);
    }

    @Provides
    fun getPlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            val gApiKey: String = BuildConfig.API_KEY
            Places.initialize(context, gApiKey)
        }
        return Places.createClient(context)
    }

}