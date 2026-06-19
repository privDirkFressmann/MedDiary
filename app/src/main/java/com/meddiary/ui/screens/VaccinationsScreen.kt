package com.meddiary.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.meddiary.ui.MedicalViewModel
import com.meddiary.data.Vaccination
import com.meddiary.data.FamilyMember
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationsScreen(
    viewModel: MedicalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val vaccinations by viewModel.allVaccinations.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val selectedPerson by viewModel.selectedPerson.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Meine Impfungen, 1: STIKO-Empfehlungen
    val tabs = listOf("Meine Impfungen", "STIKO-Empfehlungen")

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    // Filter vaccinations for selected person
    val personVaccinations = remember(vaccinations, selectedPerson) {
        vaccinations.filter { it.personName == selectedPerson }
    }

    val activeMember = remember(familyMembers, selectedPerson) {
        familyMembers.firstOrNull { it.name == selectedPerson }
    }

    var copyFromVaccination by remember { mutableStateOf<Vaccination?>(null) }
    var editVaccination by remember { mutableStateOf<Vaccination?>(null) }

    // Date Picker State for Add Dialog
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digitaler Impfpass", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Impfung eintragen")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal User Profile Selection Bar (consistent with HomeScreen)
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    familyMembers.forEach { member ->
                        val isSelected = member.name == selectedPerson
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectPerson(member.name) },
                            label = { Text(member.name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> LoggedVaccinationsList(
                    vaccinations = personVaccinations,
                    dateFormatter = dateFormatter,
                    onCopyVaccination = { vac ->
                        copyFromVaccination = vac
                        showAddDialog = true
                    },
                    onEditVaccination = { vac ->
                        editVaccination = vac
                        showAddDialog = true
                    },
                    onDeleteVaccination = { viewModel.deleteVaccination(it) }
                )
                1 -> StikoRecommendationsList(
                    vaccinations = personVaccinations,
                    member = activeMember,
                    dateFormatter = dateFormatter,
                    onAddVaccinationShortcut = { title ->
                        // Pre-populate add dialog or quick add
                        viewModel.addVaccination(selectedPerson, title, System.currentTimeMillis())
                        Toast.makeText(context, "$title eingetragen!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Add/Edit/Copy Vaccination Dialog
        if (showAddDialog) {
            val initialDate = editVaccination?.dateMillis ?: copyFromVaccination?.dateMillis ?: System.currentTimeMillis()
            val dateState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
            AddVaccinationDialog(
                initialTitle = editVaccination?.title ?: copyFromVaccination?.title ?: "",
                initialBatch = editVaccination?.batchNumber ?: copyFromVaccination?.batchNumber ?: "",
                initialDoctor = editVaccination?.doctorName ?: copyFromVaccination?.doctorName ?: "",
                initialNotes = editVaccination?.notes ?: copyFromVaccination?.notes ?: "",
                isEditMode = editVaccination != null,
                onDismiss = { 
                    showAddDialog = false
                    copyFromVaccination = null
                    editVaccination = null
                },
                onConfirm = { title, dateMillis, batch, doctor, notes ->
                    viewModel.addVaccination(
                        personName = selectedPerson,
                        title = title,
                        dateMillis = dateMillis,
                        batchNumber = batch,
                        doctorName = doctor,
                        notes = notes,
                        id = editVaccination?.id ?: 0
                    )
                    showAddDialog = false
                    val isEdit = editVaccination != null
                    copyFromVaccination = null
                    editVaccination = null
                    val msg = if (isEdit) "Impfung erfolgreich aktualisiert!" else "Impfung erfolgreich erfasst!"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                dateState = dateState,
                showDatePicker = showDatePicker,
                onShowDatePickerChange = { showDatePicker = it }
            )
        }
    }
}

@Composable
fun LoggedVaccinationsList(
    vaccinations: List<Vaccination>,
    dateFormatter: SimpleDateFormat,
    onCopyVaccination: (Vaccination) -> Unit,
    onEditVaccination: (Vaccination) -> Unit,
    onDeleteVaccination: (Vaccination) -> Unit
) {
    if (vaccinations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Noch keine Impfungen eingetragen.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trage verabreichte Impfungen über den '+' Button unten rechts ein.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(vaccinations, key = { it.id }) { vaccination ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditVaccination(vaccination) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vaccination.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Geimpft am: ${dateFormatter.format(Date(vaccination.dateMillis))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (vaccination.doctorName.isNotBlank()) {
                                Text(
                                    text = "Arzt/Praxis: ${vaccination.doctorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (vaccination.batchNumber.isNotBlank()) {
                                Text(
                                    text = "Charge: ${vaccination.batchNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (vaccination.notes.isNotBlank()) {
                                Text(
                                    text = "Notiz: ${vaccination.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onCopyVaccination(vaccination) }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Eintrag kopieren",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDeleteVaccination(vaccination) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eintrag löschen",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data holder class for STIKO recommendations assessment
data class StikoRecommendation(
    val title: String,
    val description: String,
    val ageGroupInfo: String,
    val intervalYears: Int?, // null if one-time
    val genderTarget: String = "ALL", // "ALL", "M", "F"
    val minAgeYears: Int = 0,
    val maxAgeYears: Int = 150
)

@Composable
fun StikoRecommendationsList(
    vaccinations: List<Vaccination>,
    member: FamilyMember?,
    dateFormatter: SimpleDateFormat,
    onAddVaccinationShortcut: (String) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val age = member?.let { currentYear - it.birthYear } ?: 30 // Fallback to 30 years if not set
    val gender = member?.gender ?: "ALL"
    val isChild = member?.relation == "Kind"

    val recommendations = remember(isChild, age, gender) {
        listOf(
            StikoRecommendation(
                title = "Tetanus (Wundstarrkrampf)",
                description = "Schützt vor lebensgefährlichen Wundinfektionen durch Sporen im Erdreich.",
                ageGroupInfo = "Alle Altersgruppen (Auffrischung alle 10 Jahre)",
                intervalYears = 10
            ),
            StikoRecommendation(
                title = "Diphtherie",
                description = "Schützt vor einer schweren bakteriellen Atemwegsinfektion (Krupp).",
                ageGroupInfo = "Alle Altersgruppen (Auffrischung alle 10 Jahre)",
                intervalYears = 10
            ),
            StikoRecommendation(
                title = "Pertussis (Keuchhusten)",
                description = "Schützt vor heftigen Hustenkrämpfen. Wichtig für Schwangere, junge Eltern und als regelmäßige Auffrischung.",
                ageGroupInfo = "Alle Altersgruppen (Auffrischung alle 10 Jahre, meist kombiniert)",
                intervalYears = 10
            ),
            StikoRecommendation(
                title = "Poliomyelitis (Kinderlähmung)",
                description = "Schützt vor spinaler Kinderlähmung. STIKO empfiehlt Auffrischung bei unvollständiger Grundimmunisierung.",
                ageGroupInfo = "Grundimmunisierung im Kindesalter, Auffrischung bei Bedarf",
                intervalYears = null
            ),
            StikoRecommendation(
                title = "MMR (Masern, Mumps, Röteln)",
                description = "Einmalige Schutzimpfung für alle nach 1970 geborenen Erwachsenen mit unklarem oder unvollständigem Impfstatus.",
                ageGroupInfo = "Einmalig für nach 1970 Geborene bzw. im Kleinkindalter",
                intervalYears = null,
                minAgeYears = 1
            ),
            StikoRecommendation(
                title = "FSME (Zeckenschutz)",
                description = "Schützt vor Gehirn- und Hirnhautentzündung durch Zeckenstiche in Risikogebieten.",
                ageGroupInfo = "Für Bewohner und Reisende in Risikogebieten (Auffrischung alle 3-5 Jahre)",
                intervalYears = 3
            ),
            StikoRecommendation(
                title = "Influenza (Grippe)",
                description = "Jährliche Schutzimpfung im Herbst gegen die echte Virusgrippe.",
                ageGroupInfo = "Jährlich empfohlen ab 60 Jahren, chronisch Kranken und Schwangeren",
                intervalYears = 1,
                minAgeYears = if (age >= 60) 0 else 60 // Highlight if 60+
            ),
            StikoRecommendation(
                title = "Pneumokokken",
                description = "Schützt vor Lungen-, Hirnhaut- und Mittelohrentzündungen ausgelöst durch Pneumokokken.",
                ageGroupInfo = "Empfohlen ab 60 Jahren bzw. bei chronischen Vorerkrankungen",
                intervalYears = null,
                minAgeYears = if (age >= 60) 0 else 60
            ),
            StikoRecommendation(
                title = "Herpes Zoster (Gürtelrose)",
                description = "Schützt vor der schmerzhaften Gürtelrose und langanhaltenden Nervenschmerzen.",
                ageGroupInfo = "Empfohlen ab 60 Jahren (bzw. ab 50 Jahren bei chronischer Grunderkrankung)",
                intervalYears = null,
                minAgeYears = if (age >= 60) 0 else 50
            ),
            StikoRecommendation(
                title = "HPV (Humane Papillomviren)",
                description = "Schützt vor Krebsvorstufen am Gebärmutterhals sowie anderen Krebsarten bei Jungen und Mädchen.",
                ageGroupInfo = "Empfohlen für Kinder/Jugendliche zwischen 9 und 14 Jahren",
                intervalYears = null,
                minAgeYears = 9,
                maxAgeYears = 17
            )
        ).filter { rec ->
            // Filter by gender target
            (rec.genderTarget == "ALL" || rec.genderTarget.equals(gender, ignoreCase = true)) &&
            // Filter child vs adult specific vaccines roughly
            if (isChild) {
                rec.title.contains("HPV") || rec.title.contains("Tetanus") || rec.title.contains("Diphtherie") || rec.title.contains("Pertussis") || rec.title.contains("Poliomyelitis") || rec.title.contains("MMR") || rec.title.contains("FSME")
            } else {
                // For adults, show relevant
                !rec.title.contains("HPV") || age <= 20
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Person context banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profil-Kontext für Empfehlungen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Name: ${member?.name ?: "Unbekannt"} | Typ: ${if (isChild) "Kind" else "Erwachsener"} (ca. $age Jahre) | Geschlecht: ${if (gender == "M") "Männlich" else if (gender == "F") "Weiblich" else "Divers/Allgemein"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        items(recommendations) { rec ->
            // Find if there is a recorded vaccination of this type
            val matches = vaccinations.filter { it.title.contains(rec.title.split(" ")[0], ignoreCase = true) }
            val lastVaccination = matches.maxByOrNull { it.dateMillis }

            // Determine status
            val status: VaccinationStatus = when {
                lastVaccination == null -> {
                    // Check if they are in the target group or if it's general
                    if (isChild && rec.title.contains("HPV") && (age < 9 || age > 14)) {
                        VaccinationStatus.NotYetRecommended
                    } else if (!isChild && (rec.title.contains("Herpes") || rec.title.contains("Pneumokokken") || rec.title.contains("Influenza")) && age < 60) {
                        VaccinationStatus.RecommendedLater("Empfohlen ab 60 Jahren")
                    } else {
                        VaccinationStatus.Missing
                    }
                }
                rec.intervalYears != null -> {
                    // Check if it's expired
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = lastVaccination.dateMillis
                        add(Calendar.YEAR, rec.intervalYears)
                    }
                    if (cal.timeInMillis < System.currentTimeMillis()) {
                        VaccinationStatus.Overdue(lastVaccination.dateMillis)
                    } else {
                        VaccinationStatus.UpToDate(lastVaccination.dateMillis, cal.timeInMillis)
                    }
                }
                else -> {
                    // One-time vaccine, and they have it
                    VaccinationStatus.UpToDate(lastVaccination.dateMillis, null)
                }
            }

            RecommendationCard(
                rec = rec,
                status = status,
                dateFormatter = dateFormatter,
                onQuickAdd = { onAddVaccinationShortcut(rec.title) }
            )
        }
    }
}

sealed class VaccinationStatus {
    object Missing : VaccinationStatus()
    data class UpToDate(val lastDoneMillis: Long, val nextDueMillis: Long?) : VaccinationStatus()
    data class Overdue(val lastDoneMillis: Long) : VaccinationStatus()
    data class RecommendedLater(val info: String) : VaccinationStatus()
    object NotYetRecommended : VaccinationStatus()
}

@Composable
fun RecommendationCard(
    rec: StikoRecommendation,
    status: VaccinationStatus,
    dateFormatter: SimpleDateFormat,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rec.ageGroupInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                StatusBadge(status = status, dateFormatter = dateFormatter)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rec.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )

            if (status is VaccinationStatus.Missing || status is VaccinationStatus.Overdue) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onQuickAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jetzt eintragen (Heute)")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: VaccinationStatus, dateFormatter: SimpleDateFormat) {
    val (text, color, icon) = when (status) {
        is VaccinationStatus.Missing -> Triple("Fehlt", Color(0xFFE57373), Icons.Default.Error)
        is VaccinationStatus.UpToDate -> Triple("Aktuell", Color(0xFF81C784), Icons.Default.CheckCircle)
        is VaccinationStatus.Overdue -> Triple("Fällig", Color(0xFFFFB74D), Icons.Default.Warning)
        is VaccinationStatus.RecommendedLater -> Triple("Später empfohlen", Color(0xFF90A4AE), Icons.Default.Info)
        is VaccinationStatus.NotYetRecommended -> Triple("Derzeit nicht fällig", Color(0xFFB0BEC5), Icons.Default.Info)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                if (status is VaccinationStatus.UpToDate && status.nextDueMillis != null) {
                    Text(
                        text = "Fällig: ${dateFormatter.format(Date(status.nextDueMillis))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
                if (status is VaccinationStatus.Overdue) {
                    Text(
                        text = "Überfällig",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationDialog(
    initialTitle: String = "",
    initialBatch: String = "",
    initialDoctor: String = "",
    initialNotes: String = "",
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, String, String) -> Unit,
    dateState: DatePickerState,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var batch by remember(initialBatch) { mutableStateOf(initialBatch) }
    var doctor by remember(initialDoctor) { mutableStateOf(initialDoctor) }
    var notes by remember(initialNotes) { mutableStateOf(initialNotes) }

    val formatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val dateText = remember(dateState.selectedDateMillis) {
        dateState.selectedDateMillis?.let { formatter.format(Date(it)) } ?: "Bitte Datum wählen"
    }

    var expandedDropdown by remember { mutableStateOf(false) }
    val presetVaccines = listOf(
        "Tetanus", "Diphtherie", "Pertussis", "Poliomyelitis", "MMR (Masern, Mumps, Röteln)",
        "FSME (Zecken)", "Influenza (Grippe)", "Pneumokokken", "Herpes Zoster (Gürtelrose)",
        "HPV", "Hepatitis B", "Windpocken", "Meningokokken", "COVID-19", "Sonstige"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp)
                .imePadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditMode) "Impfung bearbeiten" else "Impfung erfassen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Vaccine selection (Dropdown + Input)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Impfstoff / Krankheit (z.B. Tetanus)") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedDropdown = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Vorschläge anzeigen")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        presetVaccines.forEach { vaccine ->
                            val isSelected = remember(title) {
                                if (vaccine == "Sonstige") false else title.split(",").map { it.trim() }.contains(vaccine)
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (vaccine != "Sonstige") {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(vaccine)
                                    }
                                },
                                onClick = {
                                    if (vaccine != "Sonstige") {
                                        val currentTitle = title.trim()
                                        val parts = if (currentTitle.isEmpty()) emptyList<String>() else currentTitle.split(",").map { it.trim() }
                                        val newParts = parts.toMutableList()
                                        if (newParts.contains(vaccine)) {
                                            newParts.remove(vaccine)
                                        } else {
                                            newParts.add(vaccine)
                                        }
                                        title = newParts.joinToString(", ")
                                    } else {
                                        expandedDropdown = false
                                    }
                                }
                            )
                        }
                    }
                }

                // Date Picker trigger button
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowDatePickerChange(true) },
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Impfdatum", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Datum auswählen")
                    }
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { onShowDatePickerChange(false) },
                        confirmButton = {
                            TextButton(onClick = { onShowDatePickerChange(false) }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { onShowDatePickerChange(false) }) {
                                Text("Abbrechen")
                            }
                        }
                    ) {
                        DatePicker(state = dateState)
                    }
                }

                // Doctor name
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Arzt / Praxis") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Batch Number (Chargennummer)
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Chargennummer (z.B. LJ832)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen / Nebenwirkungen") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dateMillis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                                onConfirm(title.trim(), dateMillis, batch.trim(), doctor.trim(), notes.trim())
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}
