package com.location.reminder.sound.util

import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Geocoder
import android.media.AudioManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.location.reminder.sound.R
import com.location.reminder.sound.location.LocationUpdateService
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


fun Context.getCompleteAddressString(latitude: Double, longitude: Double): String {
    var strAdd = ""
    val geocoder = Geocoder(this, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            val returnedAddress = addresses[0]
            val strReturnedAddress = StringBuilder()
            for (i in 0..returnedAddress.maxAddressLineIndex) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
            }
            strAdd = strReturnedAddress.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return strAdd
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun distanceBetweenTwoPoints(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Double {
    val earthRadius = 3958.75 // in miles, change to 6371 for kilometer output
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val sindLat = sin(dLat / 2)
    val sindLng = sin(dLng / 2)
    val a =
        sindLat.pow(2.0) + (sindLng.pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return milesToMeters(earthRadius * c)// output distance, in MILES
}

fun milesToMeters(miles: Double): Double {
    return miles / 0.0006213711
}

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

fun Context.toggleSoundMode(soundMode: String) {
    val newSoundMode = when (soundMode) {
        resources.getString(R.string.sound) -> AudioManager.RINGER_MODE_NORMAL
        resources.getString(R.string.vibrate) -> AudioManager.RINGER_MODE_VIBRATE
        resources.getString(R.string.mute) -> AudioManager.RINGER_MODE_SILENT
        else -> 0
    }
    val audioManager: AudioManager =
        this.getSystemService(Service.AUDIO_SERVICE) as AudioManager
    audioManager.ringerMode = newSoundMode
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Context.startLocationUpdateService() {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    ContextCompat.startForegroundService(this, serviceIntent)
}

fun Context.stopLocationUpdateService() {
    val serviceIntent = Intent(this, LocationUpdateService::class.java)
    stopService(serviceIntent)
}

fun Context.checkSoundMode(): Pair<String, Int> {
    val profileCheck = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    return when (profileCheck?.ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> Pair(resources.getString(R.string.sound), 0)
        AudioManager.RINGER_MODE_VIBRATE -> Pair(resources.getString(R.string.vibrate), 1)
        AudioManager.RINGER_MODE_SILENT -> Pair(resources.getString(R.string.mute), 2)
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

fun Context.isNotificationAccessGranted(): Boolean {
    val n =
        applicationContext.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    return n.isNotificationPolicyAccessGranted
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm EEEE", Locale.getDefault())
    return sdf.format(Date())
}

fun toBounds(center: LatLng?): LatLngBounds {
    val radiusInMeters = Constants.RADIUS_IN_METERS
    val distanceFromCenterToCorner = radiusInMeters * sqrt(Constants.SQRT_TWO)
    val southwestCorner =
        SphericalUtil.computeOffset(center, distanceFromCenterToCorner, Constants.SW_HEADING)
    val northeastCorner =
        SphericalUtil.computeOffset(center, distanceFromCenterToCorner, Constants.NE_HEADING)
    return LatLngBounds(southwestCorner, northeastCorner)
}

fun roundOffDouble2Places(num: Double): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(num)
}