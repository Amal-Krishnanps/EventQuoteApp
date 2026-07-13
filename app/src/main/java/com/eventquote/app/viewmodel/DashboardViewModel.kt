package com.eventquote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.EstimateStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardStats(
    val totalEstimates: Int = 0,
    val confirmedEstimates: Int = 0,
    val pendingBalance: Double = 0.0,
    val todaysEvents: Int = 0,
    val thisMonthRevenue: Double = 0.0,
    val draftCount: Int = 0,
    val completedCount: Int = 0
)

/**
 * ViewModel for the Dashboard screen.
 * Aggregates statistics from all estimates.
 */
class DashboardViewModel(
    private val estimateRepository: EstimateRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    val recentEstimates: StateFlow<List<Estimate>> = estimateRepository.allEstimates
        .map { list -> list.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEstimates: StateFlow<List<Estimate>> = estimateRepository.allEstimates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            estimateRepository.allEstimates.collect { estimates ->
                val today = Calendar.getInstance()
                val todayStart = getStartOfDay(today)
                val todayEnd = getEndOfDay(today)

                val monthStart = getStartOfMonth(today)
                val monthEnd = getEndOfMonth(today)

                _stats.value = DashboardStats(
                    totalEstimates = estimates.size,
                    confirmedEstimates = estimates.count { it.status == EstimateStatus.CONFIRMED },
                    pendingBalance = estimates
                        .filter { it.status != EstimateStatus.COMPLETED && it.status != EstimateStatus.CANCELLED }
                        .sumOf { it.balanceAmount }
                        .coerceAtLeast(0.0),
                    todaysEvents = estimates.count { estimate ->
                        estimate.functionDate in todayStart..todayEnd
                    },
                    thisMonthRevenue = estimates
                        .filter { it.createdAt in monthStart..monthEnd }
                        .sumOf { it.grandTotal },
                    draftCount = estimates.count { it.status == EstimateStatus.DRAFT },
                    completedCount = estimates.count { it.status == EstimateStatus.COMPLETED }
                )
            }
        }
    }

    private fun getStartOfDay(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun getStartOfMonth(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonth(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
