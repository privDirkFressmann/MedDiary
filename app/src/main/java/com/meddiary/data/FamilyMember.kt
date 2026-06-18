package com.meddiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey val name: String,
    val relation: String = "Kind", // "Ich" (Dirk), "Kind", "Partner"
    val birthYear: Int = 1990,
    val gender: String = "ALL" // "ALL", "M", "F"
)

