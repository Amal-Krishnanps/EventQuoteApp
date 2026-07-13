package com.eventquote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.EstimateStatus
import com.eventquote.app.ui.theme.*
import com.eventquote.app.utils.CurrencyFormatter
import com.eventquote.app.utils.DateUtils

/**
 * Card representing a single estimate in the saved estimates list.
 */
@Composable
fun EstimateListItem(
    estimate: Estimate,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (EstimateStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ---- Top Row: Estimate Number + Status + Menu ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored left accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusColor(estimate.status))
                )
                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estimate.estimateNumber.ifBlank { "Draft" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = estimate.customerName.ifBlank { "No Name" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                StatusChip(estimate.status)
                Spacer(Modifier.width(4.dp))

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onClick() },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Status") },
                            onClick = { showMenu = false; showStatusMenu = true },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp)) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            onClick = { showMenu = false; showDeleteConfirm = true },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(10.dp))

            // ---- Info Row ----
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoChip(icon = Icons.Outlined.CalendarMonth,
                    text = DateUtils.formatDate(estimate.functionDate),
                    modifier = Modifier.weight(1f))
                InfoChip(icon = Icons.Outlined.LocationOn,
                    text = estimate.venueName.ifBlank { "No Venue" },
                    modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoChip(icon = Icons.Outlined.Person,
                    text = estimate.eventType.displayName,
                    modifier = Modifier.weight(1f))
                InfoChip(icon = Icons.Default.Group,
                    text = "${estimate.guestCount} guests",
                    modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(10.dp))

            // ---- Amount Row ----
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Grand Total", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(estimate.grandTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Balance Due", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = CurrencyFormatter.format(estimate.balanceAmount.coerceAtLeast(0.0)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (estimate.balanceAmount > 0) WarningOrange else SuccessGreen
                    )
                }
            }
        }
    }

    // ---- Dialogs ----
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Estimate",
            message = "Delete \"${estimate.estimateNumber}\" for ${estimate.customerName}? This cannot be undone.",
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showStatusMenu) {
        AlertDialog(
            onDismissRequest = { showStatusMenu = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    EstimateStatus.entries.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStatusChange(status)
                                    showStatusMenu = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(statusColor(status)))
                            Spacer(Modifier.width(12.dp))
                            Text(status.displayName, style = MaterialTheme.typography.bodyMedium,
                                color = if (status == estimate.status) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (status == estimate.status) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusMenu = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}

private fun statusColor(status: EstimateStatus): Color = when (status) {
    EstimateStatus.DRAFT -> StatusDraft
    EstimateStatus.QUOTATION_SENT -> StatusSent
    EstimateStatus.CONFIRMED -> StatusConfirmed
    EstimateStatus.CANCELLED -> StatusCancelled
    EstimateStatus.COMPLETED -> StatusCompleted
}
