package com.schedule.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.schedule.app.data.model.Schedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleAlarm(context: Context, schedule: Schedule) {
        if (!schedule.alarmEnabled || schedule.startTime == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val date = LocalDate.parse(schedule.date)
        val time = LocalTime.parse(schedule.startTime)
        val alarmDateTime = LocalDateTime.of(date, time)
            .minusMinutes(schedule.alarmMinutesBefore.toLong())

        val alarmTimeMillis = alarmDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Don't schedule alarms in the past
        if (alarmTimeMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_TITLE, schedule.title)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_TIME, schedule.startTime)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTES_BEFORE, schedule.alarmMinutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
