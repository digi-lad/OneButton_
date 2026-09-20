package com.onebutton.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Reminder(
    val id: String = "",
    @SerialName("household_id")
    val householdId: String,
    val title: String,
    val description: String? = null,
    @SerialName("start_time")
    val startTime: Instant,
    val recurrence: Recurrence = Recurrence.ONCE,
    @SerialName("snooze_duration_minutes")
    val snoozeDurationMinutes: Int = 15,
    @SerialName("inactivity_limit_minutes")
    val inactivityLimitMinutes: Int = 30,
    val status: ReminderStatus = ReminderStatus.PENDING,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("last_modified_at")
    val lastModifiedAt: Instant? = null
)

@Serializable
enum class Recurrence {
    ONCE, EVERY_DAY, EVERY_WEEK, EVERY_X_HOURS_4, EVERY_X_HOURS_8, EVERY_X_HOURS_12
}

@Serializable
enum class ReminderStatus {
    PENDING, SNOOZED, DONE, MISSED
}
