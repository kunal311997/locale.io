package com.location.reminder.sound.util

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.SphericalUtil
import com.location.reminder.sound.BuildConfig
import com.location.reminder.sound.model.Location
import com.location.reminder.sound.model.PlacesDetailsResponse
import com.location.reminder.sound.network.PlacesApi
import com.location.reminder.sound.ui.fragments.CreateTaskFragmentDialog
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.*

fun toBounds(center: LatLng?): LatLngBounds {
    val radiusInMeters = Constants.RADIUS_IN_METERS
    val distanceFromCenterToCorner = radiusInMeters * sqrt(Constants.SQRT_TWO)
    val southwestCorner =
        SphericalUtil.computeOffset(center, distanceFromCenterToCorner, Constants.SW_HEADING)
    val northeastCorner =
        SphericalUtil.computeOffset(center, distanceFromCenterToCorner, Constants.NE_HEADING)
    return LatLngBounds(southwestCorner, northeastCorner)
}

fun roundOffDouble2Places(num: Double): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(num)
}

fun Context.getCompleteAddressString(latitude: Double, longitude: Double): String {
    var strAdd = ""
    val geocoder = Geocoder(this, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            val returnedAddress = addresses[0]
            val strReturnedAddress = StringBuilder()
            for (i in 0..returnedAddress.maxAddressLineIndex) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
            }
            strAdd = strReturnedAddress.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return strAdd
}

fun distanceBetweenTwoPoints(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Double {
    val earthRadius = 3958.75 // in miles, change to 6371 for kilometer output
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val sindLat = sin(dLat / 2)
    val sindLng = sin(dLng / 2)
    val a =
        sindLat.pow(2.0) + (sindLng.pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return milesToMeters(earthRadius * c)// output distance, in MILES
}

fun milesToMeters(miles: Double): Double {
    return miles / 0.0006213711
}


fun PlacesClient.fetchPlaces(
    searchText: String,
    location: Location?,
    callback: (FindAutocompletePredictionsResponse) -> Unit
) {
    val latLng = LatLng(location?.lat ?: 0.0, location?.lng ?: 0.0)
    val token = AutocompleteSessionToken.newInstance()
    val latLngBounds = toBounds(latLng)
    val bounds = RectangularBounds.newInstance(latLngBounds)
    val request = FindAutocompletePredictionsRequest.builder()
        .setSessionToken(token)
        .setOrigin(latLng)
        .setLocationBias(bounds)
        .setQuery(searchText)
        .build()

    this.findAutocompletePredictions(request)
        .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
            Log.i(
                "", "number of results in search places response"
                        + response.autocompletePredictions.size
            )
            callback.invoke(response)
        }
        .addOnFailureListener { exception: Exception -> exception.printStackTrace() }
}

suspend fun PlacesApi.callGetPlaceDetailsApi(placeId: String?): Location? {
    var location: Location? = null
    try {
        val response: PlacesDetailsResponse = this@callGetPlaceDetailsApi.placesDetailAPI(
            Constants.GOOGLE_DETAIL_URL, placeId ?: "", BuildConfig.API_KEY
        )
        Log.e(CreateTaskFragmentDialog.TAG, "getPlaceDetails: $response")
        location = response.result?.geometry?.location
    } catch (e: java.lang.Exception) {
        Log.e(CreateTaskFragmentDialog.TAG, "getPlaceDetails: $e")
    }
    return location
}