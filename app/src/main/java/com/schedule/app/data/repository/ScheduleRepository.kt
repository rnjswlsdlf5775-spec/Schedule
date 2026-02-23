package com.schedule.app.data.repository

import androidx.lifecycle.LiveData
import com.schedule.app.data.db.ScheduleDao
import com.schedule.app.data.model.Schedule

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    fun getAllSchedules(): LiveData<List<Schedule>> = scheduleDao.getAllSchedules()

    fun getSchedulesByDate(date: String): LiveData<List<Schedule>> =
        scheduleDao.getSchedulesByDate(date)

    fun getSchedulesBetweenDates(startDate: String, endDate: String): LiveData<List<Schedule>> =
        scheduleDao.getSchedulesBetweenDates(startDate, endDate)

    suspend fun getDatesWithSchedules(startDate: String, endDate: String): List<String> =
        scheduleDao.getDatesWithSchedules(startDate, endDate)

    suspend fun getScheduleById(id: Long): Schedule? = scheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: Schedule): Long = scheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: Schedule) = scheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: Schedule) = scheduleDao.deleteSchedule(schedule)

    suspend fun deleteScheduleById(id: Long) = scheduleDao.deleteScheduleById(id)

    suspend fun getSchedulesWithAlarm(today: String): List<Schedule> =
        scheduleDao.getSchedulesWithAlarm(today)
}
