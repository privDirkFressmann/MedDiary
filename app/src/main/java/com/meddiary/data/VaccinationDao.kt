package com.meddiary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE personName = :personName ORDER BY dateMillis DESC")
    fun getVaccinationsForPerson(personName: String): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations ORDER BY dateMillis DESC")
    fun getAllVaccinations(): Flow<List<Vaccination>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination): Long

    @Delete
    suspend fun deleteVaccination(vaccination: Vaccination)
}
