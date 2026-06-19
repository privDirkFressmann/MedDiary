package com.meddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.components.AppointmentCard
import com.meddiary.ui.theme.AccentBlue
import com.meddiary.ui.theme.CoralAlert
import com.meddiary.data.Doctor
import com.meddiary.ui.components.DoctorDetailsDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MedicalViewModel,
    onNavigateToAddAppointment: (Int?, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToDoctors: (Int?) -> Unit
) {
    val context = LocalContext.current
    val doctors by viewModel.allDoctors.collectAsState()
    var activeDoctorDetails by remember { mutableStateOf<Doctor?>(null) }
    val familyMembers by viewModel.familyMembers.collectAsState()
    val appointments by viewModel.allAppointments.collectAsState()

    var selectedPersonFilter by remember { mutableStateOf("Alle") }
    var selectedSpecialtyFilter by remember { mutableStateOf("Alle") }

    val availablePersons = remember(familyMembers) {
        val names = familyMembers.map { it.name }.toMutableList()
        if (names.contains("Dirk")) {
            names.remove("Dirk")
            names.sort()
            listOf("Alle", "Dirk") + names
        } else {
            names.sort()
            listOf("Alle") + names
        }
    }

    val personFilteredAppointments = remember(appointments, selectedPersonFilter) {
        if (selectedPersonFilter == "Alle") {
            appointments
        } else {
            appointments.filter { it.personName == selectedPersonFilter }
        }
    }

    val availableSpecialties = remember(personFilteredAppointments) {
        val specialties = personFilteredAppointments.map { it.specialty }.filter { it.isNotBlank() }.distinct().sorted()
        listOf("Alle") + specialties
    }

    LaunchedEffect(availableSpecialties) {
        if (selectedSpecialtyFilter != "Alle" && !availableSpecialties.contains(selectedSpecialtyFilter)) {
            selectedSpecialtyFilter = "Alle"
        }
    }

    val filteredAppointments = remember(personFilteredAppointments, selectedSpecialtyFilter) {
        if (selectedSpecialtyFilter == "Alle") {
            personFilteredAppointments
        } else {
            personFilteredAppointments.filter { it.specialty == selectedSpecialtyFilter }
        }
    }

    val sortedAppointments = remember(filteredAppointments) {
        filteredAppointments.sortedByDescending { it.dateMillis }
    }

    val groupedAppointments = remember(sortedAppointments) {
        sortedAppointments.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            SimpleDateFormat("MMMM yyyy", Locale.GERMAN).format(cal.time)
        }
    }

    val currentTimeMillis = remember { System.currentTimeMillis() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arztbesuche-Historie") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter section
            if (appointments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = androidx.compose.ui.graphics.RectangleShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // User filter row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mitglied:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(90.dp)
                            )
                            availablePersons.forEach { person ->
                                FilterChip(
                                    selected = selectedPersonFilter == person,
                                    onClick = { selectedPersonFilter = person },
                                    label = { Text(person) }
                                )
                            }
                        }

                        // Specialty filter row
                        if (availableSpecialties.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fachrichtung:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(90.dp)
                                )
                                availableSpecialties.forEach { spec ->
                                    FilterChip(
                                        selected = selectedSpecialtyFilter == spec,
                                        onClick = { selectedSpecialtyFilter = spec },
                                        label = { Text(spec) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Keine Arztbesuche eingetragen.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { onNavigateToAddAppointment(null, false) }
                        ) {
                            Text("Arztbesuch hinzufügen")
                        }
                    }
                }
            } else if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine Einträge für die ausgewählten Filter vorhanden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedAppointments.forEach { (monthYear, apptsInMonth) ->
                        item {
                            Text(
                                text = monthYear,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(apptsInMonth, key = { it.id }) { appointment ->
                            val isPast = appointment.dateMillis < currentTimeMillis
                            val color = if (isPast) AccentBlue else CoralAlert
                            AppointmentCard(
                                appointment = appointment,
                                showCheckbox = false,
                                useStrikethrough = false,
                                indicatorColor = color,
                                onEditClick = { onNavigateToAddAppointment(appointment.id, false) },
                                onCopyClick = { onNavigateToAddAppointment(appointment.id, true) },
                                onDeleteClick = { viewModel.deleteAppointment(appointment) },
                                onDoctorClick = { docId ->
                                    val doc = doctors.firstOrNull { it.id == docId }
                                    if (doc != null) {
                                        activeDoctorDetails = doc
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (activeDoctorDetails != null) {
        DoctorDetailsDialog(
            doctor = activeDoctorDetails!!,
            onDismiss = { activeDoctorDetails = null },
            onEditClick = {
                val docId = activeDoctorDetails?.id
                activeDoctorDetails = null
                onNavigateToDoctors(docId)
            },
            onCallClick = { phone ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Anruf-App konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
                }
            },
            onMapClick = { address ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Karten-App konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
