package com.schedule.app.ui.today

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.schedule.app.R
import com.schedule.app.data.model.TodayTask
import com.schedule.app.databinding.ItemTodayTaskBinding

class TodayTaskAdapter(
    private val onTaskClick: (TodayTask) -> Unit,
    private val onCheckClick: (TodayTask) -> Unit,
    private val onDeleteClick: (TodayTask) -> Unit
) : ListAdapter<TodayTask, TodayTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTodayTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTodayTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TodayTask) {
            binding.apply {
                tvTaskTitle.text = task.title
                tvTaskDescription.text = task.description

                tvTaskDescription.visibility = if (task.description.isNotEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Priority color
                val priorityColor = when (task.priority) {
                    2 -> R.color.priority_high
                    1 -> R.color.priority_medium
                    else -> R.color.priority_low
                }
                viewPriorityIndicator.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, priorityColor)
                )

                val priorityText = when (task.priority) {
                    2 -> "높음"
                    1 -> "보통"
                    else -> "낮음"
                }
                tvPriority.text = priorityText

                // Completion state
                cbTask.isChecked = task.isCompleted
                if (task.isCompleted) {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTaskTitle.alpha = 0.5f
                    tvTaskDescription.alpha = 0.5f
                } else {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTaskTitle.alpha = 1.0f
                    tvTaskDescription.alpha = 1.0f
                }

                cbTask.setOnClickListener { onCheckClick(task) }
                ibDelete.setOnClickListener { onDeleteClick(task) }
                root.setOnClickListener { onTaskClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TodayTask>() {
        override fun areItemsTheSame(oldItem: TodayTask, newItem: TodayTask) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodayTask, newItem: TodayTask) =
            oldItem == newItem
    }
}
