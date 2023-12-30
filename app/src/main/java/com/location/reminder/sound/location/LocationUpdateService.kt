package com.location.reminder.sound.location

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.location.reminder.sound.R
import com.location.reminder.sound.model.SoundMode
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.util.checkSoundMode
import com.location.reminder.sound.util.distanceBetweenTwoPoints
import com.location.reminder.sound.util.evaluateDistance
import com.location.reminder.sound.util.toggleSoundMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationUpdateService : LifecycleService() {

    companion object {
        const val TAG: String = "LocationUpdateService"
    }

    var context: Context = this

    @Inject
    lateinit var locationClient: LocationClient

    private var distanceBetweenTwoPoints = 0.0
    private var tasks: ArrayList<Task>? = null
    private var updateTime: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotification()
        getTasksFromIntent(intent)
        initLocationClient()
        return START_REDELIVER_INTENT
    }

    private fun getTasksFromIntent(intent: Intent?) {
        updateTime = intent?.getIntExtra("updateTime", 15) ?: 15
        tasks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getSerializableExtra("tasks", ArrayList::class.java) as ArrayList<Task>
        } else {
            intent?.getSerializableExtra("tasks") as ArrayList<Task>
        }
    }

    private fun initLocationClient() {
        locationClient.startLocationUpdates(updateTime)
        locationClient.getViewAction().observe(this) { action ->
            when (action) {
                is ViewAction.OnLocationFetched -> {
                    tasks?.forEach {
                        distanceBetweenTwoPoints = distanceBetweenTwoPoints(
                            action.location?.latitude ?: 0.0, action.location?.longitude ?: 0.0,
                            it.latitude, it.longitude
                        )
                        Log.e(TAG, "distanceBetweenTwoPoints: $distanceBetweenTwoPoints")
                         if (distanceBetweenTwoPoints <= it.distance) {
                            context.toggleSoundMode(it.destinationSoundMode ?: SoundMode.RINGER)
                            showLocationReachedNotification(it)
                            Log.e(TAG, "onLocationFetched: $distanceBetweenTwoPoints")
                        } else {
                            context.toggleSoundMode(it.sourceSoundMode)
                            showDistanceLeftNotification(it)
                            Log.e(TAG, "onLocationFetched: else")
                        }

                    }
                    action.location?.latitude
                }

                is ViewAction.OnError -> {
                    Log.e(TAG, "onStartCommand: ViewAction.OnError")

                }

                else -> {
                    Log.e(TAG, "onStartCommand: ViewAction.else")
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun showLocationReachedNotification(task: Task) {
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

        val soundMessage =
            "The sound mode has been changed from" +
                    " ${this.checkSoundMode()} " +
                    "to ${task.destinationSoundMode}."

        val content =
            "You have reached your destination - ${task.address}. $soundMessage"

        val builder = NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.drawable.icon_white)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(content)
            .setSilent(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))

            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(task.uid, builder.build())
    }

    @SuppressLint("MissingPermission")
    private fun showDistanceLeftNotification(task: Task) {
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

        val distanceToShow: String = distanceBetweenTwoPoints.evaluateDistance()

        val builder = NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.drawable.icon_white)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("You are $distanceToShow from your destination - ${task.address}.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You are $distanceToShow from your destination - ${task.address}.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)

        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(task.uid, builder.build())
    }

    private fun createNotification() {
        val builder = getNotificationBuilder()
        startForeground(1001, builder.build())
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
        val intent = Intent(this, NotificationChangeBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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
        locationClient.removeLocationUpdates()
    }

}