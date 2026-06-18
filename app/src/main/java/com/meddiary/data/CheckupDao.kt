package com.meddiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckupDao {
    @Query("SELECT * FROM checkups ORDER BY nextDueMillis ASC, title ASC")
    fun getAllCheckups(): Flow<List<Checkup>>

    @Query("SELECT * FROM checkups WHERE id = :id AND personName = :personName")
    fun getCheckupByIdAndPerson(id: String, personName: String): Flow<Checkup?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckups(checkups: List<Checkup>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckup(checkup: Checkup)

    @Update
    suspend fun updateCheckup(checkup: Checkup)

    @Delete
    suspend fun deleteCheckup(checkup: Checkup)
}
