package com.location.reminder.sound.util

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.location.reminder.sound.location.NotificationChangeBroadcastReceiver
import com.location.reminder.sound.ui.CustomDialog

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isLocationPermissionsGranted(): Boolean {
    return isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
}


fun Context.isNotificationAccessGranted(): Boolean =
    (applicationContext.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted

fun Context.showNotificationAccessPermissionDialog() {
    CustomDialog(
        this,
        title = "Permissions Required",
        subTitle = "To change sound mode, you need to grant access to DO NOT DISTURB permission\n\n Press OK to go to settings page."
    ) {
        this.openDoNotDisturbSettings()
    }.show()
}

fun Context.showConfirmationDialog(soundMode: String, location: Int?, callback: () -> Unit) {
    CustomDialog(
        this,
        title = "Confirmation",
        subTitle = "Your device is currently at ${this.checkSoundMode().first} Mode. The sound mode will change to $soundMode Mode when you are at location - $location and it will change back to ${this.checkSoundMode().first} when you are away from selected location.",
        primaryActionTitle = "Confirm",
    ) {
        callback.invoke()
    }.show()
}

private fun Context.openDoNotDisturbSettings() {
    val notificationChangeReceiver = NotificationChangeBroadcastReceiver()
    this.registerReceiver(
        notificationChangeReceiver, IntentFilter(
            NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
        )
    )
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    startActivity(intent)
}

fun Context.showLocationPermissionDialog(callback: () -> Unit) {
    CustomDialog(
        this,
        title = "Permissions Required",
        subTitle = "This feature requires location in the background. The user will be notified on based on the current updated location.\n\n Do you want to allow location permissions ?"
    ) {
        callback()
    }.show()
}
