package com.meddiary.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.meddiary.data.Doctor
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.components.DoctorDetailsDialog

private val specialtiesList = listOf(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    viewModel: MedicalViewModel,
    doctorId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val doctors by viewModel.allDoctors.collectAsState()
    val context = LocalContext.current

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingDoctor by remember { mutableStateOf<Doctor?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var detailsDoctor by remember { mutableStateOf<Doctor?>(null) }

    LaunchedEffect(doctors, doctorId) {
        if (doctorId != null && doctors.isNotEmpty()) {
            val doc = doctors.firstOrNull { it.id == doctorId }
            if (doc != null) {
                editingDoctor = doc
                showAddEditDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ärzte-Datenbank") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingDoctor = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Arzt hinzufügen")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (doctors.isEmpty()) {
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
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Noch keine Ärzte eingetragen.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Füge deine behandelnden Ärzte oder Praxen hinzu, um sie bei Terminen und Impfungen einfach auszuwählen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                editingDoctor = null
                                showAddEditDialog = true
                            }
                        ) {
                            Text("Arzt hinzufügen")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(doctors) { doctor ->
                        DoctorCard(
                            doctor = doctor,
                            onEditClick = {
                                editingDoctor = doctor
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteDoctor(doctor)
                                Toast.makeText(context, "${doctor.name} gelöscht", Toast.LENGTH_SHORT).show()
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
            }
        }
    }

    if (showAddEditDialog) {
        val doctor = editingDoctor
        var name by remember { mutableStateOf(doctor?.name ?: "") }
        var selectedSpecialty by remember {
            mutableStateOf(
                if (doctor == null) {
                    "Allgemeinmedizin (Hausarzt)"
                } else if (specialtiesList.contains(doctor.specialty)) {
                    doctor.specialty
                } else if (doctor.specialty.isBlank()) {
                    "Allgemeinmedizin (Hausarzt)"
                } else {
                    "Sonstige"
                }
            )
        }
        var customSpecialty by remember {
            mutableStateOf(
                if (doctor != null && !specialtiesList.contains(doctor.specialty) && doctor.specialty.isNotBlank()) {
                    doctor.specialty
                } else {
                    ""
                }
            )
        }
        var expandedSpecialtyDropdown by remember { mutableStateOf(false) }
        var address by remember { mutableStateOf(doctor?.address ?: "") }
        var phoneNumber by remember { mutableStateOf(doctor?.phoneNumber ?: "") }

        val finalSpecialty = if (selectedSpecialty == "Sonstige") customSpecialty.trim() else selectedSpecialty

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (doctor == null) "Arzt hinzufügen" else "Arzt bearbeiten") },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveDoctor(
                                id = doctor?.id ?: 0,
                                name = name.trim(),
                                address = address.trim(),
                                phoneNumber = phoneNumber.trim(),
                                specialty = finalSpecialty
                            )
                            showAddEditDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Abbrechen")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (z.B. Dr. Müller)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

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

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Adresse (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Telefonnummer (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        )
    }

    if (showDetailsDialog) {
        val doctor = detailsDoctor
        if (doctor != null) {
            DoctorDetailsDialog(
                doctor = doctor,
                onDismiss = { showDetailsDialog = false },
                onEditClick = {
                    showDetailsDialog = false
                    editingDoctor = doctor
                    showAddEditDialog = true
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
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onMapClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (doctor.specialty.isNotBlank()) {
                        Text(
                            text = doctor.specialty,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Bearbeiten",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Löschen",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (doctor.address.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Adresse",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = doctor.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onMapClick(doctor.address) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "In Karte öffnen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (doctor.phoneNumber.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Telefon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = doctor.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onCallClick(doctor.phoneNumber) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Anrufen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
