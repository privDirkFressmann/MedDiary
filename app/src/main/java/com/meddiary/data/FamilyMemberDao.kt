package com.meddiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY relation DESC, name ASC")
    fun getAllFamilyMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFamilyMember(familyMember: FamilyMember)

    @Delete
    suspend fun deleteFamilyMember(familyMember: FamilyMember)
}
