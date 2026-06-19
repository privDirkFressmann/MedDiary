package com.meddiary.ui

import android.content.Context
import android.net.Uri
import com.meddiary.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    suspend fun exportToZip(context: Context, database: MedicalDatabase, outputUri: Uri) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val familyMembers = database.familyMemberDao().getAllFamilyMembers().first()
        val appointments = database.appointmentDao().getAllAppointments().first()
        val checkups = database.checkupDao().getAllCheckups().first()
        val attachments = database.attachmentDao().getAllAttachments().first()
        val vaccinations = database.vaccinationDao().getAllVaccinations().first()
        val doctors = database.doctorDao().getAllDoctors().first()

        // Generate JSON
        val backupJson = JSONObject().apply {
            put("version", 1)
            
            // Family Members
            val fmArray = JSONArray()
            familyMembers.forEach {
                fmArray.put(JSONObject().apply {
                    put("name", it.name)
                    put("relation", it.relation)
                    put("birthYear", it.birthYear)
                    put("gender", it.gender)
                })
            }
            put("family_members", fmArray)

            // Vaccinations
            val vacArray = JSONArray()
            vaccinations.forEach {
                vacArray.put(JSONObject().apply {
                    put("personName", it.personName)
                    put("title", it.title)
                    put("dateMillis", it.dateMillis)
                    put("batchNumber", it.batchNumber)
                    put("doctorName", it.doctorName)
                    put("notes", it.notes)
                })
            }
            put("vaccinations", vacArray)

            // Appointments
            val apptArray = JSONArray()
            appointments.forEach {
                apptArray.put(JSONObject().apply {
                    put("id", it.id)
                    put("personName", it.personName)
                    put("title", it.title)
                    put("doctor", it.doctor)
                    put("specialty", it.specialty)
                    put("dateMillis", it.dateMillis)
                    put("notes", it.notes)
                    put("reminderEnabled", it.reminderEnabled)
                    put("reminderTimeMillis", it.reminderTimeMillis)
                    put("isCompleted", it.isCompleted)
                })
            }
            put("appointments", apptArray)

            // Checkups
            val checkupArray = JSONArray()
            checkups.forEach {
                checkupArray.put(JSONObject().apply {
                    put("id", it.id)
                    put("personName", it.personName)
                    put("title", it.title)
                    put("category", it.category)
                    put("description", it.description)
                    put("recommendedAge", it.recommendedAge)
                    put("intervalMonths", it.intervalMonths)
                    put("gender", it.gender)
                    put("lastDoneMillis", it.lastDoneMillis ?: JSONObject.NULL)
                    put("nextDueMillis", it.nextDueMillis ?: JSONObject.NULL)
                    put("isCustom", it.isCustom)
                    put("isEnabled", it.isEnabled)
                })
            }
            put("checkups", checkupArray)

            // Attachments
            val attachArray = JSONArray()
            attachments.forEach {
                attachArray.put(JSONObject().apply {
                    put("id", it.id)
                    put("appointmentId", it.appointmentId)
                    put("displayName", it.displayName)
                    put("fileNameInZip", File(it.filePath).name)
                })
            }
            put("attachments", attachArray)

            // Doctors
            val docArray = JSONArray()
            doctors.forEach {
                docArray.put(JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("address", it.address)
                    put("phoneNumber", it.phoneNumber)
                })
            }
            put("doctors", docArray)
        }

        contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. Write JSON file
                val jsonEntry = ZipEntry("data.json")
                zipOut.putNextEntry(jsonEntry)
                zipOut.write(backupJson.toString(4).toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 2. Write attachment files
                attachments.forEach { attach ->
                    val file = File(attach.filePath)
                    if (file.exists()) {
                        val fileEntry = ZipEntry("attachments/${file.name}")
                        zipOut.putNextEntry(fileEntry)
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    suspend fun importFromZip(context: Context, database: MedicalDatabase, inputUri: Uri) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val tempDir = File(context.cacheDir, "temp_import").apply { 
            deleteRecursively()
            mkdirs() 
        }

        // 1. Unzip everything to a temporary cache directory
        contentResolver.openInputStream(inputUri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val destFile = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        destFile.outputStream().use { output ->
                            zipIn.copyTo(output)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }

        // 2. Parse data.json
        val jsonFile = File(tempDir, "data.json")
        if (!jsonFile.exists()) throw FileNotFoundException("data.json nicht im Backup-Archiv gefunden")
        val jsonContent = jsonFile.readText(Charsets.UTF_8)
        val backupJson = JSONObject(jsonContent)

        // Clear existing database tables
        database.clearAllTables()

        val familyMembers = mutableListOf<FamilyMember>()
        val fmArray = backupJson.optJSONArray("family_members")
        if (fmArray != null) {
            for (i in 0 until fmArray.length()) {
                val obj = fmArray.getJSONObject(i)
                val rawName = obj.getString("name")
                val mappedName = if (rawName == "Ich") "Dirk" else rawName
                familyMembers.add(
                    FamilyMember(
                        name = mappedName,
                        relation = obj.optString("relation", "Kind"),
                        birthYear = obj.optInt("birthYear", 1990),
                        gender = obj.optString("gender", "ALL")
                    )
                )
            }
        }

        val vaccinations = mutableListOf<Vaccination>()
        val vacArray = backupJson.optJSONArray("vaccinations")
        if (vacArray != null) {
            for (i in 0 until vacArray.length()) {
                val obj = vacArray.getJSONObject(i)
                val rawPerson = obj.getString("personName")
                val mappedPerson = if (rawPerson == "Ich") "Dirk" else rawPerson
                vaccinations.add(
                    Vaccination(
                        personName = mappedPerson,
                        title = obj.getString("title"),
                        dateMillis = obj.getLong("dateMillis"),
                        batchNumber = obj.optString("batchNumber", ""),
                        doctorName = obj.optString("doctorName", ""),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        val checkups = mutableListOf<Checkup>()
        val checkupArray = backupJson.optJSONArray("checkups")
        if (checkupArray != null) {
            for (i in 0 until checkupArray.length()) {
                val obj = checkupArray.getJSONObject(i)
                val lastDone = if (obj.isNull("lastDoneMillis")) null else obj.optLong("lastDoneMillis")
                val nextDue = if (obj.isNull("nextDueMillis")) null else obj.optLong("nextDueMillis")
                val rawPerson = obj.getString("personName")
                val mappedPerson = if (rawPerson == "Ich") "Dirk" else rawPerson
                checkups.add(
                    Checkup(
                        id = obj.getString("id"),
                        personName = mappedPerson,
                        title = obj.getString("title"),
                        category = obj.getString("category"),
                        description = obj.getString("description"),
                        recommendedAge = obj.getString("recommendedAge"),
                        intervalMonths = obj.getInt("intervalMonths"),
                        gender = obj.getString("gender"),
                        lastDoneMillis = lastDone,
                        nextDueMillis = nextDue,
                        isCustom = obj.optBoolean("isCustom", false),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
        }

        // Map old appointment IDs to new ones during insertion (since IDs are autogenerated)
        val oldToNewApptIdMap = mutableMapOf<Int, Int>()

        val appointmentsArray = backupJson.optJSONArray("appointments")
        if (appointmentsArray != null) {
            for (i in 0 until appointmentsArray.length()) {
                val obj = appointmentsArray.getJSONObject(i)
                val oldId = obj.getInt("id")
                val rawPerson = obj.getString("personName")
                val mappedPerson = if (rawPerson == "Ich") "Dirk" else rawPerson
                val appt = Appointment(
                    title = obj.getString("title"),
                    doctor = obj.getString("doctor"),
                    specialty = obj.getString("specialty"),
                    dateMillis = obj.getLong("dateMillis"),
                    notes = obj.optString("notes", ""),
                    reminderEnabled = obj.optBoolean("reminderEnabled", false),
                    reminderTimeMillis = obj.optLong("reminderTimeMillis", 0L),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    personName = mappedPerson
                )
                val newId = database.appointmentDao().insertAppointment(appt).toInt()
                oldToNewApptIdMap[oldId] = newId
            }
        }

        // 3. Move files to permanent attachments dir & Insert attachments records
        val attachmentsDir = File(context.filesDir, "attachments").apply { mkdirs() }
        
        val attachArray = backupJson.optJSONArray("attachments")
        if (attachArray != null) {
            for (i in 0 until attachArray.length()) {
                val obj = attachArray.getJSONObject(i)
                val oldApptId = obj.getInt("appointmentId")
                val newApptId = oldToNewApptIdMap[oldApptId] ?: continue
                val fileNameInZip = obj.getString("fileNameInZip")
                val displayName = obj.getString("displayName")

                val tempFile = File(tempDir, "attachments/$fileNameInZip")
                if (tempFile.exists()) {
                    val destFile = File(attachmentsDir, "${System.currentTimeMillis()}_$fileNameInZip")
                    tempFile.copyTo(destFile, overwrite = true)

                    database.attachmentDao().insertAttachment(
                        Attachment(
                            appointmentId = newApptId,
                            filePath = destFile.absolutePath,
                            displayName = displayName
                        )
                    )
                }
            }
        }

        val doctors = mutableListOf<Doctor>()
        val docArray = backupJson.optJSONArray("doctors")
        if (docArray != null) {
            for (i in 0 until docArray.length()) {
                val obj = docArray.getJSONObject(i)
                doctors.add(
                    Doctor(
                        name = obj.getString("name"),
                        address = obj.optString("address", ""),
                        phoneNumber = obj.optString("phoneNumber", "")
                    )
                )
            }
        }

        // Insert family members, checkups and vaccinations
        familyMembers.forEach { database.familyMemberDao().insertFamilyMember(it) }
        checkups.forEach { database.checkupDao().insertCheckup(it) }
        vaccinations.forEach { database.vaccinationDao().insertVaccination(it) }
        doctors.forEach { database.doctorDao().insertDoctor(it) }

        tempDir.deleteRecursively()
    }
}
