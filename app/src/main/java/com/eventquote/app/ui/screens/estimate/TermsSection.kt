package com.eventquote.app.ui.screens.estimate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.Estimate
import com.eventquote.app.ui.components.SectionCard

/**
 * Terms & Conditions and Notes section.
 */
@Composable
fun TermsSection(
    estimate: Estimate,
    onTermsChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Terms & Conditions",
        icon = Icons.Default.Gavel,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = estimate.termsConditions,
            onValueChange = onTermsChange,
            label = { Text("Terms & Conditions") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 15,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = estimate.notes,
            onValueChange = onNotesChange,
            label = { Text("Additional Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 8,
            leadingIcon = { Icon(Icons.Default.StickyNote2, null, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
