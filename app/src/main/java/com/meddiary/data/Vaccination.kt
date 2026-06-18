package com.meddiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccinations")
data class Vaccination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personName: String,
    val title: String, // e.g. "Tetanus"
    val dateMillis: Long, // when it was administered
    val batchNumber: String = "", // Chargennummer
    val doctorName: String = "",
    val notes: String = ""
)
