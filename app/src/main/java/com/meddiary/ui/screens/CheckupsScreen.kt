package com.meddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.components.CheckupCard
import com.meddiary.data.Checkup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckupsScreen(
    viewModel: MedicalViewModel,
    onNavigateBack: () -> Unit
) {
    val checkups by viewModel.allCheckups.collectAsState()
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
    
    val selectedPerson by viewModel.selectedPerson.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var checkupToMarkDone: Checkup? by remember { mutableStateOf(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var selectedCategoryTab by remember { mutableStateOf("Alle") }
    val categories = listOf("Alle", "Zahnmedizin", "Allgemeinmedizin", "Krebsvorsorge", "Kinderheilkunde", "Impfung")
    
    val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var expandedPersonDropdown by remember { mutableStateOf(false) }

    val filteredCheckups = remember(checkups, selectedCategoryTab, selectedPerson) {
        val personCheckups = checkups.filter { it.personName == selectedPerson }
        val categoryFiltered = if (selectedCategoryTab == "Alle") {
            personCheckups
        } else if (selectedCategoryTab == "Krebsvorsorge") {
            personCheckups.filter { it.category.contains("krebs", ignoreCase = true) || it.category.contains("screening", ignoreCase = true) }
        } else {
            personCheckups.filter { it.category.contains(selectedCategoryTab, ignoreCase = true) }
        }
        categoryFiltered.sortedByDescending { it.isEnabled }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vorsorge & Check-ups") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Eigene Vorsorge hinzufügen")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Person Selector & Add Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Dropdown selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expandedPersonDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Profil: $selectedPerson")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedPersonDropdown,
                            onDismissRequest = { expandedPersonDropdown = false }
                        ) {
                            sortedMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name + " (${member.relation})") },
                                    onClick = {
                                        viewModel.selectPerson(member.name)
                                        expandedPersonDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Add profile button
                    IconButton(
                        onClick = { showAddPersonDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Mitglied hinzufügen")
                    }
                }
            }

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategoryTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategoryTab == category,
                        onClick = { selectedCategoryTab = category },
                        text = { Text(category) }
                    )
                }
            }

            if (filteredCheckups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Keine Vorsorgeuntersuchungen für $selectedPerson in dieser Kategorie gefunden.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCheckups) { checkup ->
                        CheckupCard(
                            checkup = checkup,
                            onMarkAsDoneClick = {
                                checkupToMarkDone = checkup
                                showDatePicker = true
                            },
                            onDeleteClick = {
                                viewModel.deleteCheckup(checkup)
                            },
                            onToggleEnabledClick = {
                                viewModel.toggleCheckupEnabled(checkup)
                            }
                        )
                    }
                }
            }
        }

        // Add Family Member Dialog
        if (showAddPersonDialog) {
            var newName by remember { mutableStateOf("") }
            var relationKind by remember { mutableStateOf(true) } // true for child, false for adult
            var birthYearStr by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()) }
            var selectedGender by remember { mutableStateOf("ALL") } // "ALL", "M", "F"

            AlertDialog(
                onDismissRequest = { showAddPersonDialog = false },
                title = { Text("Familienmitglied hinzufügen") },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                val relation = if (relationKind) "Kind" else "Partner"
                                val birthYear = birthYearStr.toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedGender == "ALL", onClick = { selectedGender = "ALL" })
                                    Text("Divers", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedGender == "M", onClick = { selectedGender = "M" })
                                    Text("Männlich", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedGender == "F", onClick = { selectedGender = "F" })
                                    Text("Weiblich", style = MaterialTheme.typography.bodySmall)
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

        // Custom Checkup Dialog
        if (showAddDialog) {
            var customTitle by remember { mutableStateOf("") }
            var customCategory by remember { mutableStateOf("Allgemeinmedizin") }
            var customAge by remember { mutableStateOf("") }
            var customIntervalStr by remember { mutableStateOf("12") }
            var customDescription by remember { mutableStateOf("") }
            var customGender by remember { mutableStateOf("ALL") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Eigene Vorsorge hinzufügen") },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customTitle.isNotBlank()) {
                                val interval = customIntervalStr.toIntOrNull() ?: 12
                                viewModel.addCustomCheckup(
                                    personName = selectedPerson,
                                    title = customTitle,
                                    category = customCategory,
                                    description = customDescription,
                                    recommendedAge = if (customAge.isNotBlank()) customAge else "Beliebig",
                                    intervalMonths = interval,
                                    gender = customGender
                                )
                                showAddDialog = false
                            }
                        },
                        enabled = customTitle.isNotBlank()
                    ) {
                        Text("Hinzufügen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Abbrechen")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Titel (z.B. Blutspende)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("Kategorie (z.B. Vorsorge)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customAge,
                            onValueChange = { customAge = it },
                            label = { Text("Empfohlenes Alter (z.B. Ab 18)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customIntervalStr,
                            onValueChange = { customIntervalStr = it },
                            label = { Text("Intervall in Monaten (z.B. 12)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customDescription,
                            onValueChange = { customDescription = it },
                            label = { Text("Beschreibung") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            )
        }

        // Date Picker for marking checkup as done
        if (showDatePicker && checkupToMarkDone != null) {
            DatePickerDialog(
                onDismissRequest = { 
                    showDatePicker = false
                    checkupToMarkDone = null
                },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedMillis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                        checkupToMarkDone?.let {
                            viewModel.markCheckupAsDone(it, selectedMillis)
                        }
                        showDatePicker = false
                        checkupToMarkDone = null
                    }) {
                        Text("Speichern")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDatePicker = false
                        checkupToMarkDone = null
                    }) {
                        Text("Abbrechen")
                    }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
    }
}
