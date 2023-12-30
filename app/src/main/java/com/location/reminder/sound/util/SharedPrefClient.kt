package com.location.reminder.sound.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SharedPrefClient @Inject constructor(val context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TCPPreference", Context.MODE_PRIVATE)

    fun clear() {
        sharedPreferences.edit(commit = true) {
            this.clear().apply()
        }
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


    private val KEY_UPDATE_TIME = "updateTime"

    fun getUpdateTime(): Int {
        return sharedPreferences.getInt(KEY_UPDATE_TIME, 15)
    }

    fun setUpdateTime(updateTime: Int) {
        sharedPreferences.edit(commit = true) {
            putInt(KEY_UPDATE_TIME, updateTime)
        }
    }

}