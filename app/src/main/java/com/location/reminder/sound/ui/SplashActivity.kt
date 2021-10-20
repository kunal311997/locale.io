package com.location.reminder.sound.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.location.reminder.sound.util.Constants
import com.location.reminder.sound.R
import com.location.reminder.sound.util.SharedPrefClient
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToLong

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPrefClient: SharedPrefClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPrefClient.isWalkThroughDone()) openHomeActivity()
            else openWelcomeActivity()
        }, Constants.SPLASH_SCREEN_TIMEOUT)
    }

    private fun openWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun openHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

// use hilt
// use data store
