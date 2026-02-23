package com.schedule.app.ui.schedule

import androidx.lifecycle.*
import com.schedule.app.data.model.Schedule
import com.schedule.app.data.repository.ScheduleRepository
import kotlinx.coroutines.launch

class ScheduleDetailViewModel(private val repository: ScheduleRepository) : ViewModel() {

    private val _saveComplete = MutableLiveData<Boolean>()
    val saveComplete: LiveData<Boolean> = _saveComplete

    fun saveSchedule(schedule: Schedule, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val savedId = if (schedule.id == 0L) {
                repository.insertSchedule(schedule)
            } else {
                repository.updateSchedule(schedule)
                schedule.id
            }
            onSaved(savedId)
            _saveComplete.value = true
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}

class ScheduleDetailViewModelFactory(private val repository: ScheduleRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
