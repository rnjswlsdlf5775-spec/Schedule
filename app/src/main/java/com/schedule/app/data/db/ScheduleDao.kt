package com.schedule.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.schedule.app.data.model.Schedule

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY date ASC, startTime ASC")
    fun getAllSchedules(): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE date = :date ORDER BY startTime ASC")
    fun getSchedulesByDate(date: String): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTime ASC")
    fun getSchedulesBetweenDates(startDate: String, endDate: String): LiveData<List<Schedule>>

    @Query("SELECT DISTINCT date FROM schedules WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getDatesWithSchedules(startDate: String, endDate: String): List<String>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("SELECT * FROM schedules WHERE alarmEnabled = 1 AND date >= :today ORDER BY date ASC, startTime ASC")
    suspend fun getSchedulesWithAlarm(today: String): List<Schedule>
}
