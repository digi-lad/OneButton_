package com.onebutton.repository

import com.onebutton.model.Alert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val supabase: SupabaseClient) {

    suspend fun getAlerts(householdId: String): List<Alert> {
        return try {
            supabase.postgrest["alerts"]
                .select { filter { eq("household_id", householdId) } }
                .decodeList<Alert>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createAlert(alert: Alert) {
        try {
            supabase.postgrest["alerts"].insert(alert)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun resolveAlert(alertId: String) {
        try {
            supabase.postgrest["alerts"]
                .update({ set("resolved", true) }) {
                    filter { eq("id", alertId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Realtime subscriptions for new alerts
    suspend fun subscribeToAlerts(householdId: String): Flow<PostgresAction> {
        val channel = supabase.channel("public:alerts")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "alerts"
            filter = "household_id=eq.$householdId"
        }
        channel.subscribe()
        return flow
    }
}
