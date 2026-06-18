package com.meddiary.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEditAppointment : Screen("add_edit_appointment?appointmentId={appointmentId}") {
        fun passId(appointmentId: Int? = null): String {
            return if (appointmentId != null) "add_edit_appointment?appointmentId=$appointmentId" else "add_edit_appointment"
        }
    }
    object Checkups : Screen("checkups")
    object Calendar : Screen("calendar")
}
