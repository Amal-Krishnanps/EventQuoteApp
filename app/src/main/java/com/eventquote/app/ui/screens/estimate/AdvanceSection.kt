package com.eventquote.app.ui.screens.estimate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.AdvancePayment
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.PaymentMode
import com.eventquote.app.ui.components.AmountRow
import com.eventquote.app.ui.components.SectionCard
import com.eventquote.app.ui.theme.SuccessGreen
import com.eventquote.app.utils.CurrencyFormatter

/**
 * Advance payment section — multiple payment entries with different modes.
 */
@Composable
fun AdvanceSection(
    estimate: Estimate,
    onAddPayment: () -> Unit,
    onUpdatePayment: (AdvancePayment) -> Unit,
    onRemovePayment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Advance Payment",
        icon = Icons.Default.Payment,
        modifier = modifier
    ) {
        // Payment entries
        estimate.advancePayments.forEach { payment ->
            AdvancePaymentRow(
                payment = payment,
                onUpdate = onUpdatePayment,
                onDelete = { onRemovePayment(payment.id) }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Add payment button
        OutlinedButton(
            onClick = onAddPayment,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Payment Entry")
        }

        // Summary
        if (estimate.advancePayments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                    .padding(12.dp)
            ) {
                Column {
                    AmountRow(label = "Grand Total", amount = CurrencyFormatter.format(estimate.grandTotal))
                    AmountRow(
                        label = "Total Advance Paid",
                        amount = "- ${CurrencyFormatter.format(estimate.totalAdvancePaid)}",
                        amountColor = SuccessGreen
                    )
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(Modifier.height(6.dp))
                    AmountRow(
                        label = "Balance Due",
                        amount = CurrencyFormatter.format(estimate.balanceAmount.coerceAtLeast(0.0)),
                        isBalance = true
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancePaymentRow(
    payment: AdvancePayment,
    onUpdate: (AdvancePayment) -> Unit,
    onDelete: () -> Unit
) {
    var modeExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Row header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Payment Entry",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close, null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Payment mode dropdown
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = modeExpanded,
                        onExpandedChange = { modeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = payment.mode.displayName,
                            onValueChange = {},
                            label = { Text("Mode") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = modeExpanded,
                            onDismissRequest = { modeExpanded = false }
                        ) {
                            PaymentMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.displayName) },
                                    onClick = {
                                        onUpdate(payment.copy(mode = mode))
                                        modeExpanded = false
                                    },
                                    leadingIcon = if (mode == payment.mode) {
                                        {
                                            Icon(
                                                Icons.Default.Check, null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Amount field
                OutlinedTextField(
                    value = if (payment.amount == 0.0) "" else payment.amount.toLong().toString(),
                    onValueChange = { onUpdate(payment.copy(amount = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text(
                            "₹",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            // Reference / transaction ID
            OutlinedTextField(
                value = payment.reference,
                onValueChange = { onUpdate(payment.copy(reference = it)) },
                label = { Text("Reference / Transaction ID (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
