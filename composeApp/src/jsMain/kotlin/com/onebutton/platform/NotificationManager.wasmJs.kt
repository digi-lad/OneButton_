package com.onebutton.platform

import kotlinx.browser.window
import org.w3c.notifications.Notification

actual class NotificationManager {
    actual fun scheduleReminder(
        reminderId: String,
        title: String,
        description: String,
        timeInMillis: Long
    ) {
        // In browser, scheduling far in advance requires a service worker,
        // but for short term we can use setTimeout if the app remains open.
        val delay = timeInMillis - kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        if (delay > 0) {
            window.setTimeout({
                showNotification(title, description)
            }, delay.toInt())
        }
    }

    actual fun showNotification(title: String, message: String) {
        if (js("typeof Notification !== 'undefined'") as Boolean) {
            val permission = Notification.permission
            if (permission == "granted") {
                // Cannot call new Notification directly in Kotlin/WasmJS without external declarations, using JS interop wrapper
                js("new Notification(title, { body: message })")
            } else if (permission != "denied") {
                Notification.requestPermission().then {
                    if (it == "granted") {
                        js("new Notification(title, { body: message })")
                    }
                    null
                }
            }
        } else {
            window.alert("$title\n$message")
        }
    }
}
