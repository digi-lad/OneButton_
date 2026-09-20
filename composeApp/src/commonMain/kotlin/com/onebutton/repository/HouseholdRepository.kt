package com.onebutton.repository

import com.onebutton.model.Household
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.datetime.Clock

class HouseholdRepository(private val supabase: SupabaseClient) {

    suspend fun getHousehold(id: String): Household? {
        return try {
            supabase.postgrest["households"]
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<Household>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun createHousehold(household: Household): Household? {
        return try {
            supabase.postgrest["households"]
                .insert(household) { select() }
                .decodeSingleOrNull<Household>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateActivity(householdId: String) {
        try {
            // using kotlinx-datetime to get current instant
            val now = Clock.System.now()
            // We just send a map to update
            // the exact structure might depend on how supabase-kt expects the update
            supabase.postgrest["households"]
                .update({ set("last_active_at", now) }) {
                    filter { eq("id", householdId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
