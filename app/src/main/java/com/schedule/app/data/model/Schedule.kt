package com.schedule.app.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

@Parcelize
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // LocalDate.toString() format: yyyy-MM-dd
    val startTime: String? = null, // LocalTime.toString() format: HH:mm
    val endTime: String? = null,
    val isAllDay: Boolean = false,
    val alarmEnabled: Boolean = false,
    val alarmMinutesBefore: Int = 0, // 0 = at time, 5, 10, 15, 30, 60 minutes before
    val categoryColor: Int = 0, // Color index (0-5)
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class CategoryColor(val colorRes: String) {
    RED("#F44336"),
    ORANGE("#FF9800"),
    YELLOW("#FFC107"),
    GREEN("#4CAF50"),
    BLUE("#2196F3"),
    PURPLE("#9C27B0")
}
