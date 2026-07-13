package com.eventquote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.ServiceMasterRepository
import com.eventquote.app.model.ServiceMaster
import com.eventquote.app.model.SubItemTemplate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ServiceMasterUiState(
    val services: List<ServiceMaster> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Service Master management screen.
 */
class ServiceMasterViewModel(
    private val repository: ServiceMasterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceMasterUiState())
    val uiState: StateFlow<ServiceMasterUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    repository.searchServices(query)
                }
                .collect { services ->
                    _uiState.update { it.copy(services = services, isLoading = false) }
                }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addService(name: String, description: String = "", defaultAmount: Double = 0.0) {
        viewModelScope.launch {
            val service = ServiceMaster(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                defaultAmount = defaultAmount,
                sortOrder = _uiState.value.services.size
            )
            repository.saveService(service)
        }
    }

    fun updateService(service: ServiceMaster) {
        viewModelScope.launch { repository.saveService(service) }
    }

    fun deleteService(id: String) {
        viewModelScope.launch { repository.deleteService(id) }
    }

    fun toggleFavorite(id: String, current: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(id, !current) }
    }

    fun addSubItemToService(service: ServiceMaster, subItemName: String) {
        val newTemplate = SubItemTemplate(
            id = UUID.randomUUID().toString(),
            name = subItemName
        )
        val updated = service.copy(defaultSubItems = service.defaultSubItems + newTemplate)
        viewModelScope.launch { repository.saveService(updated) }
    }

    fun removeSubItemFromService(service: ServiceMaster, subItemId: String) {
        val updated = service.copy(
            defaultSubItems = service.defaultSubItems.filter { it.id != subItemId }
        )
        viewModelScope.launch { repository.saveService(updated) }
    }

    fun reorderServices(from: Int, to: Int) {
        val current = _uiState.value.services.toMutableList()
        if (from < 0 || to < 0 || from >= current.size || to >= current.size) return
        val item = current.removeAt(from)
        current.add(to, item)
        _uiState.update { it.copy(services = current) }
        viewModelScope.launch {
            repository.reorderServices(current)
        }
    }
}
