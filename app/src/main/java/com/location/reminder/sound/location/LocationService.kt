package com.location.reminder.sound.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.location.reminder.sound.R


class LocationService : Service() {

    private val TAG: String = "Service"
    private var mLocationCallback: LocationCallback? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    private var mLocation: Location? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //val data = intent?.getStringExtra("data")
        setLocationListener()
        startForeground(1001, getNotificationBuilder("").build())
        return START_NOT_STICKY
    }

    private fun setLocationListener() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {

            mFusedLocationClient?.lastLocation?.apply {
                addOnFailureListener {
                    //Handle the failure of the call. You can show an error dialogue or a toast stating the failure in receiving location.
                }
                addOnSuccessListener {
                    //Got last known location. (Can be null sometimes)

                    //You ca extract the details from the client, like the latitude and longitude of the place and use it accordingly.
                    Log.e(
                        TAG, "setLocationListener: myLat" + it?.latitude +
                                "long" + it?.longitude
                    )
                }
            }
            mFusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    mLocation = task.result
                    Log.e(
                        TAG, "setLocationListener: lat" + mLocation?.latitude +
                                "long" + mLocation?.longitude
                    )
                    // mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
                } else {
                    // error
                }
            }
        } catch (unlikely: SecurityException) {
            // catch exception
        }

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult
                Log.e(
                    TAG,
                    "onLocationResult: lastlocation ${locationResult.lastLocation?.latitude}, ${locationResult.lastLocation?.longitude}"
                )

                val distance = distance(
                    28.604077722097507, 77.05684771633163,// my home location
                    28.589057357822966, 77.08295730673515
                )
                Log.e(
                    "Service", "onCreate: $distance"
                )
                if (distance < 10) {
                    Log.e(
                        "Service", "show notification reached location: "
                    )
                }

                for (location in locationResult.locations) {
                    // Update UI with location data
                    // ...
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }
        }

        //val locationRequest = createLocationRequest()

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000 //60000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Log.e(TAG, "setLocationListener: ")
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
        /*try {
            mFusedLocationClient?.requestLocationUpdates(
                locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        for (location in locationResult.locations) {
                            Log.e(
                                "Service",
                                "onLocationResult: latttttt" + location.latitude.toString()
                            )
                            updateNotification(location.latitude.toString())
                        }
                        // Few more things we can do here:
                        // For example: Update the location of user on server
                    }
                },
                Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            //Utils.setRequestingLocationUpdates(this, false);
        }*/


    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient?.requestLocationUpdates(
            locationRequest,
            mLocationCallback!!,
            Looper.getMainLooper()
        )

    }

    private fun createLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return mLocationRequest
    }

    fun updateNotification(lat: String) {
        val notification: Notification = getNotificationBuilder(lat).build()
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1001, notification)
    }

    private fun getNotificationBuilder(lat: String): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val name: CharSequence = "Background-Location"
            val description = "Background-Location"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel: NotificationChannel =
                NotificationChannel("Background-Location", name, importance)
            channel.description = description
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, "Background-Location")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Background Location Notification")
                .setContentText("this is my notification$lat")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true)


        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        // notificationManagerCompat.notify(1001, builder.build())

        return builder
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    private fun distance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius = 3958.75 // in miles, change to 6371 for kilometer output
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(
            Math.toRadians(
                lat2
            )
        ))
        val c =
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c // output distance, in MILES
    }
}