package com.location.reminder.sound.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.location.reminder.sound.isPermissionGranted
import com.location.reminder.sound.model.LocationData

class LocationClientUtil(
    private val context: Context,
    private val updateTime: Int,
    private val listener: LocationClientUtilListener
) {

    private var lastLocation = LocationData()
    private var locationCallback: LocationCallback? = null
    private var locationClient: FusedLocationProviderClient? = null

    init {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        initLocationCallback()
        fetchLastLocation()

        val locationRequest = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
        }


        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    // exception.startResolutionForResult(this, 100)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        startLocationUpdates(locationRequest)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = (updateTime * 1000).toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        if (context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
            context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            locationClient?.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                lastLocation.latitude = locationResult.lastLocation.latitude
                lastLocation.longitude = locationResult.lastLocation.longitude
                listener.onLocationFetched(lastLocation)
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        try {
            locationClient?.lastLocation?.apply {
                addOnFailureListener {
                    listener.onError()
                    //Handle the failure of the call. You can show an error dialogue or a toast stating the failure in receiving location.
                }
                addOnSuccessListener {
                    lastLocation.latitude = it?.latitude
                    lastLocation.longitude = it?.longitude
                    listener.onLocationFetched(lastLocation)
                }
            }
        } catch (unlikely: SecurityException) {
            // catch exception
        }
    }

    fun removeLocationUpdates() {
        locationClient?.removeLocationUpdates(locationCallback)
    }

    interface LocationClientUtilListener {
        fun onLocationFetched(location: LocationData)
        fun onError()
    }
}