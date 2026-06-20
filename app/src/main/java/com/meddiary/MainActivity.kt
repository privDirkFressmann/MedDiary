package com.meddiary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.meddiary.data.MedicalRepository
import com.meddiary.navigation.AppNavigation
import com.meddiary.navigation.Screen
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.MedicalViewModelFactory
import com.meddiary.ui.theme.MedDiaryTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MedicalViewModel by viewModels {
        val app = application as MedDiaryApplication
        val repo = MedicalRepository(
            app.database.appointmentDao(),
            app.database.checkupDao(),
            app.database.familyMemberDao(),
            app.database.attachmentDao(),
            app.database.vaccinationDao(),
            app.database.doctorDao()
        )
        MedicalViewModelFactory(repo, app.database)
    }

    private val intentState = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentState.value = intent
        setContent {
            MedDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )

                    // Listen for incoming widget deep links
                    LaunchedEffect(intentState.value) {
                        intentState.value?.let { currentIntent ->
                            val appointmentId = currentIntent.getIntExtra("appointment_id", -1)
                            if (appointmentId != -1) {
                                navController.navigate(Screen.AddEditAppointment.passId(appointmentId, false))
                                // Clear the extra and intent state so orientation changes don't re-trigger navigation
                                currentIntent.removeExtra("appointment_id")
                                intentState.value = null
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentState.value = intent
    }
}
