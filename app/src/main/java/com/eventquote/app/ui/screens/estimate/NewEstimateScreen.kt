package com.eventquote.app.ui.screens.estimate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.EstimateStatus
import com.eventquote.app.model.PdfTemplate
import com.eventquote.app.model.Estimate
import com.eventquote.app.pdf.PdfGenerator
import com.eventquote.app.ui.components.GradientHeader
import com.eventquote.app.ui.components.LoadingOverlay
import com.eventquote.app.ui.components.StatusChip
import com.eventquote.app.ui.components.SectionCard
import com.eventquote.app.ui.theme.SuccessGreen
import com.eventquote.app.utils.DateUtils
import com.eventquote.app.utils.ShareUtils
import com.eventquote.app.viewmodel.EstimateViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEstimateScreen(
    viewModel: EstimateViewModel,
    estimateId: String?,
    onNavigateBack: () -> Unit,
    onEstimateSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val companySettings by viewModel.companySettings.collectAsState()
    val estimate = uiState.estimate
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isPdfGenerating by remember { mutableStateOf(false) }

    // Attachment picker
    val attachmentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachment(context, it) }
    }

    // Init on first composition
    LaunchedEffect(estimateId) {
        if (estimateId != null) {
            viewModel.loadEstimate(estimateId)
        } else {
            viewModel.initNewEstimate()
        }
    }

    // Show validation errors
    LaunchedEffect(uiState.validationErrors) {
        if (uiState.validationErrors.isNotEmpty()) {
            snackbarHostState.showSnackbar("Please fix the highlighted fields")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            EstimateBottomBar(
                isLoading = uiState.isLoading || isPdfGenerating,
                onSave = {
                    viewModel.saveEstimate { saved ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("✓ Saved: ${saved.estimateNumber}")
                        }
                    }
                },
                onGeneratePdf = {
                    viewModel.saveEstimate { saved ->
                        coroutineScope.launch {
                            isPdfGenerating = true
                            try {
                                val pdfFile = PdfGenerator.generate(context, saved, companySettings)
                                if (pdfFile != null) {
                                    ShareUtils.sharePdf(context, pdfFile, "Quotation ${saved.estimateNumber}")
                                } else {
                                    snackbarHostState.showSnackbar("PDF generation failed")
                                }
                            } finally {
                                isPdfGenerating = false
                            }
                        }
                    }
                },
                onShareWhatsApp = {
                    viewModel.saveEstimate { saved ->
                        coroutineScope.launch {
                            isPdfGenerating = true
                            try {
                                val pdfFile = PdfGenerator.generate(context, saved, companySettings)
                                pdfFile?.let {
                                    val number = saved.customerWhatsApp.ifBlank { saved.customerMobile }
                                    ShareUtils.shareViaWhatsApp(context, it, number)
                                } ?: snackbarHostState.showSnackbar("PDF generation failed")
                            } finally {
                                isPdfGenerating = false
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ---- Header ----
            item {
                GradientHeader(
                    title = if (estimateId == null) "New Estimate" else "Edit Estimate",
                    subtitle = estimate.estimateNumber.ifBlank { "Number auto-generated on save" },
                    onBack = onNavigateBack,
                    actions = {
                        var showStatusMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showStatusMenu = true }) {
                                Icon(Icons.Default.SwapHoriz, null, tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                EstimateStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.displayName) },
                                        onClick = {
                                            viewModel.updateStatus(status)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // ---- Meta Card (Number, Dates, Template) ----
            item {
                EstimateMetaCard(
                    estimate = estimate,
                    onTemplateChange = viewModel::updateTemplate,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 1. Customer ----
            item {
                CustomerSection(
                    estimate = estimate,
                    validationErrors = uiState.validationErrors,
                    onNameChange = viewModel::updateCustomerName,
                    onAddressChange = viewModel::updateCustomerAddress,
                    onCityChange = viewModel::updateCustomerCity,
                    onDistrictChange = viewModel::updateCustomerDistrict,
                    onStateChange = viewModel::updateCustomerState,
                    onPincodeChange = viewModel::updateCustomerPincode,
                    onMobileChange = viewModel::updateCustomerMobile,
                    onWhatsAppChange = viewModel::updateCustomerWhatsApp,
                    onEmailChange = viewModel::updateCustomerEmail,
                    onClearError = viewModel::clearValidationError,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 2. Event ----
            item {
                EventSection(
                    estimate = estimate,
                    validationErrors = uiState.validationErrors,
                    onEventTypeChange = viewModel::updateEventType,
                    onFunctionDateChange = viewModel::updateFunctionDate,
                    onFunctionTimeChange = viewModel::updateFunctionTime,
                    onVenueNameChange = viewModel::updateVenueName,
                    onVenueAddressChange = viewModel::updateVenueAddress,
                    onCoordinatorChange = viewModel::updateEventCoordinator,
                    onGuestCountChange = viewModel::updateGuestCount,
                    onEventNotesChange = viewModel::updateEventNotes,
                    onClearError = viewModel::clearValidationError,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 3. Services ----
            item {
                ServicesSection(
                    estimate = estimate,
                    availableMasterServices = uiState.availableMasterServices,
                    validationErrors = uiState.validationErrors,
                    onAddService = { viewModel.addService() },
                    onAddServiceFromMaster = viewModel::addServiceFromMaster,
                    onUpdateService = viewModel::updateService,
                    onRemoveService = viewModel::removeService,
                    onToggleExpand = viewModel::toggleServiceExpanded,
                    onAddSubItem = viewModel::addSubItem,
                    onUpdateSubItem = viewModel::updateSubItem,
                    onRemoveSubItem = viewModel::removeSubItem,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 4. Amounts ----
            item {
                AmountSection(
                    estimate = estimate,
                    onDiscountTypeChange = viewModel::updateDiscountType,
                    onDiscountValueChange = viewModel::updateDiscountValue,
                    onTaxEnabledChange = viewModel::updateTaxEnabled,
                    onTaxRateChange = viewModel::updateTaxRate,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 5. Advance Payment ----
            item {
                AdvanceSection(
                    estimate = estimate,
                    onAddPayment = viewModel::addAdvancePayment,
                    onUpdatePayment = viewModel::updateAdvancePayment,
                    onRemovePayment = viewModel::removeAdvancePayment,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 6. Terms & Notes ----
            item {
                TermsSection(
                    estimate = estimate,
                    onTermsChange = viewModel::updateTermsConditions,
                    onNotesChange = viewModel::updateNotes,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ---- 7. Attachments ----
            item {
                AttachmentsSection(
                    attachments = estimate.attachments,
                    onAddAttachment = { attachmentPicker.launch("image/*") },
                    onRemoveAttachment = viewModel::removeAttachment,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    // PDF generating overlay
    if (isPdfGenerating) {
        LoadingOverlay("Generating PDF...")
    }
}

// ---- Meta Card ----

@Composable
private fun EstimateMetaCard(
    estimate: Estimate,
    onTemplateChange: (PdfTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Estimate #",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        estimate.estimateNumber.ifBlank { "Auto-generated" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusChip(estimate.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Quotation Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        DateUtils.formatDate(estimate.quotationDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Valid Until",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        DateUtils.formatDate(estimate.validityDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "PDF Template",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PdfTemplate.entries.forEach { template ->
                    FilterChip(
                        selected = estimate.templateId == template,
                        onClick = { onTemplateChange(template) },
                        label = {
                            Text(template.displayName, style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }
        }
    }
}

// ---- Bottom Action Bar ----

@Composable
private fun EstimateBottomBar(
    isLoading: Boolean,
    onSave: () -> Unit,
    onGeneratePdf: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onGeneratePdf,
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PDF", fontWeight = FontWeight.SemiBold)
                }
            }
            Button(
                onClick = onShareWhatsApp,
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("WhatsApp", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---- Attachments Section ----

@Composable
private fun AttachmentsSection(
    attachments: List<String>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Attachments (${attachments.size})",
        icon = Icons.Default.AttachFile,
        modifier = modifier
    ) {
        if (attachments.isEmpty()) {
            Text(
                "No attachments yet. Add venue photos, reference images, etc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            attachments.forEach { path ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Image, null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        path.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onRemoveAttachment(path) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onAddAttachment,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Attachment")
        }
    }
}
