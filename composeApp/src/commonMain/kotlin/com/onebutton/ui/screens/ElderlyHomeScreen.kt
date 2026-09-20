package com.onebutton.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.onebutton.ui.components.ReminderCard
import com.onebutton.ui.components.SOSButton
import com.onebutton.ui.viewmodel.ElderlyHomeModel
import org.koin.core.parameter.parametersOf

data class ElderlyHomeScreen(val householdId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ElderlyHomeModel> { parametersOf(householdId) }
        val reminders by screenModel.reminders.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        Text(
                            text = "Last active: Just now",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* TODO: Open add reminder dialog */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SOSButton(
                    onClick = { screenModel.triggerSOS() }
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Today's Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp).align(Alignment.Start)
                )
                
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onEdit = { /* TODO: edit reminder */ }
                        )
                    }
                }
            }
        }
    }
}
