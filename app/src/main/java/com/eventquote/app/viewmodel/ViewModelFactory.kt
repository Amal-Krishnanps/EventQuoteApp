package com.eventquote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eventquote.app.data.repository.CompanyRepository
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.data.repository.ServiceMasterRepository

/**
 * Manual DI: ViewModelFactory that provides all repositories to ViewModels.
 * Pass this factory to viewModel() composable calls.
 */
class ViewModelFactory(
    private val estimateRepository: EstimateRepository,
    private val companyRepository: CompanyRepository,
    private val serviceMasterRepository: ServiceMasterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(estimateRepository) as T

            modelClass.isAssignableFrom(EstimateViewModel::class.java) ->
                EstimateViewModel(estimateRepository, serviceMasterRepository, companyRepository) as T

            modelClass.isAssignableFrom(CompanyViewModel::class.java) ->
                CompanyViewModel(companyRepository) as T

            modelClass.isAssignableFrom(ServiceMasterViewModel::class.java) ->
                ServiceMasterViewModel(serviceMasterRepository) as T

            modelClass.isAssignableFrom(SavedEstimatesViewModel::class.java) ->
                SavedEstimatesViewModel(estimateRepository) as T

            modelClass.isAssignableFrom(BackupViewModel::class.java) ->
                BackupViewModel(estimateRepository, companyRepository, serviceMasterRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
