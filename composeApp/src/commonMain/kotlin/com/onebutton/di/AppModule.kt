package com.onebutton.di

import com.onebutton.repository.AuthRepository
import com.onebutton.repository.AlertRepository
import com.onebutton.repository.HouseholdRepository
import com.onebutton.repository.ReminderRepository
import com.onebutton.ui.viewmodel.AuthScreenModel
import com.onebutton.ui.viewmodel.CaretakerHomeModel
import com.onebutton.ui.viewmodel.ElderlyHomeModel
import org.koin.dsl.module

val appModule = module {
    single { AuthRepository(get()) }
    single { AlertRepository(get()) }
    single { HouseholdRepository(get()) }
    single { ReminderRepository(get()) }

    factory { AuthScreenModel(get()) }
    factory { (householdId: String) -> ElderlyHomeModel(householdId, get(), get(), get()) }
    factory { (householdId: String) -> CaretakerHomeModel(householdId, get(), get()) }
}

val koinModules = listOf(supabaseModule, appModule)
