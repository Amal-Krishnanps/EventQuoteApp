package com.eventquote.app.ui.screens.estimate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.Estimate
import com.eventquote.app.ui.components.*

/**
 * Customer details section of the estimate form.
 */
@Composable
fun CustomerSection(
    estimate: Estimate,
    validationErrors: Map<String, String>,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onPincodeChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onWhatsAppChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onClearError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Customer Details",
        icon = Icons.Default.Person,
        modifier = modifier
    ) {
        // Customer Name
        FormTextField(
            label = "Customer Name *",
            value = estimate.customerName,
            onValueChange = { onNameChange(it); onClearError("customerName") },
            isError = validationErrors.containsKey("customerName"),
            errorMessage = validationErrors["customerName"],
            leadingIcon = Icons.Default.Person
        )
        Spacer(Modifier.height(10.dp))

        // Address
        FormTextField(
            label = "Address",
            value = estimate.customerAddress,
            onValueChange = onAddressChange,
            leadingIcon = Icons.Default.Home,
            singleLine = false,
            maxLines = 2
        )
        Spacer(Modifier.height(10.dp))

        // City + District
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormTextField(
                label = "City",
                value = estimate.customerCity,
                onValueChange = onCityChange,
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                label = "District",
                value = estimate.customerDistrict,
                onValueChange = onDistrictChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))

        // State + Pincode
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormTextField(
                label = "State",
                value = estimate.customerState,
                onValueChange = onStateChange,
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                label = "Pincode",
                value = estimate.customerPincode,
                onValueChange = onPincodeChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))

        // Mobile
        FormTextField(
            label = "Mobile Number *",
            value = estimate.customerMobile,
            onValueChange = { onMobileChange(it); onClearError("customerMobile") },
            isError = validationErrors.containsKey("customerMobile"),
            errorMessage = validationErrors["customerMobile"],
            keyboardType = KeyboardType.Phone,
            leadingIcon = Icons.Default.Phone
        )

        // "Same as Mobile" toggle for WhatsApp
        val sameAsMobile = estimate.customerWhatsApp == estimate.customerMobile &&
            estimate.customerMobile.isNotBlank()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sameAsMobile,
                onCheckedChange = { checked ->
                    if (checked) onWhatsAppChange(estimate.customerMobile)
                    else onWhatsAppChange("")
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "WhatsApp same as Mobile",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // WhatsApp number (editable only when not "same as mobile")
        FormTextField(
            label = "WhatsApp Number",
            value = estimate.customerWhatsApp,
            onValueChange = onWhatsAppChange,
            keyboardType = KeyboardType.Phone,
            leadingIcon = Icons.Outlined.Phone,
            placeholder = "Same as mobile if blank",
            enabled = !sameAsMobile
        )
        Spacer(Modifier.height(10.dp))

        // Email
        FormTextField(
            label = "Email (Optional)",
            value = estimate.customerEmail,
            onValueChange = onEmailChange,
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Default.Email
        )
    }
}
