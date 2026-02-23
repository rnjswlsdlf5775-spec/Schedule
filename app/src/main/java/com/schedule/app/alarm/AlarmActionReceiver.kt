package com.schedule.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class AlarmActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.schedule.app.DISMISS_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISMISS -> {
                val scheduleId = intent.getLongExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, -1)
                if (scheduleId != -1L) {
                    NotificationManagerCompat.from(context).cancel(scheduleId.toInt())
                }
            }
        }
    }
}
