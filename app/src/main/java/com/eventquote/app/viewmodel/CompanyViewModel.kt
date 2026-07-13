package com.eventquote.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.CompanyRepository
import com.eventquote.app.model.CompanySettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CompanyUiState(
    val settings: CompanySettings = CompanySettings(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Company Settings screen.
 */
class CompanyViewModel(
    private val repository: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.companySettings.collect { settings ->
                _uiState.update { it.copy(settings = settings ?: CompanySettings(), isLoading = false) }
            }
        }
    }

    fun updateName(value: String) = update { copy(name = value) }
    fun updateAddress(value: String) = update { copy(address = value) }
    fun updatePhone(value: String) = update { copy(phone = value) }
    fun updateWhatsApp(value: String) = update { copy(whatsAppNumber = value) }
    fun updateEmail(value: String) = update { copy(email = value) }
    fun updateGst(value: String) = update { copy(gstNumber = value) }
    fun updateWebsite(value: String) = update { copy(website = value) }
    fun updateBankName(value: String) = update { copy(bankName = value) }
    fun updateAccountNumber(value: String) = update { copy(accountNumber = value) }
    fun updateIfscCode(value: String) = update { copy(ifscCode = value) }
    fun updateAccountHolderName(value: String) = update { copy(accountHolderName = value) }
    fun updateDefaultTerms(value: String) = update { copy(defaultTerms = value) }

    fun pickLogo(context: Context, uri: Uri) {
        viewModelScope.launch {
            val path = saveImageToAppStorage(context, uri, "logos") ?: return@launch
            update { copy(logoPath = path) }
        }
    }

    fun pickQrCode(context: Context, uri: Uri) {
        viewModelScope.launch {
            val path = saveImageToAppStorage(context, uri, "qr_codes") ?: return@launch
            update { copy(qrCodePath = path) }
        }
    }

    fun pickSignature(context: Context, uri: Uri) {
        viewModelScope.launch {
            val path = saveImageToAppStorage(context, uri, "signatures") ?: return@launch
            update { copy(signaturePath = path) }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.saveCompanySettings(_uiState.value.settings)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearSavedFlag() = _uiState.update { it.copy(isSaved = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun update(transform: CompanySettings.() -> CompanySettings) {
        _uiState.update { it.copy(settings = it.settings.transform()) }
    }

    private suspend fun saveImageToAppStorage(context: Context, uri: Uri, subdir: String): String? {
        return runCatching {
            val dir = File(context.filesDir, subdir).also { it.mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }.getOrNull()
    }
}
