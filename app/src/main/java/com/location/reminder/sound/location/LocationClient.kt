package com.location.reminder.sound.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.location.reminder.sound.util.isPermissionGranted
import kotlinx.coroutines.*

class LocationClient constructor(
    private val context: Context,
    private val providerClient: FusedLocationProviderClient,
) {
    private val _viewAction = MutableLiveData<ViewAction>()
    fun getViewAction(): LiveData<ViewAction> = _viewAction

    private var updateTime: Int = 10
    lateinit var locationCallback: LocationCallback

    init {
        initLocationCallback()
    }

    private fun createLocationRequest(): LocationRequest {
        val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS
        ).build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
            context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            providerClient.requestLocationUpdates(
                createLocationRequest(), locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { _viewAction.value = ViewAction.OnLocationFetched(it) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        try {
            providerClient.lastLocation.apply {
                addOnFailureListener {
                    _viewAction.value = ViewAction.OnError(it)
                }
                addOnSuccessListener {
                    _viewAction.value = ViewAction.OnLocationFetched(it)
                }
            }
        } catch (unlikely: SecurityException) {
            _viewAction.value = ViewAction.OnError(unlikely)
        }
    }

    fun removeLocationUpdates() {
        providerClient.removeLocationUpdates(locationCallback)
    }
}

sealed class ViewAction(val location: Location? = null, val error: java.lang.Exception? = null) {
    class OnLocationFetched(location: Location) : ViewAction(location)
    class OnError(exception: java.lang.Exception) : ViewAction(error = exception)
}