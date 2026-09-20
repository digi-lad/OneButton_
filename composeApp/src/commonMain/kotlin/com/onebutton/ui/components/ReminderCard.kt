package com.onebutton.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onebutton.model.Reminder
import com.onebutton.model.ReminderStatus
import com.onebutton.ui.theme.AlertRed
import com.onebutton.ui.theme.SoftBlueDark
import com.onebutton.ui.theme.SoftGreenDark

@Composable
fun ReminderCard(
    reminder: Reminder,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!reminder.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(status = reminder.status)
            }
            Button(onClick = onEdit) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun StatusBadge(status: ReminderStatus) {
    val color = when (status) {
        ReminderStatus.PENDING -> SoftBlueDark
        ReminderStatus.DONE -> SoftGreenDark
        ReminderStatus.MISSED -> AlertRed
        ReminderStatus.SNOOZED -> SoftBlueDark
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
