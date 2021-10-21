package com.location.reminder.sound.location

import android.app.NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.location.reminder.sound.ui.AddReminderActivity
import com.location.reminder.sound.ui.activity.HomeActivity
import com.location.reminder.sound.util.Constants
import com.location.reminder.sound.util.SharedPrefClient


class MyBroadcastReceiver : BroadcastReceiver() {

    private lateinit var sharedPrefClient: SharedPrefClient

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("Receiver", "onReceive: ")
        if (p1?.action == ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED) {
            val reminderIntent = Intent(p0, AddReminderActivity::class.java).apply {
                putExtra(Constants.IS_FROM_RECEIVER, true)
            }
            val homeIntent = Intent(p0, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            TaskStackBuilder.create(p0)
                .addNextIntent(homeIntent)
                .addNextIntent(reminderIntent)
                .startActivities()
        } else {
          //  sharedPrefClient = SharedPrefClient()
            p0?.let { context ->
                /*sharedPrefClient.init(p0)
                sharedPrefClient.clearData()*/
                val serviceIntent = Intent(p0, LocationUpdateService::class.java)
                context.stopService(serviceIntent)
                NotificationManagerCompat.from(context).cancelAll()
            }
        }
    }
}
