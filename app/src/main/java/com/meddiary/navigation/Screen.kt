package com.meddiary.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEditAppointment : Screen("add_edit_appointment?appointmentId={appointmentId}&copy={copy}") {
        fun passId(appointmentId: Int? = null, copy: Boolean = false): String {
            return if (appointmentId != null) "add_edit_appointment?appointmentId=$appointmentId&copy=$copy" else "add_edit_appointment"
        }
    }
    object Checkups : Screen("checkups")
    object Calendar : Screen("calendar")
    object Vaccinations : Screen("vaccinations")
    object Doctors : Screen("doctors")
}
