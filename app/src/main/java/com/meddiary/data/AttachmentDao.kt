package com.meddiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE appointmentId = :appointmentId")
    fun getAttachmentsForAppointment(appointmentId: Int): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments")
    fun getAllAttachments(): Flow<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: Attachment): Long

    @Delete
    suspend fun deleteAttachment(attachment: Attachment)
}
