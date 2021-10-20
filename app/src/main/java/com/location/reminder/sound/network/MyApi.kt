package com.location.reminder.sound.network

import com.location.reminder.sound.model.PlacesDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MyApi {

    @GET
    suspend fun placesDetailAPI(
        @Url url: String,
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): PlacesDetailsResponse
}