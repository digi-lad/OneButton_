package com.onebutton.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.onebutton.model.Alert
import com.onebutton.model.Reminder
import com.onebutton.repository.AlertRepository
import com.onebutton.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaretakerHomeModel(
    private val householdId: String,
    private val reminderRepo: ReminderRepository,
    private val alertRepo: AlertRepository
) : ScreenModel {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadData()
        observeData()
    }

    private fun loadData() {
        screenModelScope.launch {
            _alerts.value = alertRepo.getAlerts(householdId).sortedByDescending { it.createdAt }
            _reminders.value = reminderRepo.getReminders(householdId)
        }
    }

    private fun observeData() {
        screenModelScope.launch {
            alertRepo.subscribeToAlerts(householdId).collect {
                loadData()
            }
        }
        screenModelScope.launch {
            reminderRepo.subscribeToReminders(householdId).collect {
                loadData()
            }
        }
    }

    fun resolveAlert(alertId: String) {
        screenModelScope.launch {
            alertRepo.resolveAlert(alertId)
            loadData()
        }
    }
}
