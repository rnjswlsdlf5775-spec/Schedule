package com.schedule.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.schedule.app.R
import com.schedule.app.ScheduleApplication
import com.schedule.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_SCHEDULE_TITLE = "schedule_title"
        const val EXTRA_SCHEDULE_TIME = "schedule_time"
        const val EXTRA_ALARM_MINUTES_BEFORE = "alarm_minutes_before"
        const val CHANNEL_ID = "schedule_alarm_channel"
        const val CHANNEL_NAME = "일정 알람"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
        val scheduleTitle = intent.getStringExtra(EXTRA_SCHEDULE_TITLE) ?: "일정 알람"
        val scheduleTime = intent.getStringExtra(EXTRA_SCHEDULE_TIME) ?: ""
        val minutesBefore = intent.getIntExtra(EXTRA_ALARM_MINUTES_BEFORE, 0)

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarms(context)
            return
        }

        val notificationText = when {
            minutesBefore == 0 -> "지금 시작합니다 - $scheduleTime"
            minutesBefore < 60 -> "${minutesBefore}분 후 시작 - $scheduleTime"
            else -> "1시간 후 시작 - $scheduleTime"
        }

        createNotificationChannel(context)
        showNotification(context, scheduleId, scheduleTitle, notificationText)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "일정 시작 전 알람 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        context: Context,
        scheduleId: Long,
        title: String,
        text: String
    ) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt() + 10000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check, "확인", dismissPendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(scheduleId.toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val app = context.applicationContext as ScheduleApplication
        CoroutineScope(Dispatchers.IO).launch {
            val today = java.time.LocalDate.now().toString()
            val schedules = app.scheduleRepository.getSchedulesWithAlarm(today)
            schedules.forEach { schedule ->
                AlarmScheduler.scheduleAlarm(context, schedule)
            }
        }
    }
}
