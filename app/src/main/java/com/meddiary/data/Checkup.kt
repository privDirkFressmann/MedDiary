package com.meddiary.data

import androidx.room.Entity

@Entity(tableName = "checkups", primaryKeys = ["id", "personName"])
data class Checkup(
    val id: String,
    val personName: String = "Dirk",
    val title: String,
    val category: String,
    val description: String,
    val recommendedAge: String,
    val intervalMonths: Int, // 0 means one-time or irregular
    val gender: String, // "ALL", "M", "F"
    val lastDoneMillis: Long? = null,
    val nextDueMillis: Long? = null,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
)
