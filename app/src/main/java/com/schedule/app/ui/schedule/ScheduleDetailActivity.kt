package com.schedule.app.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.schedule.app.R
import com.schedule.app.ScheduleApplication
import com.schedule.app.alarm.AlarmScheduler
import com.schedule.app.data.model.CategoryColor
import com.schedule.app.data.model.Schedule
import com.schedule.app.databinding.ActivityScheduleDetailBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDetailBinding
    private val viewModel: ScheduleDetailViewModel by viewModels {
        ScheduleDetailViewModelFactory(
            (application as ScheduleApplication).scheduleRepository
        )
    }

    private var selectedDate: LocalDate = LocalDate.now()
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null
    private var existingSchedule: Schedule? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    companion object {
        const val EXTRA_SCHEDULE = "extra_schedule"
        const val EXTRA_DATE = "extra_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSpinners()
        loadIntentData()
        setupListeners()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupSpinners() {
        // Category color spinner
        val colorNames = CategoryColor.values().map { it.name }
        val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorNames)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerColor.adapter = colorAdapter

        // Alarm options
        val alarmOptions = arrayOf("알람 없음", "정시", "5분 전", "10분 전", "15분 전", "30분 전", "1시간 전")
        val alarmAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, alarmOptions)
        alarmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAlarm.adapter = alarmAdapter
    }

    private fun loadIntentData() {
        existingSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_SCHEDULE, Schedule::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_SCHEDULE)
        }
        val dateStr = intent.getStringExtra(EXTRA_DATE)

        if (existingSchedule != null) {
            // Edit mode
            supportActionBar?.title = "일정 수정"
            binding.btnDelete.visibility = View.VISIBLE
            fillFormWithSchedule(existingSchedule!!)
        } else {
            // Add mode
            supportActionBar?.title = "일정 추가"
            binding.btnDelete.visibility = View.GONE
            dateStr?.let {
                selectedDate = LocalDate.parse(it)
            }
            updateDateDisplay()
        }
    }

    private fun fillFormWithSchedule(schedule: Schedule) {
        binding.etTitle.setText(schedule.title)
        binding.etDescription.setText(schedule.description)
        selectedDate = LocalDate.parse(schedule.date)
        updateDateDisplay()

        schedule.startTime?.let {
            startTime = LocalTime.parse(it)
            binding.btnStartTime.text = it
        }
        schedule.endTime?.let {
            endTime = LocalTime.parse(it)
            binding.btnEndTime.text = it
        }

        binding.switchAllDay.isChecked = schedule.isAllDay
        toggleAllDayMode(schedule.isAllDay)

        binding.spinnerColor.setSelection(schedule.categoryColor)

        if (schedule.alarmEnabled) {
            val alarmIndex = when (schedule.alarmMinutesBefore) {
                0 -> 1
                5 -> 2
                10 -> 3
                15 -> 4
                30 -> 5
                60 -> 6
                else -> 0
            }
            binding.spinnerAlarm.setSelection(alarmIndex)
        }
    }

    private fun setupListeners() {
        binding.btnDate.setOnClickListener { showDatePicker() }
        binding.btnStartTime.setOnClickListener { showTimePicker(isStart = true) }
        binding.btnEndTime.setOnClickListener { showTimePicker(isStart = false) }

        binding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            toggleAllDayMode(isChecked)
        }

        binding.btnSave.setOnClickListener { saveSchedule() }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("일정 삭제")
                .setMessage("이 일정을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    existingSchedule?.let { schedule ->
                        viewModel.deleteSchedule(schedule)
                        AlarmScheduler.cancelAlarm(this, schedule.id)
                    }
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun toggleAllDayMode(isAllDay: Boolean) {
        val timeVisibility = if (isAllDay) View.GONE else View.VISIBLE
        binding.layoutStartTime.visibility = timeVisibility
        binding.layoutEndTime.visibility = timeVisibility
        binding.layoutAlarm.visibility = timeVisibility
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val current = if (isStart) startTime ?: LocalTime.now() else endTime ?: LocalTime.now()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val time = LocalTime.of(hourOfDay, minute)
                if (isStart) {
                    startTime = time
                    binding.btnStartTime.text = time.format(timeFormatter)
                } else {
                    endTime = time
                    binding.btnEndTime.text = time.format(timeFormatter)
                }
            },
            current.hour,
            current.minute,
            true
        ).show()
    }

    private fun updateDateDisplay() {
        binding.btnDate.text = selectedDate.format(dateFormatter)
    }

    private fun saveSchedule() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTitle.error = "제목을 입력하세요"
            return
        }

        val isAllDay = binding.switchAllDay.isChecked
        val alarmSpinnerPos = binding.spinnerAlarm.selectedItemPosition
        val alarmEnabled = alarmSpinnerPos > 0 && !isAllDay
        val alarmMinutesBefore = when (alarmSpinnerPos) {
            1 -> 0
            2 -> 5
            3 -> 10
            4 -> 15
            5 -> 30
            6 -> 60
            else -> 0
        }

        val schedule = Schedule(
            id = existingSchedule?.id ?: 0,
            title = title,
            description = binding.etDescription.text.toString().trim(),
            date = selectedDate.toString(),
            startTime = if (!isAllDay) startTime?.format(timeFormatter) else null,
            endTime = if (!isAllDay) endTime?.format(timeFormatter) else null,
            isAllDay = isAllDay,
            alarmEnabled = alarmEnabled,
            alarmMinutesBefore = alarmMinutesBefore,
            categoryColor = binding.spinnerColor.selectedItemPosition,
            isCompleted = existingSchedule?.isCompleted ?: false
        )

        viewModel.saveSchedule(schedule) { savedId ->
            if (alarmEnabled) {
                AlarmScheduler.scheduleAlarm(this, schedule.copy(id = savedId))
            } else {
                AlarmScheduler.cancelAlarm(this, savedId)
            }
            finish()
        }
    }

    private fun observeData() {
        viewModel.saveComplete.observe(this) { success ->
            if (success) finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
