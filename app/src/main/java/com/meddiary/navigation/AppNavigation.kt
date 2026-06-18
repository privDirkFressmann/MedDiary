package com.meddiary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.meddiary.ui.MedicalViewModel
import com.meddiary.ui.screens.AddEditAppointmentScreen
import com.meddiary.ui.screens.CalendarScreen
import com.meddiary.ui.screens.CheckupsScreen
import com.meddiary.ui.screens.HomeScreen
import com.meddiary.ui.screens.VaccinationsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MedicalViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddAppointment = { id ->
                    navController.navigate(Screen.AddEditAppointment.passId(id))
                },
                onNavigateToCheckups = {
                    navController.navigate(Screen.Checkups.route)
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToVaccinations = {
                    navController.navigate(Screen.Vaccinations.route)
                }
            )
        }

        composable(
            route = Screen.AddEditAppointment.route,
            arguments = listOf(
                navArgument("appointmentId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val idStr = backStackEntry.arguments?.getString("appointmentId")
            val id = idStr?.toIntOrNull()
            AddEditAppointmentScreen(
                viewModel = viewModel,
                appointmentId = id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Checkups.route) {
            CheckupsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToAddAppointment = { id ->
                    navController.navigate(Screen.AddEditAppointment.passId(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Vaccinations.route) {
            VaccinationsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
