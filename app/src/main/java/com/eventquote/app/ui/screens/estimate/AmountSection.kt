package com.eventquote.app.ui.screens.estimate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.DiscountType
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.TaxRate
import com.eventquote.app.ui.components.AmountRow
import com.eventquote.app.ui.components.SectionCard
import com.eventquote.app.ui.theme.SuccessGreen
import com.eventquote.app.utils.CurrencyFormatter

/**
 * Amount summary section with discount, tax, and grand total calculations.
 */
@Composable
fun AmountSection(
    estimate: Estimate,
    onDiscountTypeChange: (DiscountType) -> Unit,
    onDiscountValueChange: (Double) -> Unit,
    onTaxEnabledChange: (Boolean) -> Unit,
    onTaxRateChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Amount & Pricing",
        icon = Icons.Default.Calculate,
        modifier = modifier
    ) {
        // Subtotal display
        AmountRow(label = "Services Subtotal", amount = CurrencyFormatter.format(estimate.subtotal))
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(12.dp))

        // ---- Discount ----
        Text(
            "Discount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DiscountType.entries.forEach { type ->
                FilterChip(
                    selected = estimate.discountType == type,
                    onClick = { onDiscountTypeChange(type) },
                    label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (estimate.discountValue == 0.0) "" else estimate.discountValue.toString(),
                onValueChange = { onDiscountValueChange(it.toDoubleOrNull() ?: 0.0) },
                label = {
                    Text(
                        if (estimate.discountType == DiscountType.FIXED) "Discount Amount (₹)"
                        else "Discount Percentage (%)"
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        if (estimate.discountType == DiscountType.FIXED) "₹" else "%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            if (estimate.discountAmount > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Saving",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "- ${CurrencyFormatter.format(estimate.discountAmount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(12.dp))

        // ---- Tax (GST) ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "GST / Tax",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = estimate.taxEnabled,
                onCheckedChange = onTaxEnabledChange,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        AnimatedVisibility(visible = estimate.taxEnabled) {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TaxRate.GST_5, TaxRate.GST_12, TaxRate.GST_18).forEach { taxRate ->
                        FilterChip(
                            selected = estimate.taxRate == taxRate.rate,
                            onClick = { onTaxRateChange(taxRate.rate) },
                            label = {
                                Text(taxRate.displayName, style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (estimate.taxRate == 0.0) "" else estimate.taxRate.toString(),
                    onValueChange = { onTaxRateChange(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Custom Tax Rate (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        Text(
                            "%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(12.dp))

        // ---- Grand Total Summary Box ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                .padding(12.dp)
        ) {
            Column {
                AmountRow(
                    label = "Subtotal",
                    amount = CurrencyFormatter.format(estimate.subtotal)
                )
                if (estimate.discountAmount > 0) {
                    AmountRow(
                        label = "Discount${if (estimate.discountType == DiscountType.PERCENTAGE) " (${estimate.discountValue}%)" else ""}",
                        amount = "- ${CurrencyFormatter.format(estimate.discountAmount)}",
                        amountColor = SuccessGreen
                    )
                }
                if (estimate.taxEnabled && estimate.taxAmount > 0) {
                    AmountRow(
                        label = "GST (${estimate.taxRate}%)",
                        amount = "+ ${CurrencyFormatter.format(estimate.taxAmount)}"
                    )
                }
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(Modifier.height(6.dp))
                AmountRow(
                    label = "Grand Total",
                    amount = CurrencyFormatter.format(estimate.grandTotal),
                    isTotal = true
                )
            }
        }
    }
}
