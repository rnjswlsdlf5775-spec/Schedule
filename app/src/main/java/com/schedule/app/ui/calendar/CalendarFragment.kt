package com.schedule.app.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.app.ScheduleApplication
import com.schedule.app.databinding.FragmentCalendarBinding
import com.schedule.app.ui.schedule.ScheduleAdapter
import com.schedule.app.ui.schedule.ScheduleDetailActivity
import android.content.Intent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            (requireActivity().application as ScheduleApplication).scheduleRepository
        )
    }

    private lateinit var scheduleAdapter: ScheduleAdapter
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendarView()
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.loadSchedulesForDate(selectedDate)
            updateDateLabel()
        }
        viewModel.loadSchedulesForDate(selectedDate)
        updateDateLabel()
    }

    private fun updateDateLabel() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        binding.tvSelectedDate.text = selectedDate.format(formatter)
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(
            onItemClick = { schedule ->
                val intent = Intent(requireContext(), ScheduleDetailActivity::class.java).apply {
                    putExtra(ScheduleDetailActivity.EXTRA_SCHEDULE, schedule)
                }
                startActivity(intent)
            },
            onCheckClick = { schedule ->
                viewModel.toggleScheduleComplete(schedule)
            }
        )
        binding.rvSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddSchedule.setOnClickListener {
            val intent = Intent(requireContext(), ScheduleDetailActivity::class.java).apply {
                putExtra(ScheduleDetailActivity.EXTRA_DATE, selectedDate.toString())
            }
            startActivity(intent)
        }
    }

    private fun observeData() {
        viewModel.schedulesForSelectedDate.observe(viewLifecycleOwner) { schedules ->
            scheduleAdapter.submitList(schedules)
            binding.tvEmptySchedules.visibility =
                if (schedules.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.markedDates.observe(viewLifecycleOwner) { dates ->
            // Calendar dots for dates with schedules handled by custom view
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
