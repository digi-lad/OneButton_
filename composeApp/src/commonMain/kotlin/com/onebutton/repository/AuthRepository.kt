package com.onebutton.repository

import com.onebutton.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository(private val supabase: SupabaseClient) {

    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }
    
    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun getCurrentProfile(): Profile? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        return try {
            supabase.postgrest["profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
