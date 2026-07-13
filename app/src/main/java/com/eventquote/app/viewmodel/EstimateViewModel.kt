package com.eventquote.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.CompanyRepository
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.data.repository.ServiceMasterRepository
import com.eventquote.app.model.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * UI state for the estimate creation/editing screen.
 */
data class EstimateUiState(
    val estimate: Estimate = Estimate(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val availableMasterServices: List<ServiceMaster> = emptyList()
)

/**
 * ViewModel managing the full lifecycle of estimate creation and editing.
 * All changes auto-save via debounced StateFlow.
 */
@OptIn(FlowPreview::class)
class EstimateViewModel(
    private val estimateRepository: EstimateRepository,
    private val serviceMasterRepository: ServiceMasterRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstimateUiState())
    val uiState: StateFlow<EstimateUiState> = _uiState.asStateFlow()

    val companySettings: StateFlow<CompanySettings?> = companyRepository.companySettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Load master services for the "pick from list" dialog
        viewModelScope.launch {
            serviceMasterRepository.allServices.collect { masters ->
                _uiState.update { it.copy(availableMasterServices = masters) }
            }
        }

        // Auto-save: debounce 1.5 seconds after last edit
        viewModelScope.launch {
            _uiState
                .map { it.estimate }
                .distinctUntilChanged()
                .debounce(1500)
                .filter { it.id.isNotEmpty() }
                .collect { estimate ->
                    autoSave(estimate)
                }
        }
    }

    /** Load an existing estimate for editing */
    fun loadEstimate(estimateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val estimate = estimateRepository.getEstimateById(estimateId)
            if (estimate != null) {
                // Get company default terms if estimate has no terms
                val finalEstimate = if (estimate.termsConditions.isEmpty()) {
                    val company = companyRepository.getCompanySettingsOnce()
                    estimate.copy(termsConditions = company?.defaultTerms ?: CompanySettings.DEFAULT_TERMS)
                } else estimate
                _uiState.update { it.copy(estimate = finalEstimate, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Initialize a new blank estimate */
    fun initNewEstimate() {
        viewModelScope.launch {
            val company = companyRepository.getCompanySettingsOnce()
            _uiState.update {
                it.copy(
                    estimate = Estimate(
                        termsConditions = company?.defaultTerms ?: CompanySettings.DEFAULT_TERMS
                    )
                )
            }
        }
    }

    // ---- Customer Updates ----

    fun updateCustomerName(value: String) = updateEstimate { copy(customerName = value) }
    fun updateCustomerAddress(value: String) = updateEstimate { copy(customerAddress = value) }
    fun updateCustomerCity(value: String) = updateEstimate { copy(customerCity = value) }
    fun updateCustomerDistrict(value: String) = updateEstimate { copy(customerDistrict = value) }
    fun updateCustomerState(value: String) = updateEstimate { copy(customerState = value) }
    fun updateCustomerPincode(value: String) = updateEstimate { copy(customerPincode = value) }
    fun updateCustomerMobile(value: String) = updateEstimate { copy(customerMobile = value) }
    fun updateCustomerWhatsApp(value: String) = updateEstimate { copy(customerWhatsApp = value) }
    fun updateCustomerEmail(value: String) = updateEstimate { copy(customerEmail = value) }

    // ---- Event Updates ----

    fun updateEventType(value: EventType) = updateEstimate { copy(eventType = value) }
    fun updateFunctionDate(value: Long) = updateEstimate { copy(functionDate = value) }
    fun updateFunctionTime(value: String) = updateEstimate { copy(functionTime = value) }
    fun updateVenueName(value: String) = updateEstimate { copy(venueName = value) }
    fun updateVenueAddress(value: String) = updateEstimate { copy(venueAddress = value) }
    fun updateEventCoordinator(value: String) = updateEstimate { copy(eventCoordinator = value) }
    fun updateGuestCount(value: Int) = updateEstimate { copy(guestCount = value) }
    fun updateEventNotes(value: String) = updateEstimate { copy(eventNotes = value) }

    // ---- Services ----

    fun addService(service: EstimateService? = null) {
        val newService = service ?: EstimateService(id = UUID.randomUUID().toString())
        updateEstimate { copy(services = services + newService) }
    }

    fun addServiceFromMaster(master: ServiceMaster) {
        val service = EstimateService(
            id = UUID.randomUUID().toString(),
            name = master.name,
            description = master.description,
            amount = master.defaultAmount,
            subItems = master.defaultSubItems.map { template ->
                SubItem(
                    id = UUID.randomUUID().toString(),
                    name = template.name,
                    description = template.description,
                    cost = template.defaultCost
                )
            }
        )
        updateEstimate { copy(services = services + service) }
    }

    fun updateService(updatedService: EstimateService) {
        updateEstimate {
            copy(services = services.map {
                if (it.id == updatedService.id) updatedService else it
            })
        }
    }

    fun removeService(serviceId: String) {
        updateEstimate { copy(services = services.filter { it.id != serviceId }) }
    }

    fun toggleServiceExpanded(serviceId: String) {
        updateEstimate {
            copy(services = services.map {
                if (it.id == serviceId) it.copy(isExpanded = !it.isExpanded) else it
            })
        }
    }

    // ---- Sub-items ----

    fun addSubItem(serviceId: String) {
        val newSubItem = SubItem(id = UUID.randomUUID().toString())
        updateEstimate {
            copy(services = services.map { service ->
                if (service.id == serviceId)
                    service.copy(subItems = service.subItems + newSubItem)
                else service
            })
        }
    }

    fun updateSubItem(serviceId: String, updatedSubItem: SubItem) {
        updateEstimate {
            copy(services = services.map { service ->
                if (service.id == serviceId)
                    service.copy(subItems = service.subItems.map {
                        if (it.id == updatedSubItem.id) updatedSubItem else it
                    })
                else service
            })
        }
    }

    fun removeSubItem(serviceId: String, subItemId: String) {
        updateEstimate {
            copy(services = services.map { service ->
                if (service.id == serviceId)
                    service.copy(subItems = service.subItems.filter { it.id != subItemId })
                else service
            })
        }
    }

    // ---- Pricing ----

    fun updateDiscountType(type: DiscountType) = updateEstimate { copy(discountType = type) }
    fun updateDiscountValue(value: Double) = updateEstimate { copy(discountValue = value.coerceAtLeast(0.0)) }
    fun updateTaxEnabled(enabled: Boolean) = updateEstimate { copy(taxEnabled = enabled) }
    fun updateTaxRate(rate: Double) = updateEstimate { copy(taxRate = rate.coerceAtLeast(0.0)) }

    // ---- Advance Payments ----

    fun addAdvancePayment() {
        val payment = AdvancePayment(id = UUID.randomUUID().toString())
        updateEstimate { copy(advancePayments = advancePayments + payment) }
    }

    fun updateAdvancePayment(updated: AdvancePayment) {
        updateEstimate {
            copy(advancePayments = advancePayments.map {
                if (it.id == updated.id) updated else it
            })
        }
    }

    fun removeAdvancePayment(id: String) {
        updateEstimate { copy(advancePayments = advancePayments.filter { it.id != id }) }
    }

    // ---- Terms & Notes ----

    fun updateTermsConditions(value: String) = updateEstimate { copy(termsConditions = value) }
    fun updateNotes(value: String) = updateEstimate { copy(notes = value) }

    // ---- Status ----

    fun updateStatus(status: EstimateStatus) = updateEstimate { copy(status = status) }
    fun updateTemplate(template: PdfTemplate) = updateEstimate { copy(templateId = template) }

    // ---- Attachments ----

    fun addAttachment(context: Context, uri: Uri) {
        viewModelScope.launch {
            val savedPath = copyImageToAppStorage(context, uri)
            if (savedPath != null) {
                updateEstimate { copy(attachments = attachments + savedPath) }
            }
        }
    }

    fun removeAttachment(path: String) {
        updateEstimate { copy(attachments = attachments.filter { it != path }) }
        // Optionally delete physical file
        File(path).delete()
    }

    // ---- Save ----

    fun saveEstimate(onSaved: (Estimate) -> Unit = {}) {
        viewModelScope.launch {
            if (!validateEstimate()) return@launch
            _uiState.update { it.copy(isLoading = true) }
            val saved = estimateRepository.saveEstimate(_uiState.value.estimate)
            _uiState.update { it.copy(estimate = saved, isLoading = false, isSaved = true) }
            onSaved(saved)
        }
    }

    private fun autoSave(estimate: Estimate) {
        viewModelScope.launch {
            runCatching { estimateRepository.saveEstimate(estimate) }
        }
    }

    // ---- Validation ----

    private fun validateEstimate(): Boolean {
        val estimate = _uiState.value.estimate
        val errors = mutableMapOf<String, String>()

        if (estimate.customerName.isBlank())
            errors["customerName"] = "Customer name is required"
        if (estimate.customerMobile.isBlank())
            errors["customerMobile"] = "Mobile number is required"
        if (estimate.venueName.isBlank())
            errors["venueName"] = "Venue name is required"
        if (estimate.services.isEmpty())
            errors["services"] = "At least one service is required"

        _uiState.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }

    fun clearValidationError(field: String) {
        _uiState.update {
            it.copy(validationErrors = it.validationErrors - field)
        }
    }

    // ---- Helpers ----

    private fun updateEstimate(transform: Estimate.() -> Estimate) {
        _uiState.update { it.copy(estimate = it.estimate.transform()) }
    }

    private suspend fun copyImageToAppStorage(context: Context, uri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, "attachments").also { it.mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        }.getOrNull()
    }
}
