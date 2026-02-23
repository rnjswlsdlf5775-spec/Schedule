package com.schedule.app.data.repository

import androidx.lifecycle.LiveData
import com.schedule.app.data.db.TodayTaskDao
import com.schedule.app.data.model.TodayTask

class TodayTaskRepository(private val taskDao: TodayTaskDao) {

    fun getTasksByDate(date: String): LiveData<List<TodayTask>> = taskDao.getTasksByDate(date)

    fun getAllTasks(): LiveData<List<TodayTask>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TodayTask? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TodayTask): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TodayTask) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TodayTask) = taskDao.deleteTask(task)

    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) =
        taskDao.updateTaskCompletion(id, isCompleted)

    fun getCompletedTaskCount(date: String): LiveData<Int> = taskDao.getCompletedTaskCount(date)

    fun getTotalTaskCount(date: String): LiveData<Int> = taskDao.getTotalTaskCount(date)
}
