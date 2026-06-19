package com.meddiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)
}
