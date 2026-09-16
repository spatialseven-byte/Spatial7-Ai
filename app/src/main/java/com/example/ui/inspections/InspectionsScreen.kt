package com.example.ui.inspections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InspectionTicket
import com.example.model.Tenant
import com.example.ui.theme.*

@Composable
fun InspectionsScreen(
    activeTenant: Tenant,
    tickets: List<InspectionTicket>,
    onCreateTicket: (InspectionTicket) -> Unit,
    onToggleResolved: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var filterResolved by remember { mutableStateOf<Boolean?>(null) } // null = all, false = open, true = resolved

    val filteredTickets = remember(tickets, filterResolved) {
        when (filterResolved) {
            null -> tickets
            true -> tickets.filter { it.resolved }
            false -> tickets.filter { !it.resolved }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NexusBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NexusCyan,
                contentColor = NexusBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Work Order")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WORK ORDERS & DRONE INSPECTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = NexusSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ROOM DB ISOLATED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexusEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Field Asset Maintenance Register",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Logged inspection tickets and maintenance tasks for ${activeTenant.name}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = filterResolved == null,
                                onClick = { filterResolved = null },
                                label = { Text("All (${tickets.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filterResolved == false,
                                onClick = { filterResolved = false },
                                label = { Text("Open (${tickets.count { !it.resolved }})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NexusCoral.copy(alpha = 0.2f), selectedLabelColor = NexusCoral)
                            )
                            FilterChip(
                                selected = filterResolved == true,
                                onClick = { filterResolved = true },
                                label = { Text("Resolved (${tickets.count { it.resolved }})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NexusEmerald.copy(alpha = 0.2f), selectedLabelColor = NexusEmerald)
                            )
                        }
                    }
                }
            }

            if (filteredTickets.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NexusSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No work orders found in this filter.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(filteredTickets, key = { it.id }) { ticket ->
                    InspectionTicketItem(
                        ticket = ticket,
                        onToggle = { onToggleResolved(ticket.id, !ticket.resolved) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateWorkOrderDialog(
            activeTenant = activeTenant,
            onDismiss = { showCreateDialog = false },
            onCreate = { newTicket ->
                onCreateTicket(newTicket)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun InspectionTicketItem(ticket: InspectionTicket, onToggle: () -> Unit) {
    val isCritical = ticket.priority.contains("P1") || ticket.priority.contains("Critical") || ticket.priority.contains("High")
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (ticket.resolved) NexusCardBorder else if (isCritical) NexusCoral else NexusAmber)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (isCritical) NexusCoral.copy(alpha = 0.2f) else NexusAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = ticket.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCritical) NexusCoral else NexusAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = ticket.assetName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Checkbox(
                    checked = ticket.resolved,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = NexusEmerald, uncheckedColor = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = ticket.findings, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspector: ${ticket.inspector}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "AI Conf: ${String.format("%.0f%%", ticket.aiConfidence * 100)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CreateWorkOrderDialog(
    activeTenant: Tenant,
    onDismiss: () -> Unit,
    onCreate: (InspectionTicket) -> Unit
) {
    var assetName by remember { mutableStateOf("") }
    var findings by remember { mutableStateOf("") }
    var inspector by remember { mutableStateOf("Field Engineer #12") }
    var priority by remember { mutableStateOf("P2 - High Priority") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexusSurface,
        title = {
            Text(text = "Log Spatial Work Order", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = assetName,
                    onValueChange = { assetName = it },
                    label = { Text("Asset Name / Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NexusCyan, unfocusedBorderColor = NexusCardBorder)
                )
                OutlinedTextField(
                    value = findings,
                    onValueChange = { findings = it },
                    label = { Text("Findings / Defect Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NexusCyan, unfocusedBorderColor = NexusCardBorder)
                )
                OutlinedTextField(
                    value = inspector,
                    onValueChange = { inspector = it },
                    label = { Text("Assigned Unit / Inspector") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NexusCyan, unfocusedBorderColor = NexusCardBorder)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (assetName.isNotBlank() && findings.isNotBlank()) {
                        onCreate(
                            InspectionTicket(
                                id = "ticket-${System.currentTimeMillis()}",
                                tenantId = activeTenant.tenantId,
                                assetId = "custom-asset-${System.currentTimeMillis() % 1000}",
                                assetName = assetName,
                                inspector = inspector,
                                findings = findings,
                                priority = priority,
                                aiConfidence = 0.92f,
                                timestamp = "2026-09-14 05:40 AEST",
                                resolved = false
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexusCyan)
            ) {
                Text("Create Work Order", color = NexusBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
