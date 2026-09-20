package com.onebutton.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.dsl.module

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://ozvzeqcyygabpppwlgzn.supabase.co",
            supabaseKey = "sb_publishable_RG2J4nwZIRb_j5_MkydRig_Gp_8R1VG"
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
