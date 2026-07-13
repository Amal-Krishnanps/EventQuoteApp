package com.eventquote.app.ui.screens.estimate

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eventquote.app.model.*
import com.eventquote.app.ui.components.*
import com.eventquote.app.utils.CurrencyFormatter

/**
 * Services section — add/remove/edit services with sub-items.
 */
@Composable
fun ServicesSection(
    estimate: Estimate,
    availableMasterServices: List<ServiceMaster>,
    validationErrors: Map<String, String>,
    onAddService: () -> Unit,
    onAddServiceFromMaster: (ServiceMaster) -> Unit,
    onUpdateService: (EstimateService) -> Unit,
    onRemoveService: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onAddSubItem: (String) -> Unit,
    onUpdateSubItem: (String, SubItem) -> Unit,
    onRemoveSubItem: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMasterPicker by remember { mutableStateOf(false) }
    val hasError = validationErrors.containsKey("services")

    SectionCard(
        title = "Services (${estimate.services.size})",
        icon = Icons.Default.Category,
        modifier = modifier
    ) {
        // Validation error
        AnimatedVisibility(visible = hasError) {
            Text(
                text = validationErrors["services"] ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Total display
        if (estimate.services.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Services Subtotal", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyFormatter.format(estimate.subtotal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
        }

        // Service cards
        estimate.services.forEach { service ->
            ServiceCard(
                service = service,
                onUpdate = onUpdateService,
                onDelete = { onRemoveService(service.id) },
                onToggleExpand = { onToggleExpand(service.id) },
                onAddSubItem = { onAddSubItem(service.id) },
                onUpdateSubItem = { subItem -> onUpdateSubItem(service.id, subItem) },
                onDeleteSubItem = { subItemId -> onRemoveSubItem(service.id, subItemId) },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAddService,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Custom Service")
            }
            Button(
                onClick = { showMasterPicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = availableMasterServices.isNotEmpty()
            ) {
                Icon(Icons.Default.LibraryAdd, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("From List")
            }
        }
    }

    // Master service picker dialog
    if (showMasterPicker) {
        MasterServicePickerDialog(
            services = availableMasterServices,
            existingServiceNames = estimate.services.map { it.name },
            onSelect = { master ->
                onAddServiceFromMaster(master)
                showMasterPicker = false
            },
            onDismiss = { showMasterPicker = false }
        )
    }
}

@Composable
private fun MasterServicePickerDialog(
    services: List<ServiceMaster>,
    existingServiceNames: List<String>,
    onSelect: (ServiceMaster) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = services.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Service", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search services...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(filtered) { service ->
                        val alreadyAdded = existingServiceNames.any {
                            it.equals(service.name, ignoreCase = true)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyAdded) { onSelect(service) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (alreadyAdded) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                null,
                                tint = if (alreadyAdded) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(service.name, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (alreadyAdded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface)
                                if (service.defaultSubItems.isNotEmpty()) {
                                    Text("${service.defaultSubItems.size} sub-items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (alreadyAdded) {
                                Text("Added", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            } else if (service.defaultAmount > 0) {
                                Text(CurrencyFormatter.format(service.defaultAmount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}
