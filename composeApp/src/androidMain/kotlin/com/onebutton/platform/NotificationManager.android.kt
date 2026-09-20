package com.onebutton.platform

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

actual class NotificationManager(private val context: Context) {
    actual fun scheduleReminder(
        reminderId: String,
        title: String,
        description: String,
        timeInMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // In a real app we'd create an intent to a BroadcastReceiver
        // val intent = Intent(context, ReminderReceiver::class.java).apply {
        //     putExtra("title", title)
        //     putExtra("description", description)
        // }
        // val pendingIntent = PendingIntent.getBroadcast(context, reminderId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
        // alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        println("Android: Scheduled reminder $title at $timeInMillis")
    }

    actual fun showNotification(title: String, message: String) {
        val channelId = "onebutton_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
        println("Android: Showed notification $title: $message")
    }
}
