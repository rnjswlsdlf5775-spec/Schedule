package com.schedule.app.ui.calendar

import androidx.lifecycle.*
import com.schedule.app.data.model.Schedule
import com.schedule.app.data.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val repository: ScheduleRepository) : ViewModel() {

    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private var _schedulesForSelectedDate = MutableLiveData<List<Schedule>>()
    val schedulesForSelectedDate: LiveData<List<Schedule>> = _schedulesForSelectedDate

    private var _markedDates = MutableLiveData<List<String>>()
    val markedDates: LiveData<List<String>> = _markedDates

    private var currentObserver: Observer<List<Schedule>>? = null
    private var currentLiveData: LiveData<List<Schedule>>? = null

    fun loadSchedulesForDate(date: LocalDate) {
        _selectedDate.value = date
        currentObserver?.let { currentLiveData?.removeObserver(it) }

        val liveData = repository.getSchedulesByDate(date.toString())
        val observer = Observer<List<Schedule>> { schedules ->
            _schedulesForSelectedDate.value = schedules
        }
        currentLiveData = liveData
        currentObserver = observer
        liveData.observeForever(observer)

        loadMarkedDatesForMonth(YearMonth.from(date))
    }

    fun loadMarkedDatesForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            val startDate = yearMonth.atDay(1).toString()
            val endDate = yearMonth.atEndOfMonth().toString()
            val dates = repository.getDatesWithSchedules(startDate, endDate)
            _markedDates.value = dates
        }
    }

    fun toggleScheduleComplete(schedule: Schedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule.copy(isCompleted = !schedule.isCompleted))
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentObserver?.let { currentLiveData?.removeObserver(it) }
    }
}

class CalendarViewModelFactory(private val repository: ScheduleRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
