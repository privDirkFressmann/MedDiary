package com.meddiary.ui.screens

import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import com.meddiary.data.FamilyMember
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.components.AppointmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MedicalViewModel,
    onNavigateToAddAppointment: (Int?, Boolean) -> Unit,
    onNavigateToCheckups: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToVaccinations: () -> Unit
) {
    val context = LocalContext.current
    val selectedPerson by viewModel.selectedPerson.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsState()
    val checkups by viewModel.allCheckups.collectAsState()
    
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showEditPersonDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }
    var expandedPersonDropdown by remember { mutableStateOf(false) }

    val personUpcomingAppointments = remember(upcomingAppointments, selectedPerson) {
        upcomingAppointments.filter { it.personName == selectedPerson }
    }

    val personCheckups = remember(checkups, selectedPerson) {
        checkups.filter { it.personName == selectedPerson }
    }

    val enabledCheckups = remember(personCheckups) {
        personCheckups.filter { it.isEnabled }
    }

    val overdueCheckupsCount = remember(enabledCheckups) {
        enabledCheckups.count { checkup ->
            checkup.nextDueMillis?.let { it < System.currentTimeMillis() } ?: false
        }
    }

    val openCheckupsCount = remember(enabledCheckups) {
        enabledCheckups.count { it.lastDoneMillis == null }
    }

    // Backup Export File Launcher
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            viewModel.exportBackup(context, it) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, "Backup erfolgreich exportiert!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Backup-Export fehlgeschlagen: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Backup Import File Launcher
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importBackup(context, it) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, "Daten erfolgreich wiederhergestellt!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Import fehlgeschlagen: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MedDiary",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Gesundheitstagebuch für $selectedPerson",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    // Profile Switcher & Backup Icon Menu
                    Box {
                        IconButton(onClick = { expandedPersonDropdown = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profil wechseln / Backup",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expandedPersonDropdown,
                            onDismissRequest = { expandedPersonDropdown = false }
                        ) {
                             // Family Members List
                             familyMembers.forEach { member ->
                                 DropdownMenuItem(
                                     text = { Text(member.name + " (${member.relation}) bearbeiten") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Edit,
                                             contentDescription = null,
                                             modifier = Modifier.size(18.dp)
                                         )
                                     },
                                     onClick = {
                                         editingMember = member
                                         showEditPersonDialog = true
                                         expandedPersonDropdown = false
                                     }
                                 )
                             }
                             HorizontalDivider()
                             DropdownMenuItem(
                                 text = {
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text("Mitglied hinzufügen")
                                     }
                                 },
                                 onClick = {
                                     showAddPersonDialog = true
                                     expandedPersonDropdown = false
                                 }
                             )
                             HorizontalDivider()
                             DropdownMenuItem(
                                 text = {
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text("Backup exportieren (ZIP)")
                                     }
                                 },
                                 onClick = {
                                     exportBackupLauncher.launch("MedDiary_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.zip")
                                     expandedPersonDropdown = false
                                 }
                             )
                             DropdownMenuItem(
                                 text = {
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text("Backup importieren (ZIP)")
                                     }
                                 },
                                 onClick = {
                                     importBackupLauncher.launch(arrayOf("application/zip"))
                                     expandedPersonDropdown = false
                                 }
                             )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddAppointment(null, false) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Termin hinzufügen")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal User Profile Selection Bar
            Box(
                modifier = Modifier.fillMaxWidth(),
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Dashboard Status Summary / Gradient Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Hallo $selectedPerson!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (overdueCheckupsCount > 0) {
                                "Du hast $overdueCheckupsCount überfällige Vorsorgeuntersuchungen. Nimm dir kurz Zeit für deine Gesundheit!"
                            } else if (openCheckupsCount > 0) {
                                "Du hast $openCheckupsCount ausstehende Vorsorgeuntersuchungen. Nimm dir kurz Zeit für deine Gesundheit!"
                            } else {
                                "Alle deine Vorsorgeuntersuchungen sind aktuell. Gut gemacht!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Quick Navigation shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onNavigateToCheckups() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vorsorge",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Checkups",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onNavigateToVaccinations() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Impfpass",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "STIKO-Schutz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onNavigateToCalendar() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Historie",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kalender",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section: Anstehende Termine
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anstehende Termine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    TextButton(onClick = onNavigateToCalendar) {
                        Text("Alle anzeigen")
                    }
                }
            }

            if (personUpcomingAppointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Keine anstehenden Termine für $selectedPerson",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToAddAppointment(null, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Jetzt eintragen")
                                }
                            }
                        }
                    }
                }
            } else {
                items(personUpcomingAppointments.take(3)) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCheckedChange = { viewModel.toggleAppointmentCompleted(appointment) },
                        onEditClick = { onNavigateToAddAppointment(appointment.id, false) },
                        onCopyClick = { onNavigateToAddAppointment(appointment.id, true) },
                        onDeleteClick = { viewModel.deleteAppointment(appointment) }
                    )
                }
            }
        }
    }
}

    // Add Family Member Dialog
    if (showAddPersonDialog) {
        var newName by remember { mutableStateOf("") }
        var relationKind by remember { mutableStateOf(true) } // true for child, false for adult
        var birthYearStr by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
        var selectedGender by remember { mutableStateOf("M") } // "M", "F"

        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("Familienmitglied hinzufügen") },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val relation = if (relationKind) "Kind" else "Partner"
                            val birthYear = birthYearStr.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                            viewModel.addFamilyMember(newName.trim(), relation, birthYear, selectedGender)
                            viewModel.selectPerson(newName.trim())
                            showAddPersonDialog = false
                        }
                    },
                    enabled = newName.isNotBlank() && birthYearStr.isNotBlank()
                ) {
                    Text("Anlegen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) {
                    Text("Abbrechen")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name (z.B. Leo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = birthYearStr,
                        onValueChange = { birthYearStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Geburtsjahr") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Geschlecht:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedGender == "M", onClick = { selectedGender = "M" })
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Männlich", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedGender == "F", onClick = { selectedGender = "F" })
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Weiblich", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Profil-Typ:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = relationKind,
                                onClick = { relationKind = true }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Kind", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Lädt U-Untersuchungen (Kinderheilkunde)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !relationKind,
                                onClick = { relationKind = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Erwachsener", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Lädt Standard-Vorsorgeuntersuchungen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        )
    }

    // Edit Family Member Dialog
    if (showEditPersonDialog && editingMember != null) {
        val member = editingMember!!
        var relationKind by remember(member) { mutableStateOf(member.relation == "Kind") }
        var birthYearStr by remember(member) { mutableStateOf(member.birthYear.toString()) }
        var selectedGender by remember(member) { mutableStateOf(if (member.gender == "ALL") "M" else member.gender) }

        Dialog(onDismissRequest = { showEditPersonDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Familienmitglied bearbeiten",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = member.name,
                            onValueChange = {},
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false
                        )

                        OutlinedTextField(
                            value = birthYearStr,
                            onValueChange = { birthYearStr = it.filter { char -> char.isDigit() } },
                            label = { Text("Geburtsjahr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Geschlecht:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedGender == "M", onClick = { selectedGender = "M" })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Männlich", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedGender == "F", onClick = { selectedGender = "F" })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Weiblich", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        if (member.relation != "Ich") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Profil-Typ:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = relationKind,
                                        onClick = { relationKind = true }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Kind", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Lädt U-Untersuchungen (Kinderheilkunde)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !relationKind,
                                        onClick = { relationKind = false }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Erwachsener (Partner)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Lädt Standard-Vorsorgeuntersuchungen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (familyMembers.size > 1) {
                            Button(
                                onClick = {
                                    if (selectedPerson == member.name) {
                                        val nextPerson = familyMembers.firstOrNull { it.name != member.name }?.name ?: ""
                                        viewModel.selectPerson(nextPerson)
                                    }
                                    viewModel.deleteFamilyMember(member)
                                    showEditPersonDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Profil löschen")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditPersonDialog = false }) {
                                Text("Abbrechen")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val relation = if (relationKind) "Kind" else if (member.relation == "Ich") "Ich" else "Partner"
                                    val birthYear = birthYearStr.toIntOrNull() ?: member.birthYear
                                    viewModel.updateFamilyMember(
                                        FamilyMember(
                                            name = member.name,
                                            relation = relation,
                                            birthYear = birthYear,
                                            gender = selectedGender
                                        )
                                    )
                                    showEditPersonDialog = false
                                },
                                enabled = birthYearStr.isNotBlank()
                            ) {
                                Text("Speichern")
                            }
                        }
                    }
                }
            }
        }
    }
}
