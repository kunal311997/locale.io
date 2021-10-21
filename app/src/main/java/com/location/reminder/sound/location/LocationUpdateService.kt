package com.location.reminder.sound.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.location.reminder.sound.R
import com.location.reminder.sound.util.distanceBetweenTwoPoints
import com.location.reminder.sound.model.LocationData
import com.location.reminder.sound.util.roundOffDouble2Places
import com.location.reminder.sound.util.toggleSoundMode
import com.location.reminder.sound.util.SharedPrefClient

class LocationUpdateService : Service() {

    var context: Context = this
    val TAG = "LocationUpdateService"
    lateinit var locationClientUtil: LocationClientUtil
    private lateinit var sharedPrefClient: SharedPrefClient
    var distanceBetweenTwoPoints = 0.0


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initSharedPrefClient()
        createNotification()
        initLocationClient()
        return START_NOT_STICKY
    }

    private fun initSharedPrefClient() {
       /* sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(this)*/
    }

    private fun initLocationClient() {
        locationClientUtil =
            LocationClientUtil(
                this, sharedPrefClient.getUpdateTime(),
                object : LocationClientUtil.LocationClientUtilListener {
                    override fun onLocationFetched(location: LocationData) {

                        Log.e(TAG, "onLocationFetched: " + location.latitude + location.longitude)
                        val savedLatitude = sharedPrefClient.getLatitude()
                        val savedLongitude = sharedPrefClient.getLongitude()

                        Log.e(TAG, "savedLocation $savedLatitude , $savedLongitude")
                        distanceBetweenTwoPoints = distanceBetweenTwoPoints(
                            location.latitude ?: 0.0, location.longitude ?: 0.0,
                            savedLatitude, savedLongitude
                        )
                        val soundMode = sharedPrefClient.getSoundMode()
                        val lastSoundMode = sharedPrefClient.getLastSoundMode()

                        Log.e(TAG, "onLocationFetched: $soundMode$lastSoundMode")
                        if (distanceBetweenTwoPoints < sharedPrefClient.getDistance()) {
                            context.toggleSoundMode(soundMode)
                            showLocationReachedNotification()
                            Log.e(TAG, "onLocationFetched: $distanceBetweenTwoPoints")
                        } else {
                            context.toggleSoundMode(lastSoundMode)
                            showDistanceLeftNotification()
                            Log.e(TAG, "onLocationFetched: else")
                        }
                    }

                    override fun onError() {
                        Log.e(TAG, "onError: ")
                    }
                })
    }


    private fun showLocationReachedNotification() {
        val id = "Completed"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val name: CharSequence = id
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = id
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val content = "You have reached your destination. The sound mode has been changed from" +
                " ${sharedPrefClient.getLastSoundMode()} " +
                "to ${sharedPrefClient.getSoundMode()}."

        val builder = NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.drawable.icon_white)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(1002, builder.build())
    }

    private fun showDistanceLeftNotification() {
        val id = "DistanceLeft"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val name: CharSequence = id
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = id
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val distance = distanceBetweenTwoPoints.toInt()
        val distanceToShow: String = if (distance < 1000) {
            "$distance metres"
        } else {
            "${roundOffDouble2Places(distanceBetweenTwoPoints / 1000)} kms"
        }

        val builder = NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.drawable.icon_white)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("You are $distanceToShow from your destination.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You are $distanceToShow from your destination.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)

        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(1002, builder.build())
    }

    private fun createNotification() {
        val builder = getNotificationBuilder()
        startForeground(1001, builder.build())
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val id = "Started"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val name: CharSequence = id
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = id
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MyBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1001, intent, 0)

        val content =
            if (distanceBetweenTwoPoints != 0.0) "This app is continuously tracking your location." +
                    "You are $distanceBetweenTwoPoints metres from your destination."
            else "This app is continuously tracking your location."

        return NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.drawable.icon_white)
            .setContentTitle(resources.getString(R.string.app_name) + " is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText("This app is continuously tracking your location.")
            .addAction(R.drawable.icon_add, "Turn Off", pendingIntent)
            .setSilent(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClientUtil.removeLocationUpdates()
    }

}