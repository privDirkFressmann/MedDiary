package com.meddiary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.meddiary.MainActivity
import com.meddiary.R

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
        // Notify the list view that data changed
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
        
        // Update the widget shell
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.appointment_widget)

    // Set up the intent that starts the WidgetService, which will
    // provide the views for this collection.
    val intent = Intent(context, WidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Set data to force system to re-bind adapter when widget receives an update
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    views.setRemoteAdapter(R.id.widget_list, intent)

    // Set empty view
    views.setEmptyView(R.id.widget_list, R.id.widget_empty_view)

    // Set pending intent template for list items
    val clickIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        clickIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    views.setPendingIntentTemplate(R.id.widget_list, pendingIntent)

    // Set pending intent for header
    views.setOnClickPendingIntent(R.id.widget_header, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
