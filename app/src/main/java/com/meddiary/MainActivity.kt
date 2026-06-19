package com.meddiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.meddiary.data.MedicalRepository
import com.meddiary.navigation.AppNavigation
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                }
            }
        }
    }
}
