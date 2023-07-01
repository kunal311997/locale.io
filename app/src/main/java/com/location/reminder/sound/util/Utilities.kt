package com.location.reminder.sound.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.location.reminder.sound.R
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.location.LocationUpdateService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun Context.toggleSoundMode(soundMode: String) {
    val newSoundMode = when (soundMode) {
        resources.getString(R.string.ring) -> AudioManager.RINGER_MODE_NORMAL
        resources.getString(R.string.vibrate) -> AudioManager.RINGER_MODE_VIBRATE
        resources.getString(R.string.silent) -> AudioManager.RINGER_MODE_SILENT
        else -> 0
    }
    val audioManager: AudioManager =
        this.getSystemService(Service.AUDIO_SERVICE) as AudioManager
    audioManager.ringerMode = newSoundMode
}


fun Context.startLocationUpdateService(addedTasks: ArrayList<Task>?) {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    serviceIntent.putExtra("tasks", addedTasks)
    ContextCompat.startForegroundService(this, serviceIntent)
}

fun Context.stopLocationUpdateService() {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    stopService(serviceIntent)
}

fun Context.checkSoundMode(): Pair<String, Int> {
    val profileCheck = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    return when (profileCheck?.ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> Pair(resources.getString(R.string.ring), 0)
        AudioManager.RINGER_MODE_VIBRATE -> Pair(resources.getString(R.string.vibrate), 1)
        AudioManager.RINGER_MODE_SILENT -> Pair(resources.getString(R.string.silent), 2)
        else -> Pair("", 0)
    }
}

fun setTextViewDrawableColor(textView: TextView, color: Int) {
    for (drawable in textView.compoundDrawables) {
        if (drawable != null) {
            drawable.colorFilter =
                PorterDuffColorFilter(
                    ContextCompat.getColor(textView.context, color),
                    PorterDuff.Mode.SRC_IN
                )
        }
    }
}


fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm EEEE", Locale.getDefault())
    return sdf.format(Date())
}

fun Double.evaluateDistance(): String {
    val distance = this.toInt()
    val distanceToShow: String = if (distance < 1000) {
        "$distance metres"
    } else {
        "${roundOffDouble2Places(this / 1000)} kms"
    }
    return distanceToShow
}
