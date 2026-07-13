package com.eventquote.app.ui.screens.company

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eventquote.app.ui.components.*
import com.eventquote.app.ui.theme.SuccessGreen
import com.eventquote.app.viewmodel.CompanyViewModel

@Composable
fun CompanySettingsScreen(
    viewModel: CompanyViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Company settings saved!")
            viewModel.clearSavedFlag()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }

    // Image pickers
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.pickLogo(context, it) }
    }
    val qrPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.pickQrCode(context, it) }
    }
    val signaturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.pickSignature(context, it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Company Settings", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                GradientHeader(
                    title = "Company Settings",
                    subtitle = "Configure your company details for quotations",
                    onBack = onNavigateBack
                )
            }

            // ---- Company Logo ----
            item {
                SectionCard(
                    title = "Company Logo",
                    icon = Icons.Default.Image,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { logoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.settings.logoPath.isNotBlank()) {
                                AsyncImage(
                                    model = uiState.settings.logoPath,
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null,
                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Text("Add Logo", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Company Logo", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium)
                            Text("Appears on every PDF quotation", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { logoPicker.launch("image/*") },
                                shape = RoundedCornerShape(8.dp)) {
                                Text(if (uiState.settings.logoPath.isBlank()) "Upload Logo" else "Change Logo",
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // ---- Basic Info ----
            item {
                SectionCard(
                    title = "Basic Information",
                    icon = Icons.Default.Business,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FormTextField(
                        label = "Company Name *",
                        value = uiState.settings.name,
                        onValueChange = viewModel::updateName,
                        leadingIcon = Icons.Default.Business
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Address",
                        value = uiState.settings.address,
                        onValueChange = viewModel::updateAddress,
                        leadingIcon = Icons.Default.LocationOn,
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Phone Number *",
                        value = uiState.settings.phone,
                        onValueChange = viewModel::updatePhone,
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "WhatsApp Number",
                        value = uiState.settings.whatsAppNumber,
                        onValueChange = viewModel::updateWhatsApp,
                        leadingIcon = Icons.Default.Chat,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Email Address",
                        value = uiState.settings.email,
                        onValueChange = viewModel::updateEmail,
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Website (Optional)",
                        value = uiState.settings.website,
                        onValueChange = viewModel::updateWebsite,
                        leadingIcon = Icons.Default.Language
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "GST Number (Optional)",
                        value = uiState.settings.gstNumber,
                        onValueChange = viewModel::updateGst,
                        leadingIcon = Icons.Default.Receipt
                    )
                }
            }

            // ---- Bank Details ----
            item {
                SectionCard(
                    title = "Bank Details",
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FormTextField(
                        label = "Bank Name",
                        value = uiState.settings.bankName,
                        onValueChange = viewModel::updateBankName,
                        leadingIcon = Icons.Default.AccountBalance
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Account Holder Name",
                        value = uiState.settings.accountHolderName,
                        onValueChange = viewModel::updateAccountHolderName,
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "Account Number",
                        value = uiState.settings.accountNumber,
                        onValueChange = viewModel::updateAccountNumber,
                        leadingIcon = Icons.Default.Numbers,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(10.dp))
                    FormTextField(
                        label = "IFSC Code",
                        value = uiState.settings.ifscCode,
                        onValueChange = viewModel::updateIfscCode,
                        leadingIcon = Icons.Default.Code
                    )
                }
            }

            // ---- QR Code & Signature ----
            item {
                SectionCard(
                    title = "QR Code & Signature",
                    icon = Icons.Default.QrCode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // QR Code
                        ImagePickerBox(
                            label = "Payment QR Code",
                            imagePath = uiState.settings.qrCodePath,
                            onPick = { qrPicker.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                        // Signature
                        ImagePickerBox(
                            label = "Signature",
                            imagePath = uiState.settings.signaturePath,
                            onPick = { signaturePicker.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ---- Default Terms ----
            item {
                SectionCard(
                    title = "Default Terms & Conditions",
                    icon = Icons.Default.Description,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        "This text will be auto-filled in every new estimate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.settings.defaultTerms,
                        onValueChange = viewModel::updateDefaultTerms,
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
                }
            }
        }
    }
}

@Composable
private fun ImagePickerBox(
    label: String,
    imagePath: String,
    onPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (imagePath.isNotBlank()) {
                AsyncImage(imagePath, contentDescription = label,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
