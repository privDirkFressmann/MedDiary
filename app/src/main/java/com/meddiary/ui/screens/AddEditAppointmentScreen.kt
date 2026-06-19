package com.meddiary.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.meddiary.ui.MedicalViewModel
import com.meddiary.data.Appointment
import java.io.File
import kotlinx.coroutines.flow.first
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    viewModel: MedicalViewModel,
    appointmentId: Int?,
    copy: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val familyMembers by viewModel.familyMembers.collectAsState()
    val sortedMembers = remember(familyMembers) {
        val list = familyMembers.toMutableList()
        val dirk = list.firstOrNull { it.name == "Dirk" }
        if (dirk != null) {
            list.remove(dirk)
            list.add(0, dirk)
        }
        list
    }
    
    val specialtiesList = remember {
        listOf(
            "Allgemeinmedizin (Hausarzt)",
            "Augenarzt",
            "Dermatologie (Hautarzt)",
            "Frauenarzt (Gynäkologie)",
            "HNO-Arzt",
            "Kardiologie",
            "Kinderarzt",
            "Orthopädie",
            "Rheumatologie",
            "Urologie",
            "Zahnarzt",
            "Sonstige"
        )
    }

    var selectedPerson by remember { mutableStateOf("Dirk") }
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf("Allgemeinmedizin (Hausarzt)") }
    var customSpecialty by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    var isEditMode by remember { mutableStateOf(false) }
    var existingAppointment: Appointment? by remember { mutableStateOf(null) }
    var expandedPersonDropdown by remember { mutableStateOf(false) }
    var expandedSpecialtyDropdown by remember { mutableStateOf(false) }

    // File Attachments States
    val newAttachments = remember { mutableStateListOf<Pair<String, String>>() } // Path, Name
    val savedAttachments by if (appointmentId != null && appointmentId != 0 && !copy) {
        viewModel.getAttachmentsForAppointment(appointmentId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    var previewImagePath by remember { mutableStateOf<String?>(null) }

    val doctorsState by viewModel.allDoctors.collectAsState()
    val existingDoctors = remember(doctorsState) {
        doctorsState.map { it.name }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val filteredDoctors = remember(existingDoctors, doctor) {
        if (doctor.isBlank()) {
            existingDoctors
        } else {
            existingDoctors.filter { it.contains(doctor, ignoreCase = true) && it.lowercase() != doctor.lowercase() }
        }
    }
    var expandedDoctorDropdown by remember { mutableStateOf(false) }

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val copiedFile = copyUriToInternalStorage(context, it)
            if (copiedFile != null) {
                newAttachments.add(copiedFile)
            }
        }
    }

    // Load existing appointment if editing or copying
    LaunchedEffect(appointmentId) {
        if (appointmentId != null && appointmentId != 0) {
            viewModel.getAppointmentById(appointmentId).collect { appt ->
                if (appt != null && existingAppointment == null) {
                    existingAppointment = appt
                    isEditMode = !copy
                    selectedPerson = appt.personName
                    title = appt.title
                    doctor = appt.doctor
                    notes = appt.notes
                    reminderEnabled = appt.reminderEnabled
                    selectedDateMillis = appt.dateMillis
                    calendar.timeInMillis = appt.dateMillis

                    if (specialtiesList.contains(appt.specialty)) {
                        selectedSpecialty = appt.specialty
                        customSpecialty = ""
                    } else {
                        selectedSpecialty = "Sonstige"
                        customSpecialty = appt.specialty
                    }

                    if (copy) {
                        try {
                            viewModel.getAttachmentsForAppointment(appointmentId).first().forEach { att ->
                                val srcFile = File(att.filePath)
                                if (srcFile.exists()) {
                                    val attachmentsDir = File(context.filesDir, "attachments").apply { mkdirs() }
                                    val destFile = File(attachmentsDir, "${System.currentTimeMillis()}_${srcFile.name}")
                                    try {
                                        srcFile.inputStream().use { input ->
                                            destFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        newAttachments.add(Pair(destFile.absolutePath, att.displayName))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val timeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    val formattedDate = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN).format(Date(selectedDateMillis))
    val formattedTime = String.format(Locale.GERMAN, "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

    val finalSpecialty = remember(selectedSpecialty, customSpecialty) {
        if (selectedSpecialty == "Sonstige") customSpecialty.trim() else selectedSpecialty
    }

    val isSaveEnabled = title.isNotBlank() && finalSpecialty.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Termin bearbeiten" else "Termin hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaveEnabled) {
                                viewModel.saveAppointment(
                                    id = if (copy) 0 else (appointmentId ?: 0),
                                    personName = selectedPerson,
                                    title = title,
                                    doctor = doctor,
                                    specialty = finalSpecialty,
                                    dateMillis = selectedDateMillis,
                                    notes = notes,
                                    reminderEnabled = reminderEnabled,
                                    isCompleted = if (copy) false else (existingAppointment?.isCompleted ?: false),
                                    newAttachments = newAttachments
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = isSaveEnabled
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Speichern")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown to select the family member
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedPerson,
                    onValueChange = {},
                    label = { Text("Für wen ist dieser Termin?") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedPersonDropdown = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Auswählen")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedPersonDropdown,
                    onDismissRequest = { expandedPersonDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sortedMembers.forEach { member ->
                        DropdownMenuItem(
                            text = { Text(member.name) },
                            onClick = {
                                selectedPerson = member.name
                                expandedPersonDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titel (z.B. Zahnreinigung)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { newValue ->
                        doctor = newValue
                        expandedDoctorDropdown = true
                        val matchedDoc = doctorsState.firstOrNull { it.name.trim().equals(newValue.trim(), ignoreCase = true) }
                        if (matchedDoc != null && matchedDoc.specialty.isNotBlank()) {
                            if (specialtiesList.contains(matchedDoc.specialty)) {
                                selectedSpecialty = matchedDoc.specialty
                                customSpecialty = ""
                            } else {
                                selectedSpecialty = "Sonstige"
                                customSpecialty = matchedDoc.specialty
                            }
                        }
                    },
                    label = { Text("Arzt/Ärztin (z.B. Dr. Müller)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (existingDoctors.isNotEmpty()) {
                            IconButton(onClick = { expandedDoctorDropdown = !expandedDoctorDropdown }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Ärzte vorschlagen")
                            }
                        }
                    }
                )
                if (filteredDoctors.isNotEmpty()) {
                    DropdownMenu(
                        expanded = expandedDoctorDropdown,
                        onDismissRequest = { expandedDoctorDropdown = false },
                        properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredDoctors.take(5).forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc) },
                                onClick = {
                                    doctor = doc
                                    expandedDoctorDropdown = false
                                    val matchedDoc = doctorsState.firstOrNull { it.name.trim().equals(doc.trim(), ignoreCase = true) }
                                    if (matchedDoc != null && matchedDoc.specialty.isNotBlank()) {
                                        if (specialtiesList.contains(matchedDoc.specialty)) {
                                            selectedSpecialty = matchedDoc.specialty
                                            customSpecialty = ""
                                        } else {
                                            selectedSpecialty = "Sonstige"
                                            customSpecialty = matchedDoc.specialty
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Specialty Selector (Dropdown)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedSpecialty,
                    onValueChange = {},
                    label = { Text("Fachrichtung") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedSpecialtyDropdown = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Auswählen")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedSpecialtyDropdown,
                    onDismissRequest = { expandedSpecialtyDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    specialtiesList.forEach { spec ->
                        DropdownMenuItem(
                            text = { Text(spec) },
                            onClick = {
                                selectedSpecialty = spec
                                expandedSpecialtyDropdown = false
                            }
                        )
                    }
                }
            }

            // Custom specialty field (if "Sonstige" is selected)
            if (selectedSpecialty == "Sonstige") {
                OutlinedTextField(
                    value = customSpecialty,
                    onValueChange = { customSpecialty = it },
                    label = { Text("Eigene Fachrichtung eingeben") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker Button
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Datum", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formattedDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Time Picker Button
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Uhrzeit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$formattedTime Uhr", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notizen / Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Erinnerung aktivieren", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Erinnert dich rechtzeitig an diesen Termin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // File Attachments Section
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Dokumente & Arztbriefe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // List of Saved Attachments (from DB)
            if (savedAttachments.isNotEmpty()) {
                Text("Gespeicherte Dokumente:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                savedAttachments.forEach { attachment ->
                    AttachmentRow(
                        name = attachment.displayName,
                        onOpen = { openAttachment(context, attachment.filePath) { previewImagePath = it } },
                        onDelete = { viewModel.deleteAttachment(attachment) }
                    )
                }
            }

            // List of Newly Picked Attachments (to be saved)
            if (newAttachments.isNotEmpty()) {
                Text("Neu hinzuzufügen:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                newAttachments.forEach { (path, name) ->
                    AttachmentRow(
                        name = name,
                        onOpen = { openAttachment(context, path) { previewImagePath = it } },
                        onDelete = {
                            newAttachments.remove(Pair(path, name))
                            try {
                                val file = File(path)
                                if (file.exists()) file.delete()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            }

            // Attachment Add Button
            OutlinedButton(
                onClick = { filePickerLauncher.launch(arrayOf("image/*", "application/pdf")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dokument / Bild anhängen")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isSaveEnabled) {
                        viewModel.saveAppointment(
                            id = if (copy) 0 else (appointmentId ?: 0),
                            personName = selectedPerson,
                            title = title,
                            doctor = doctor,
                            specialty = finalSpecialty,
                            dateMillis = selectedDateMillis,
                            notes = notes,
                            reminderEnabled = reminderEnabled,
                            isCompleted = if (copy) false else (existingAppointment?.isCompleted ?: false),
                            newAttachments = newAttachments
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isSaveEnabled
            ) {
                Text("Termin speichern")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Image Preview Dialog
        previewImagePath?.let { path ->
            val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
            AlertDialog(
                onDismissRequest = { previewImagePath = null },
                confirmButton = {
                    TextButton(onClick = { previewImagePath = null }) {
                        Text("Schließen")
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dokumentenvorschau", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { previewImagePath = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Schließen")
                        }
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Vorschau konnte nicht geladen werden.")
                        }
                    }
                }
            )
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let {
                            selectedDateMillis = it
                            val tempCal = Calendar.getInstance().apply { timeInMillis = it }
                            calendar.set(Calendar.YEAR, tempCal.get(Calendar.YEAR))
                            calendar.set(Calendar.DAY_OF_YEAR, tempCal.get(Calendar.DAY_OF_YEAR))
                            selectedDateMillis = calendar.timeInMillis
                        }
                        showDatePicker = false
                    }) {
                        Text("Auswählen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Abbrechen")
                    }
                }
            ) {
                DatePicker(state = dateState)
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        calendar.set(Calendar.MINUTE, timeState.minute)
                        selectedDateMillis = calendar.timeInMillis
                        showTimePicker = false
                    }) {
                        Text("Auswählen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Abbrechen")
                    }
                },
                title = { Text("Uhrzeit wählen") },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(state = timeState)
                    }
                }
            )
        }
    }
}

@Composable
fun AttachmentRow(
    name: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Helper to copy selected file to App's Internal Storage
fun copyUriToInternalStorage(context: Context, uri: Uri): Pair<String, String>? {
    val contentResolver = context.contentResolver
    var fileName = "document_${System.currentTimeMillis()}"
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use { c ->
        if (c.moveToFirst()) {
            val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = c.getString(nameIndex)
            }
        }
    }
    val attachmentsDir = File(context.filesDir, "attachments").apply { mkdirs() }
    val destFile = File(attachmentsDir, "${System.currentTimeMillis()}_$fileName")
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Pair(destFile.absolutePath, fileName)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper to open/view files (Images in Dialog, PDFs via chooser intent)
fun openAttachment(context: Context, filePath: String, onShowImagePreview: (String) -> Unit) {
    val file = File(filePath)
    if (!file.exists()) return

    val isImage = filePath.endsWith(".jpg", ignoreCase = true) ||
                  filePath.endsWith(".jpeg", ignoreCase = true) ||
                  filePath.endsWith(".png", ignoreCase = true)

    if (isImage) {
        onShowImagePreview(filePath)
    } else {
        // Open PDF or other document via external App chooser Intent
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "com.meddiary.fileprovider",
                file
            )
            val mimeType = if (filePath.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Datei öffnen"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
