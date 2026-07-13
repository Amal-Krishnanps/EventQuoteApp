package com.eventquote.app.ui.screens.estimate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.EventType
import com.eventquote.app.ui.components.*
import com.eventquote.app.utils.DateUtils
import java.util.Calendar

/**
 * Event details section of the estimate form.
 */
@Composable
fun EventSection(
    estimate: Estimate,
    validationErrors: Map<String, String>,
    onEventTypeChange: (EventType) -> Unit,
    onFunctionDateChange: (Long) -> Unit,
    onFunctionTimeChange: (String) -> Unit,
    onVenueNameChange: (String) -> Unit,
    onVenueAddressChange: (String) -> Unit,
    onCoordinatorChange: (String) -> Unit,
    onGuestCountChange: (Int) -> Unit,
    onEventNotesChange: (String) -> Unit,
    onClearError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SectionCard(
        title = "Event Details",
        icon = Icons.Default.Celebration,
        modifier = modifier
    ) {
        // Event Type
        DropdownSelector(
            label = "Event Type",
            items = EventType.entries,
            selectedItem = estimate.eventType,
            onItemSelected = onEventTypeChange,
            itemLabel = { it.displayName },
            leadingIcon = Icons.Default.Celebration
        )
        Spacer(Modifier.height(10.dp))

        // Function Date & Time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            // --- Date picker ---
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = DateUtils.formatDate(estimate.functionDate),
                    onValueChange = {},
                    label = { Text("Function Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                // Transparent overlay to capture taps
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = estimate.functionDate }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis.let(onFunctionDateChange)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                )
            }

            // --- Time picker ---
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = estimate.functionTime.ifBlank { "Tap to set" },
                    onValueChange = {},
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                // Transparent overlay to capture taps
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val amPm = if (hour < 12) "AM" else "PM"
                                    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                                    onFunctionTimeChange("$h:${minute.toString().padStart(2, '0')} $amPm")
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        // Venue Name
        FormTextField(
            label = "Venue Name *",
            value = estimate.venueName,
            onValueChange = { onVenueNameChange(it); onClearError("venueName") },
            isError = validationErrors.containsKey("venueName"),
            errorMessage = validationErrors["venueName"],
            leadingIcon = Icons.Default.LocationOn
        )
        Spacer(Modifier.height(10.dp))

        // Venue Address
        FormTextField(
            label = "Venue Address",
            value = estimate.venueAddress,
            onValueChange = onVenueAddressChange,
            leadingIcon = Icons.Default.Map,
            singleLine = false,
            maxLines = 2
        )
        Spacer(Modifier.height(10.dp))

        // Coordinator + Guest Count
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormTextField(
                label = "Event Coordinator",
                value = estimate.eventCoordinator,
                onValueChange = onCoordinatorChange,
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                label = "Guest Count",
                value = if (estimate.guestCount == 0) "" else estimate.guestCount.toString(),
                onValueChange = { onGuestCountChange(it.toIntOrNull() ?: 0) },
                keyboardType = KeyboardType.Number,
                leadingIcon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))

        // Notes
        FormTextField(
            label = "Notes (Optional)",
            value = estimate.eventNotes,
            onValueChange = onEventNotesChange,
            singleLine = false,
            maxLines = 3,
            leadingIcon = Icons.Default.Notes
        )
    }
}
