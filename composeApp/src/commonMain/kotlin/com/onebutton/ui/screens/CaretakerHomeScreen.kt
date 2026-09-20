package com.onebutton.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.onebutton.ui.components.ReminderCard
import com.onebutton.ui.viewmodel.CaretakerHomeModel
import org.koin.core.parameter.parametersOf

data class CaretakerHomeScreen(val householdId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<CaretakerHomeModel> { parametersOf(householdId) }
        val alerts by screenModel.alerts.collectAsState()
        val reminders by screenModel.reminders.collectAsState()

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Reminders", "Alerts", "Settings")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Caretaker Dashboard") }
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = { },
                            label = { Text(title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(reminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onEdit = {}
                                )
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            items(alerts) { alert ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Alert Type: ${alert.type.name}", style = MaterialTheme.typography.titleMedium)
                                        Text(alert.message ?: "", style = MaterialTheme.typography.bodyMedium)
                                        if (!alert.resolved) {
                                            Button(onClick = { screenModel.resolveAlert(alert.id) }) {
                                                Text("Resolve")
                                            }
                                        } else {
                                            Text("Resolved", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Household ID: $householdId")
                        }
                    }
                }
            }
        }
    }
}
