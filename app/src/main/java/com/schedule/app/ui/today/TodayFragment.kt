package com.schedule.app.ui.today

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.app.R
import com.schedule.app.ScheduleApplication
import com.schedule.app.data.model.TodayTask
import com.schedule.app.databinding.FragmentTodayBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodayViewModel by viewModels {
        TodayViewModelFactory(
            (requireActivity().application as ScheduleApplication).taskRepository
        )
    }

    private lateinit var taskAdapter: TodayTaskAdapter
    private val today = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDateHeader()
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupDateHeader() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (EEEE)")
        binding.tvTodayDate.text = today.format(
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        )
    }

    private fun setupRecyclerView() {
        taskAdapter = TodayTaskAdapter(
            onTaskClick = { task -> showEditTaskDialog(task) },
            onCheckClick = { task -> viewModel.toggleTaskCompletion(task) },
            onDeleteClick = { task -> viewModel.deleteTask(task) }
        )
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        showTaskDialog(null)
    }

    private fun showEditTaskDialog(task: TodayTask) {
        showTaskDialog(task)
    }

    private fun showTaskDialog(existingTask: TodayTask?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_task_title)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_task_description)
        val rgPriority = dialogView.findViewById<RadioGroup>(R.id.rg_priority)

        existingTask?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
            when (it.priority) {
                0 -> rgPriority.check(R.id.rb_low)
                1 -> rgPriority.check(R.id.rb_medium)
                2 -> rgPriority.check(R.id.rb_high)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existingTask == null) "할 일 추가" else "할 일 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    val priority = when (rgPriority.checkedRadioButtonId) {
                        R.id.rb_low -> 0
                        R.id.rb_high -> 2
                        else -> 1
                    }
                    val task = TodayTask(
                        id = existingTask?.id ?: 0,
                        title = title,
                        description = etDescription.text.toString().trim(),
                        date = today.toString(),
                        isCompleted = existingTask?.isCompleted ?: false,
                        priority = priority
                    )
                    viewModel.saveTask(task)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun observeData() {
        viewModel.tasksForToday.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            binding.tvEmptyTasks.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.completedCount.observe(viewLifecycleOwner) { completed ->
            viewModel.totalCount.observe(viewLifecycleOwner) { total ->
                if (total > 0) {
                    binding.tvProgress.text = "$completed / $total 완료"
                    binding.progressBar.progress = (completed * 100 / total)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvProgress.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.tvProgress.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
