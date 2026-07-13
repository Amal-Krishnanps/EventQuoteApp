package com.eventquote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.EstimateStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    BY_EVENT_DATE("By Event Date"),
    BY_BALANCE("By Balance")
}

data class SavedEstimatesUiState(
    val estimates: List<Estimate> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedStatus: EstimateStatus? = null,
    val sortOption: SortOption = SortOption.NEWEST,
    val error: String? = null,
    val deleteSuccess: Boolean = false,
    val duplicateSuccess: String? = null  // ID of duplicated estimate
)

/**
 * ViewModel for the Saved Estimates list screen.
 */
@OptIn(FlowPreview::class)
class SavedEstimatesViewModel(
    private val estimateRepository: EstimateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedEstimatesUiState())
    val uiState: StateFlow<SavedEstimatesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<EstimateStatus?>(null)
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)

    init {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _statusFilter,
                _sortOption
            ) { query, status, sort ->
                Triple(query, status, sort)
            }.flatMapLatest { (query, status, sort) ->
                val baseFlow = when {
                    query.isNotBlank() -> estimateRepository.searchEstimates(query)
                    status != null -> estimateRepository.getEstimatesByStatus(status)
                    else -> estimateRepository.allEstimates
                }
                baseFlow.map { list ->
                    when (sort) {
                        SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
                        SortOption.OLDEST -> list.sortedBy { it.createdAt }
                        SortOption.BY_EVENT_DATE -> list.sortedBy { it.functionDate }
                        SortOption.BY_BALANCE -> list.sortedByDescending { it.balanceAmount }
                    }
                }
            }.collect { estimates ->
                _uiState.update { it.copy(estimates = estimates, isLoading = false) }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun filterByStatus(status: EstimateStatus?) {
        _statusFilter.value = status
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun sort(option: SortOption) {
        _sortOption.value = option
        _uiState.update { it.copy(sortOption = option) }
    }

    fun deleteEstimate(id: String) {
        viewModelScope.launch {
            runCatching {
                estimateRepository.deleteEstimate(id)
                _uiState.update { it.copy(deleteSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun duplicateEstimate(id: String) {
        viewModelScope.launch {
            runCatching {
                val duplicate = estimateRepository.duplicateEstimate(id)
                _uiState.update { it.copy(duplicateSuccess = duplicate?.id) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateStatus(id: String, status: EstimateStatus) {
        viewModelScope.launch {
            estimateRepository.updateStatus(id, status)
        }
    }

    fun clearDeleteSuccess() = _uiState.update { it.copy(deleteSuccess = false) }
    fun clearDuplicateSuccess() = _uiState.update { it.copy(duplicateSuccess = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
