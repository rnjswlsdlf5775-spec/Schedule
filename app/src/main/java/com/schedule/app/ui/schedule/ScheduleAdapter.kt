package com.schedule.app.ui.schedule

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.schedule.app.data.model.CategoryColor
import com.schedule.app.data.model.Schedule
import com.schedule.app.databinding.ItemScheduleBinding

class ScheduleAdapter(
    private val onItemClick: (Schedule) -> Unit,
    private val onCheckClick: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule) {
            binding.apply {
                tvTitle.text = schedule.title
                tvDescription.text = schedule.description.ifEmpty { "" }

                // Time display
                if (schedule.isAllDay) {
                    tvTime.text = "종일"
                } else {
                    val time = buildString {
                        schedule.startTime?.let { append(it) }
                        schedule.endTime?.let { append(" ~ $it") }
                    }
                    tvTime.text = time.ifEmpty { "" }
                }

                // Color indicator
                val colors = CategoryColor.values()
                val colorHex = if (schedule.categoryColor < colors.size) {
                    colors[schedule.categoryColor].colorRes
                } else {
                    colors[0].colorRes
                }
                viewColorIndicator.setBackgroundColor(Color.parseColor(colorHex))

                // Alarm indicator
                ivAlarm.visibility = if (schedule.alarmEnabled) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Completion state
                cbComplete.isChecked = schedule.isCompleted
                if (schedule.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTitle.alpha = 0.5f
                } else {
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTitle.alpha = 1.0f
                }

                cbComplete.setOnClickListener { onCheckClick(schedule) }
                root.setOnClickListener { onItemClick(schedule) }
            }
        }
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule) =
            oldItem == newItem
    }
}
