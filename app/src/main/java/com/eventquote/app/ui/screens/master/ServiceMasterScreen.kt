package com.eventquote.app.ui.screens.master

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eventquote.app.model.ServiceMaster
import com.eventquote.app.model.SubItemTemplate
import com.eventquote.app.ui.components.*
import com.eventquote.app.ui.theme.ErrorRed
import com.eventquote.app.viewmodel.ServiceMasterViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceMasterScreen(
    viewModel: ServiceMasterViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceMaster?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            PrimaryFab(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                label = "Add Service"
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            GradientHeader(
                title = "Services Master",
                subtitle = "${uiState.services.size} services configured",
                onBack = onNavigateBack
            )

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder = { Text("Search services...") },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (uiState.services.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Category,
                        title = "No services yet",
                        subtitle = "Add your first service template",
                        actionLabel = "Add Service",
                        onAction = { showAddDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceMasterItem(
                            service = service,
                            onEdit = { editingService = service },
                            onDelete = { viewModel.deleteService(service.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(service.id, service.isFavorite) }
                        )
                    }
                }
            }
        }
    }

    // Add new service dialog
    if (showAddDialog) {
        AddEditServiceDialog(
            service = null,
            onSave = { name, description, amount, subItems ->
                val newService = ServiceMaster(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    defaultAmount = amount,
                    sortOrder = uiState.services.size,
                    defaultSubItems = subItems
                )
                viewModel.updateService(newService)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit existing service dialog
    editingService?.let { service ->
        AddEditServiceDialog(
            service = service,
            onSave = { name, description, amount, subItems ->
                viewModel.updateService(service.copy(
                    name = name, description = description,
                    defaultAmount = amount, defaultSubItems = subItems
                ))
                editingService = null
            },
            onDismiss = { editingService = null }
        )
    }
}

@Composable
private fun ServiceMasterItem(
    service: ServiceMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (service.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                        null,
                        tint = if (service.isFavorite) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    if (service.description.isNotBlank()) {
                        Text(service.description, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Sub-items chip
                if (service.defaultSubItems.isNotEmpty()) {
                    AssistChip(
                        onClick = { expanded = !expanded },
                        label = { Text("${service.defaultSubItems.size} items",
                            style = MaterialTheme.typography.labelSmall) }
                    )
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                }
            }

            // Expanded sub-items list
            AnimatedVisibility(visible = expanded && service.defaultSubItems.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(6.dp))
                    service.defaultSubItems.forEach { subItem ->
                        Row(modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(subItem.name, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Service",
            message = "Delete \"${service.name}\" from master list? This won't affect existing estimates.",
            onConfirm = { onDelete(); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun AddEditServiceDialog(
    service: ServiceMaster?,
    onSave: (String, String, Double, List<SubItemTemplate>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var amount by remember { mutableStateOf(service?.defaultAmount?.toString() ?: "") }
    var subItems by remember { mutableStateOf(service?.defaultSubItems ?: emptyList()) }
    var newSubItemName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (service == null) "Add Service" else "Edit Service",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Default Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(12.dp))

                // Sub-items
                Text("Default Sub-Items", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                subItems.forEach { sub ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(sub.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            subItems = subItems.filter { it.id != sub.id }
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp), tint = ErrorRed)
                        }
                    }
                }

                // Add sub-item inline
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubItemName,
                        onValueChange = { newSubItemName = it },
                        placeholder = { Text("Add sub-item...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newSubItemName.isNotBlank()) {
                                subItems = subItems + SubItemTemplate(
                                    id = UUID.randomUUID().toString(), name = newSubItemName.trim()
                                )
                                newSubItemName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), description.trim(),
                                    amount.toDoubleOrNull() ?: 0.0, subItems)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) { Text("Save") }
                }
            }
        }
    }
}
