package com.onebutton.platform

expect class NotificationManager {
    fun scheduleReminder(reminderId: String, title: String, description: String, timeInMillis: Long)
    fun showNotification(title: String, message: String)
}
