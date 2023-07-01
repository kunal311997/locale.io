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


interface LocationClient {
    fun fetchLastLocation()
    fun startLocationUpdates(updateTime: Int)
    fun removeLocationUpdates()
    fun getViewAction(): LiveData<ViewAction>
}

class LocationClientImpl constructor(
    private val context: Context,
    private val providerClient: FusedLocationProviderClient,
) : LocationClient {
    private val _viewAction = MutableLiveData<ViewAction>()
    override fun getViewAction(): LiveData<ViewAction> = _viewAction

    private lateinit var locationCallback: LocationCallback


    init {
        initLocationCallback()
    }

    private fun getLocationRequest(updateTime: Int) = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, (updateTime * 1000).toLong()
    ).build()

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(updateTime: Int) {
        if (context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && context.isPermissionGranted(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            providerClient.requestLocationUpdates(
                getLocationRequest(updateTime), locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    _viewAction.value = ViewAction.OnLocationFetched(it)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun fetchLastLocation() {
        try {
            providerClient.lastLocation.apply {
                addOnFailureListener {
                    _viewAction.value = ViewAction.OnError(it)
                }
                addOnSuccessListener {
                    if (it != null) _viewAction.value = ViewAction.OnLocationFetched(it)
                }
            }
        } catch (unlikely: SecurityException) {
            _viewAction.value = ViewAction.OnError(unlikely)
        }
    }

    override fun removeLocationUpdates() {
        providerClient.removeLocationUpdates(locationCallback)
    }
}

sealed class ViewAction(val location: Location? = null, val error: java.lang.Exception? = null) {
    class OnLocationFetched(location: Location) : ViewAction(location)
    class OnError(exception: java.lang.Exception) : ViewAction(error = exception)
}