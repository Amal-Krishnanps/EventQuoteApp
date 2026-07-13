package com.eventquote.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.EstimateService
import com.eventquote.app.model.SubItem
import com.eventquote.app.ui.theme.ErrorRed
import com.eventquote.app.utils.CurrencyFormatter

/**
 * Expandable service card used in estimate creation.
 * Supports expand/collapse, editing amount, sub-items, and deletion.
 */
@Composable
fun ServiceCard(
    service: EstimateService,
    onUpdate: (EstimateService) -> Unit,
    onDelete: () -> Unit,
    onToggleExpand: () -> Unit,
    onAddSubItem: () -> Unit,
    onUpdateSubItem: (SubItem) -> Unit,
    onDeleteSubItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ---- Header Row ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (service.isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name.ifBlank { "New Service" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (service.subItems.isNotEmpty()) {
                        Text(
                            text = "${service.subItems.size} sub-items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = CurrencyFormatter.format(service.totalCost),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, contentDescription = "Delete",
                        tint = ErrorRed, modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ---- Expanded Content ----
            AnimatedVisibility(visible = service.isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {

                    // Service Name
                    OutlinedTextField(
                        value = service.name,
                        onValueChange = { onUpdate(service.copy(name = it)) },
                        label = { Text("Service Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    // Description
                    OutlinedTextField(
                        value = service.description,
                        onValueChange = { onUpdate(service.copy(description = it)) },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Amount
                        OutlinedTextField(
                            value = if (service.amount == 0.0) "" else service.amount.toLong().toString(),
                            onValueChange = { raw ->
                                val amount = raw.toDoubleOrNull() ?: 0.0
                                onUpdate(service.copy(amount = amount.coerceAtLeast(0.0)))
                            },
                            label = { Text("Amount (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Remarks
                        OutlinedTextField(
                            value = service.remarks,
                            onValueChange = { onUpdate(service.copy(remarks = it)) },
                            label = { Text("Remarks") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // ---- Sub-Items ----
                    if (service.subItems.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sub-Items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))

                        service.subItems.forEach { subItem ->
                            SubItemRow(
                                subItem = subItem,
                                onUpdate = onUpdateSubItem,
                                onDelete = { onDeleteSubItem(subItem.id) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    // Add Sub-item button
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAddSubItem,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Sub-Item", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Service",
            message = "Are you sure you want to remove \"${service.name}\"?",
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun SubItemRow(
    subItem: SubItem,
    onUpdate: (SubItem) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Sub-item name
            BasicTextField(
                value = subItem.name,
                onUpdate = { onUpdate(subItem.copy(name = it)) },
                placeholder = "Sub-item name"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicTextField(
                    value = if (subItem.cost == 0.0) "" else subItem.cost.toLong().toString(),
                    onUpdate = { onUpdate(subItem.copy(cost = it.toDoubleOrNull() ?: 0.0)) },
                    placeholder = "₹ Cost",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                BasicTextField(
                    value = subItem.remarks,
                    onUpdate = { onUpdate(subItem.copy(remarks = it)) },
                    placeholder = "Remarks",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed,
                modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun BasicTextField(
    value: String,
    onUpdate: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onUpdate,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}
