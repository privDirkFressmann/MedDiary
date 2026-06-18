package com.meddiary.data

import kotlinx.coroutines.flow.Flow

class MedicalRepository(
    private val appointmentDao: AppointmentDao,
    private val checkupDao: CheckupDao,
    private val familyMemberDao: FamilyMemberDao,
    private val attachmentDao: AttachmentDao
) {
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
    
    fun getUpcomingAppointments(currentMillis: Long): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(currentMillis)
    }

    fun getAppointmentById(id: Int): Flow<Appointment?> {
        return appointmentDao.getAppointmentById(id)
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.deleteAppointment(appointment)
    }

    val allCheckups: Flow<List<Checkup>> = checkupDao.getAllCheckups()

    fun getCheckupById(id: String, personName: String): Flow<Checkup?> {
        return checkupDao.getCheckupByIdAndPerson(id, personName)
    }

    suspend fun insertCheckup(checkup: Checkup) {
        checkupDao.insertCheckup(checkup)
    }

    suspend fun updateCheckup(checkup: Checkup) {
        checkupDao.updateCheckup(checkup)
    }

    suspend fun deleteCheckup(checkup: Checkup) {
        checkupDao.deleteCheckup(checkup)
    }

    // Family Member operations
    val allFamilyMembers: Flow<List<FamilyMember>> = familyMemberDao.getAllFamilyMembers()

    suspend fun addFamilyMemberWithCheckups(name: String, relation: String, checkups: List<Checkup>) {
        familyMemberDao.insertFamilyMember(FamilyMember(name, relation))
        checkupDao.insertCheckups(checkups)
    }

    suspend fun deleteFamilyMember(familyMember: FamilyMember) {
        familyMemberDao.deleteFamilyMember(familyMember)
    }

    // Attachment operations
    fun getAttachmentsForAppointment(appointmentId: Int): Flow<List<Attachment>> {
        return attachmentDao.getAttachmentsForAppointment(appointmentId)
    }

    suspend fun insertAttachment(attachment: Attachment): Long {
        return attachmentDao.insertAttachment(attachment)
    }

    suspend fun deleteAttachment(attachment: Attachment) {
        attachmentDao.deleteAttachment(attachment)
    }
}
