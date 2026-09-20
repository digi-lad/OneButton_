package com.onebutton.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.onebutton.model.Reminder
import com.onebutton.repository.HouseholdRepository
import com.onebutton.repository.ReminderRepository
import com.onebutton.repository.AlertRepository
import com.onebutton.model.Alert
import com.onebutton.model.AlertType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.datetime.Clock

class ElderlyHomeModel(
    private val householdId: String,
    private val reminderRepo: ReminderRepository,
    private val householdRepo: HouseholdRepository,
    private val alertRepo: AlertRepository
) : ScreenModel {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadReminders()
        observeReminders()
    }

    private fun loadReminders() {
        screenModelScope.launch {
            _reminders.value = reminderRepo.getReminders(householdId)
        }
    }

    private fun observeReminders() {
        screenModelScope.launch {
            reminderRepo.subscribeToReminders(householdId).collect { action ->
                loadReminders() // Simple refresh on any change
            }
        }
    }

    fun triggerSOS() {
        screenModelScope.launch {
            householdRepo.updateActivity(householdId)
            val alert = Alert(
                householdId = householdId,
                type = AlertType.SOS,
                message = "Emergency! SOS button pressed."
            )
            alertRepo.createAlert(alert)
        }
    }

    fun registerActivity() {
        screenModelScope.launch {
            householdRepo.updateActivity(householdId)
        }
    }
}
