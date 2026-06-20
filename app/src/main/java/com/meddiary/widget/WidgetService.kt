package com.meddiary.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.meddiary.MedDiaryApplication
import com.meddiary.R
import com.meddiary.data.Appointment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AppointmentRemoteViewsFactory(applicationContext)
    }
}

class AppointmentRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var appointments = listOf<Appointment>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Fetch data synchronously since onDataSetChanged runs on a background thread
        val application = context.applicationContext as? MedDiaryApplication ?: return
        val appointmentDao = application.database.appointmentDao()
        runBlocking {
            try {
                appointments = appointmentDao.getUpcomingAppointments(System.currentTimeMillis()).first()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        appointments = emptyList()
    }

    override fun getCount(): Int = appointments.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= appointments.size) {
            return RemoteViews(context.packageName, R.layout.widget_item_appointment)
        }
        val appointment = appointments[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item_appointment)

        val formattedDate = SimpleDateFormat("EEE, dd. MMM - HH:mm", Locale.GERMAN).format(Date(appointment.dateMillis))
        val doctorText = if (appointment.doctor.isNotBlank()) "bei ${appointment.doctor}" else appointment.specialty

        views.setTextViewText(R.id.widget_item_date, "$formattedDate Uhr")
        views.setTextViewText(R.id.widget_item_title, appointment.title)
        views.setTextViewText(R.id.widget_item_details, "$doctorText • ${appointment.personName}")

        // Add a click fillInIntent so clicking an item opens the app
        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = if (position < appointments.size) appointments[position].id.toLong() else position.toLong()

    override fun hasStableIds(): Boolean = true
}
