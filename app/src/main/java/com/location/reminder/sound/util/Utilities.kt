package com.location.reminder.sound.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.location.reminder.sound.location.LocationUpdateService
import com.location.reminder.sound.model.SoundMode
import com.location.reminder.sound.model.Task

fun Context.toggleSoundMode(soundMode: SoundMode) {
    val newSoundMode = when (soundMode) {
        SoundMode.RINGER -> AudioManager.RINGER_MODE_NORMAL
        SoundMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
        SoundMode.SILENT -> AudioManager.RINGER_MODE_SILENT
    }
    try {
        (this.getSystemService(Service.AUDIO_SERVICE) as AudioManager).ringerMode = newSoundMode
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.startLocationUpdateService(addedTasks: ArrayList<Task>?, updateTime: Int) {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    serviceIntent.putExtra("tasks", addedTasks)
    serviceIntent.putExtra("updateTime", updateTime)
    ContextCompat.startForegroundService(this, serviceIntent)
}

fun Context.stopLocationUpdateService() {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    stopService(serviceIntent)
}

fun Context.checkSoundMode(): SoundMode {
    val profileCheck = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    return when (profileCheck?.ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> SoundMode.RINGER
        AudioManager.RINGER_MODE_VIBRATE -> SoundMode.VIBRATE
        AudioManager.RINGER_MODE_SILENT -> SoundMode.SILENT
        else -> SoundMode.RINGER
    }
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
