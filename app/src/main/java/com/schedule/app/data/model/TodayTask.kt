package com.schedule.app.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "today_tasks")
data class TodayTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // yyyy-MM-dd
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0=Low, 1=Medium, 2=High
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class TaskPriority(val level: Int, val label: String) {
    LOW(0, "낮음"),
    MEDIUM(1, "보통"),
    HIGH(2, "높음")
}
