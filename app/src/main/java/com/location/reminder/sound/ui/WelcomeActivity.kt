package com.location.reminder.sound.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.location.reminder.sound.R
import com.location.reminder.sound.util.SharedPrefClient
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    private lateinit var sharedPrefClient: SharedPrefClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(this)

        btnContinue.setOnClickListener {
            sharedPrefClient.updateWalkThroughDone(true)
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}