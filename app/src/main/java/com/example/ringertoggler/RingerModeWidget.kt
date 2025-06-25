package com.example.ringertoggler

import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import android.widget.Toast

class RingerModeWidget : AppWidgetProvider() {

    private val ACTION_TOGGLE_RINGER = "com.example.ringertoggler.ACTION_TOGGLE_RINGER"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (intent.action == ACTION_TOGGLE_RINGER) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                Toast.makeText(context, "DND permission needed for Silent mode", Toast.LENGTH_LONG).show()
                val permissionIntent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                permissionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(permissionIntent)
                return
            }

            val currentRinger = audioManager.ringerMode
            val currentDND = notificationManager.currentInterruptionFilter

            when {
                currentRinger == AudioManager.RINGER_MODE_NORMAL && currentDND != NotificationManager.INTERRUPTION_FILTER_NONE -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Toast.makeText(context, "Switched to Vibrate", Toast.LENGTH_SHORT).show()
                }
                currentRinger == AudioManager.RINGER_MODE_VIBRATE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    Toast.makeText(context, "Switched to Silent (DND)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    Toast.makeText(context, "Switched to Ring", Toast.LENGTH_SHORT).show()
                }
            }

            // Debug Toast to show current state
           // val debugMessage = "Ringer: ${audioManager.ringerMode}\nDND: ${notificationManager.currentInterruptionFilter}"
           // Toast.makeText(context, debugMessage, Toast.LENGTH_LONG).show()

            // Force update widget UI after mode change
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, RingerModeWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Determine widget UI based on ringer and DND state
        val dndFilter = notificationManager.currentInterruptionFilter
        val ringerMode = audioManager.ringerMode

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && dndFilter == NotificationManager.INTERRUPTION_FILTER_NONE -> {
                views.setTextViewText(R.id.widget_text, "Silent")
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#F44336")) // Red
            }
            ringerMode == AudioManager.RINGER_MODE_VIBRATE -> {
                views.setTextViewText(R.id.widget_text, "Vibrate")
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#2196F3")) // Blue
            }
            else -> {
                views.setTextViewText(R.id.widget_text, "Ring")
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#4CAF50")) // Green
            }
        }

        // Set up click behavior
        val intent = Intent(context, RingerModeWidget::class.java).apply {
            action = ACTION_TOGGLE_RINGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
