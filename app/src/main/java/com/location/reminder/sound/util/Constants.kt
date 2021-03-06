package com.location.reminder.sound.util

object Constants {
    const val SPLASH_SCREEN_TIMEOUT: Long = 3000

    const val LOCATION_UPDATE_INTERVAL_IN_MILLIS: Long = 10000
    const val LOCATION_PERMISSIONS_REQUEST_CODE = 100
    const val RADIUS_IN_METERS = 5.0 * 1000.0 // 1 KM = 1000 Meter
    const val SW_HEADING = 225.0
    const val NE_HEADING = 45.0
    const val SQRT_TWO = 2.0

    const val GOOGLE_DETAIL_URL = "https://maps.googleapis.com/maps/api/place/details/json"
    
    const val IS_FROM_RECEIVER = "isFromReceiver"
    const val IS_FROM_EDIT= "isFromEdit"

}