package com.schedule.app.ui.today

import androidx.lifecycle.*
import com.schedule.app.data.model.TodayTask
import com.schedule.app.data.repository.TodayTaskRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(private val repository: TodayTaskRepository) : ViewModel() {

    private val today = LocalDate.now().toString()

    val tasksForToday: LiveData<List<TodayTask>> = repository.getTasksByDate(today)
    val completedCount: LiveData<Int> = repository.getCompletedTaskCount(today)
    val totalCount: LiveData<Int> = repository.getTotalTaskCount(today)

    fun saveTask(task: TodayTask) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun toggleTaskCompletion(task: TodayTask) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TodayTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}

class TodayViewModelFactory(private val repository: TodayTaskRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
