package com.onebutton.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Profile(
    val id: String,
    val role: Role,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("household_id")
    val householdId: String? = null,
    @SerialName("fcm_token")
    val fcmToken: String? = null,
    @SerialName("created_at")
    val createdAt: Instant? = null
)

@Serializable
enum class Role {
    @SerialName("elderly")
    ELDERLY,
    @SerialName("caretaker")
    CARETAKER
}
