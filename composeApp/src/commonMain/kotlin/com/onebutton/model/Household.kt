package com.onebutton.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Household(
    val id: String,
    @SerialName("elderly_user_id")
    val elderlyUserId: String? = null,
    @SerialName("elderly_name")
    val elderlyName: String,
    @SerialName("last_active_at")
    val lastActiveAt: Instant? = null,
    @SerialName("emergency_number")
    val emergencyNumber: String? = null,
    @SerialName("created_at")
    val createdAt: Instant? = null
)
