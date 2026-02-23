package com.schedule.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.schedule.app.data.model.TodayTask

@Dao
interface TodayTaskDao {

    @Query("SELECT * FROM today_tasks WHERE date = :date ORDER BY priority DESC, createdAt ASC")
    fun getTasksByDate(date: String): LiveData<List<TodayTask>>

    @Query("SELECT * FROM today_tasks ORDER BY date DESC, priority DESC")
    fun getAllTasks(): LiveData<List<TodayTask>>

    @Query("SELECT * FROM today_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TodayTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodayTask): Long

    @Update
    suspend fun updateTask(task: TodayTask)

    @Delete
    suspend fun deleteTask(task: TodayTask)

    @Query("UPDATE today_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM today_tasks WHERE date = :date AND isCompleted = 1")
    fun getCompletedTaskCount(date: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM today_tasks WHERE date = :date")
    fun getTotalTaskCount(date: String): LiveData<Int>
}
