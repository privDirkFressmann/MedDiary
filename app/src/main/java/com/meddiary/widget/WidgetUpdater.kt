package com.meddiary.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

object WidgetUpdater {
    fun updateWidget(context: Context) {
        val intent = Intent(context, AppointmentWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}
