package com.schedule.app

import android.app.Application
import com.schedule.app.data.db.AppDatabase
import com.schedule.app.data.repository.ScheduleRepository
import com.schedule.app.data.repository.TodayTaskRepository

class ScheduleApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    val scheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val taskRepository by lazy { TodayTaskRepository(database.todayTaskDao()) }
}
