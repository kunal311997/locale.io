package com.location.reminder.sound.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SharedPrefClient @Inject constructor(val context: Context) {

    var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TCPPreference", Context.MODE_PRIVATE)

    fun clearData() {
        sharedPreferences.edit(commit = true) {
            this.clear().apply()
        }
    }

    fun clearLocationData() {
        setAddress("")
        setSoundMode("")
        setCreatedAt("")
        setLatitude(0.0)
        setLongitude(0.0)
    }

    private val WALK_THROUGH_DONE = "walk_through_done"

    fun completeWalkThrough() {
        sharedPreferences.edit(commit = true) {
            putBoolean(WALK_THROUGH_DONE, true)
        }
    }

    fun isWalkThroughCompleted(): Boolean {
        return sharedPreferences.getBoolean(WALK_THROUGH_DONE, false)
    }

    private val KEY_ADDRESS = "address"

    fun getAddress(): String {
        return sharedPreferences.getString(KEY_ADDRESS, "") ?: ""
    }

    fun setAddress(address: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_ADDRESS, address)
        }
    }

    private val KEY_CREATED_AT = "createdAt"

    fun getCreatedAt(): String {
        return sharedPreferences.getString(KEY_CREATED_AT, "") ?: ""
    }

    fun setCreatedAt(createdAt: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_CREATED_AT, createdAt)
        }
    }

    private val KEY_SOUND_MODE = "soundMode"

    fun getSoundMode(): String {
        return sharedPreferences.getString(KEY_SOUND_MODE, "") ?: ""
    }

    fun setSoundMode(soundMode: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_SOUND_MODE, soundMode)
        }
    }

    private val KEY_LATITUDE = "latitude"

    fun getLatitude(): Double {
        return java.lang.Double.longBitsToDouble(
            sharedPreferences.getLong(
                KEY_LATITUDE,
                java.lang.Double.doubleToLongBits(0.0)
            )
        )
    }

    fun setLatitude(latitude: Double) {
        sharedPreferences.edit(commit = true) {
            putLong(KEY_LATITUDE, java.lang.Double.doubleToRawLongBits(latitude))
        }
    }

    private val KEY_LONGITUDE = "longitude"

    fun getLongitude(): Double {
        return java.lang.Double.longBitsToDouble(
            sharedPreferences.getLong(
                KEY_LONGITUDE,
                java.lang.Double.doubleToLongBits(0.0)
            )
        )
    }

    fun setLongitude(longitude: Double) {
        sharedPreferences.edit(commit = true) {
            putLong(KEY_LONGITUDE, java.lang.Double.doubleToRawLongBits(longitude))
        }
    }

    private val KEY_LAST_SOUND_MODE = "lastSoundMode"

    fun getLastSoundMode(): String {
        return sharedPreferences.getString(KEY_LAST_SOUND_MODE, "") ?: ""
    }

    fun setLastSoundMode(lastSoundMode: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_LAST_SOUND_MODE, lastSoundMode)
        }
    }

    private val KEY_DISTANCE = "distance"

    fun getDistance(): Int {
        return sharedPreferences.getInt(KEY_DISTANCE, 0)
    }

    fun setDistance(distance: Int) {
        sharedPreferences.edit(commit = true) {
            putInt(KEY_DISTANCE, distance)
        }
    }

    private val KEY_UPDATE_TIME = "updateTime"

    fun getUpdateTime(): Int {
        return sharedPreferences.getInt(KEY_UPDATE_TIME, 0)
    }

    fun setUpdateTime(updateTime: Int) {
        sharedPreferences.edit(commit = true) {
            putInt(KEY_UPDATE_TIME, updateTime)
        }
    }

    private val KEY_SERVICE_RUNNING = "isServiceRunning"

    fun isServiceRunning(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
    }

    fun setServiceRunning(isServiceRunning: Boolean) {
        sharedPreferences.edit(commit = true) {
            putBoolean(KEY_SERVICE_RUNNING, isServiceRunning)
        }
    }

}