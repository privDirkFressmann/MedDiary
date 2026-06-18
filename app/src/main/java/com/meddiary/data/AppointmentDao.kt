package com.meddiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateMillis ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateMillis >= :currentMillis ORDER BY dateMillis ASC")
    fun getUpcomingAppointments(currentMillis: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getAppointmentById(id: Int): Flow<Appointment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}
