package com.location.reminder.sound.location

import android.app.NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.location.reminder.sound.ui.activities.HomeActivity


class NotificationChangeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("Receiver", "onReceive: ")
        if (p1?.action == ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED) {
            val homeIntent = Intent(p0, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            TaskStackBuilder.create(p0)
                .addNextIntent(homeIntent)
                .startActivities()
        } else {
            p0?.let { context ->
                val serviceIntent = Intent(p0, LocationUpdateService::class.java)
                context.stopService(serviceIntent)
                NotificationManagerCompat.from(context).cancelAll()
            }
        }
    }
}
