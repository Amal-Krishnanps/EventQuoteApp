package com.eventquote.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.CompanyRepository
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.data.repository.ServiceMasterRepository
import com.eventquote.app.utils.BackupUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: String? = null,   // file path
    val importSuccess: Boolean = false,
    val error: String? = null,
    val estimateCount: Int = 0,
    val lastBackupDate: String? = null
)

/**
 * ViewModel for Backup & Restore screen.
 */
class BackupViewModel(
    private val estimateRepository: EstimateRepository,
    private val companyRepository: CompanyRepository,
    private val serviceMasterRepository: ServiceMasterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            estimateRepository.allEstimates.collect { estimates ->
                _uiState.update { it.copy(estimateCount = estimates.size) }
            }
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            runCatching {
                val path = BackupUtils.exportToJson(
                    context,
                    estimateRepository.allEstimates.first(),
                    companyRepository.getCompanySettingsOnce(),
                    serviceMasterRepository.getAllServicesOnce()
                )
                _uiState.update { it.copy(isExporting = false, exportSuccess = path) }
            }.onFailure { e ->
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun importJson(context: Context, filePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            runCatching {
                BackupUtils.importFromJson(
                    context, filePath,
                    estimateRepository, companyRepository, serviceMasterRepository
                )
                _uiState.update { it.copy(isImporting = false, importSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isImporting = false, error = e.message) }
            }
        }
    }

    fun clearExportSuccess() = _uiState.update { it.copy(exportSuccess = null) }
    fun clearImportSuccess() = _uiState.update { it.copy(importSuccess = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
