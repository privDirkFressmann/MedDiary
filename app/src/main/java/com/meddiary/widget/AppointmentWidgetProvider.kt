package com.meddiary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.meddiary.MainActivity
import com.meddiary.MedDiaryApplication
import com.meddiary.R
import com.meddiary.data.Appointment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        triggerWidgetRefresh(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || 
            intent.action == "com.meddiary.ACTION_WIDGET_REFRESH") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AppointmentWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            triggerWidgetRefresh(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun triggerWidgetRefresh(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val application = context.applicationContext as? MedDiaryApplication ?: return
        val appointmentDao = application.database.appointmentDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val upcoming = appointmentDao.getUpcomingAppointments(System.currentTimeMillis()).first()
                CoroutineScope(Dispatchers.Main).launch {
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId, upcoming)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    appointments: List<Appointment>
) {
    val views = RemoteViews(context.packageName, R.layout.appointment_widget)

    // Clear previous items from the container
    views.removeAllViews(R.id.widget_list_container)

    if (appointments.isEmpty()) {
        views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
        views.setViewVisibility(R.id.widget_list_container, View.GONE)
    } else {
        views.setViewVisibility(R.id.widget_empty_view, View.GONE)
        views.setViewVisibility(R.id.widget_list_container, View.VISIBLE)

        // Show up to 3 upcoming appointments
        for (appointment in appointments.take(3)) {
            val itemView = RemoteViews(context.packageName, R.layout.widget_item_appointment)
            
            val formattedDate = SimpleDateFormat("EEE, dd. MMM - HH:mm", Locale.GERMAN).format(Date(appointment.dateMillis))
            val doctorText = if (appointment.doctor.isNotBlank()) "bei ${appointment.doctor}" else appointment.specialty

            itemView.setTextViewText(R.id.widget_item_date, "$formattedDate Uhr")
            itemView.setTextViewText(R.id.widget_item_title, appointment.title)
            itemView.setTextViewText(R.id.widget_item_details, "$doctorText • ${appointment.personName}")

            views.addView(R.id.widget_list_container, itemView)
        }
    }

    // Set pending intent to open app when clicking the header or empty view
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_header, pendingIntent)
    views.setOnClickPendingIntent(R.id.widget_empty_view, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
