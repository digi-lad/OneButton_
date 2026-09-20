package com.onebutton.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Alert(
    val id: String = "",
    @SerialName("household_id")
    val householdId: String,
    val type: AlertType,
    @SerialName("reminder_id")
    val reminderId: String? = null,
    val message: String? = null,
    val resolved: Boolean = false,
    @SerialName("created_at")
    val createdAt: Instant? = null
)

@Serializable
enum class AlertType {
    MISSED_REMINDER, SOS, INACTIVITY_24H
}
