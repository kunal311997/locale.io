package com.location.reminder.sound.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.location.reminder.sound.location.LocationService
import com.location.reminder.sound.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartService = findViewById(R.id.buttonStartService)
        btnStopService = findViewById(R.id.buttonStopService)

        btnStartService.setOnClickListener {
            startService()
        }
        btnStopService.setOnClickListener {
            stopService()
        }

        Log.e(
            "Service", "onCreate: " + distance(
                28.604077722097507, 77.05684771633163,
                28.589057357822966, 77.08295730673515
            )
        )
        Log.e(
            "Service", "onCreate: " + SphericalUtil.computeDistanceBetween(
                LatLng(28.604077722097507, 77.05684771633163),
                LatLng(28.589057357822966, 77.08295730673515)
            )
        )


    }

    private fun startService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
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
// 28.604077722097507, 77.05684771633163
// 28.589057357822966, 77.08295730673515