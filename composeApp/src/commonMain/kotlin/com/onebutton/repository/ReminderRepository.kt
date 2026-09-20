package com.onebutton.repository

import com.onebutton.model.Reminder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(private val supabase: SupabaseClient) {

    suspend fun getReminders(householdId: String): List<Reminder> {
        return try {
            supabase.postgrest["reminders"]
                .select { filter { eq("household_id", householdId) } }
                .decodeList<Reminder>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun addReminder(reminder: Reminder) {
        try {
            supabase.postgrest["reminders"].insert(reminder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateReminder(reminder: Reminder) {
        try {
            supabase.postgrest["reminders"]
                .update(reminder) { filter { eq("id", reminder.id) } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteReminder(id: String) {
        try {
            supabase.postgrest["reminders"]
                .delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Realtime subscriptions
    suspend fun subscribeToReminders(householdId: String): Flow<PostgresAction> {
        val channel = supabase.channel("public:reminders")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "reminders"
            filter = "household_id=eq.$householdId"
        }
        channel.subscribe()
        return flow
    }
}
