package com.meddiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personName: String = "Dirk",
    val title: String,
    val doctor: String,
    val specialty: String,
    val dateMillis: Long,
    val notes: String = "",
    val reminderEnabled: Boolean = false,
    val reminderTimeMillis: Long = 0,
    val isCompleted: Boolean = false
)
