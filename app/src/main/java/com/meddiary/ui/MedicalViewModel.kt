package com.meddiary.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meddiary.data.Appointment
import com.meddiary.data.Attachment
import com.meddiary.data.Checkup
import com.meddiary.data.FamilyMember
import com.meddiary.data.MedicalDatabase
import com.meddiary.data.MedicalRepository
import com.meddiary.data.Vaccination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class MedicalViewModel(
    private val repository: MedicalRepository,
    val database: MedicalDatabase
) : ViewModel() {

    private val _selectedPerson = MutableStateFlow("Dirk")
    val selectedPerson: StateFlow<String> = _selectedPerson.asStateFlow()

    val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val upcomingAppointments: StateFlow<List<Appointment>> = repository.getUpcomingAppointments(System.currentTimeMillis())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCheckups: StateFlow<List<Checkup>> = repository.allCheckups
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val familyMembers: StateFlow<List<FamilyMember>> = repository.allFamilyMembers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allVaccinations: StateFlow<List<Vaccination>> = repository.getAllVaccinations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        // Safe database initialization: check if profiles are empty and insert default "Dirk"
        viewModelScope.launch {
            try {
                val members = repository.allFamilyMembers.first()
                if (members.isEmpty()) {
                    addFamilyMember("Dirk", "Ich", 1990, "ALL")
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun selectPerson(name: String) {
        _selectedPerson.value = name
    }

    fun getAppointmentById(id: Int): Flow<Appointment?> {
        return repository.getAppointmentById(id)
    }

    fun saveAppointment(
        id: Int = 0,
        personName: String = "Dirk",
        title: String,
        doctor: String,
        specialty: String,
        dateMillis: Long,
        notes: String = "",
        reminderEnabled: Boolean = false,
        reminderTimeMillis: Long = 0,
        isCompleted: Boolean = false,
        newAttachments: List<Pair<String, String>> = emptyList() // Pair<FilePath, DisplayName>
    ) {
        viewModelScope.launch {
            val appointment = Appointment(
                id = id,
                personName = personName,
                title = title,
                doctor = doctor,
                specialty = specialty,
                dateMillis = dateMillis,
                notes = notes,
                reminderEnabled = reminderEnabled,
                reminderTimeMillis = reminderTimeMillis,
                isCompleted = isCompleted
            )
            val finalId = if (id == 0) {
                repository.insertAppointment(appointment).toInt()
            } else {
                repository.updateAppointment(appointment)
                id
            }
            
            // Save newly picked attachments
            newAttachments.forEach { (path, name) ->
                repository.insertAttachment(
                    Attachment(
                        appointmentId = finalId,
                        filePath = path,
                        displayName = name
                    )
                )
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    fun toggleAppointmentCompleted(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(isCompleted = !appointment.isCompleted))
        }
    }

    fun markCheckupAsDone(checkup: Checkup, doneDateMillis: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = doneDateMillis
            }
            val nextDueMillis = if (checkup.intervalMonths > 0) {
                calendar.add(Calendar.MONTH, checkup.intervalMonths)
                calendar.timeInMillis
            } else {
                null
            }
            val updatedCheckup = checkup.copy(
                lastDoneMillis = doneDateMillis,
                nextDueMillis = nextDueMillis
            )
            repository.updateCheckup(updatedCheckup)
        }
    }

    fun addFamilyMember(name: String, relation: String, birthYear: Int, gender: String) {
        viewModelScope.launch {
            val checkups = if (relation == "Kind") {
                MedicalDatabase.getDefaultChildCheckups(name)
            } else {
                MedicalDatabase.getDefaultAdultCheckups(name)
            }
            repository.addFamilyMemberWithCheckups(name, relation, birthYear, gender, checkups)
        }
    }

    fun deleteFamilyMember(familyMember: FamilyMember) {
        viewModelScope.launch {
            repository.deleteFamilyMember(familyMember)
        }
    }

    // Vaccination operations
    fun addVaccination(
        personName: String,
        title: String,
        dateMillis: Long,
        batchNumber: String = "",
        doctorName: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertVaccination(
                Vaccination(
                    personName = personName,
                    title = title,
                    dateMillis = dateMillis,
                    batchNumber = batchNumber,
                    doctorName = doctorName,
                    notes = notes
                )
            )
        }
    }

    fun deleteVaccination(vaccination: Vaccination) {
        viewModelScope.launch {
            repository.deleteVaccination(vaccination)
        }
    }

    fun addCustomCheckup(
        personName: String,
        title: String,
        category: String,
        description: String,
        recommendedAge: String,
        intervalMonths: Int,
        gender: String
    ) {
        viewModelScope.launch {
            val checkup = Checkup(
                id = "custom_" + System.currentTimeMillis().toString(),
                personName = personName,
                title = title,
                category = category,
                description = description,
                recommendedAge = recommendedAge,
                intervalMonths = intervalMonths,
                gender = gender,
                isCustom = true
            )
            repository.insertCheckup(checkup)
        }
    }

    fun deleteCheckup(checkup: Checkup) {
        viewModelScope.launch {
            repository.deleteCheckup(checkup)
        }
    }

    fun toggleCheckupEnabled(checkup: Checkup) {
        viewModelScope.launch {
            repository.updateCheckup(checkup.copy(isEnabled = !checkup.isEnabled))
        }
    }

    // Attachment operations
    fun getAttachmentsForAppointment(appointmentId: Int): Flow<List<Attachment>> {
        return repository.getAttachmentsForAppointment(appointmentId)
    }

    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            try {
                // Delete physical file on disk to free up space
                val file = File(attachment.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Backup & Import operations
    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                BackupManager.exportToZip(context, database, uri)
                onResult(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, e.localizedMessage ?: e.message ?: "Fehler beim Export")
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                BackupManager.importFromZip(context, database, uri)
                // Select first family member if "Dirk" is not present in the imported family members
                val members = repository.allFamilyMembers.first()
                if (members.isNotEmpty()) {
                    val firstMember = members.firstOrNull { it.name == "Dirk" } ?: members.first()
                    selectPerson(firstMember.name)
                } else {
                    addFamilyMember("Dirk", "Ich", 1990, "ALL")
                    selectPerson("Dirk")
                }
                onResult(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, e.localizedMessage ?: e.message ?: "Fehler beim Import")
            }
        }
    }
}

class MedicalViewModelFactory(
    private val repository: MedicalRepository,
    private val database: MedicalDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalViewModel(repository, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
