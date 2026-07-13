package com.eventquote.app.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an event service with optional sub-items.
 * Stored as JSON within the Estimate entity.
 */
@Serializable
data class EstimateService(
    val id: String,
    val name: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val remarks: String = "",
    val isExpanded: Boolean = true,
    val isFavorite: Boolean = false,
    val subItems: List<SubItem> = emptyList()
) {
    /** Returns total cost = service amount + sum of sub-item costs */
    val totalCost: Double
        get() = amount + subItems.sumOf { it.cost }
}

/**
 * Sub-item under a service. E.g. "Breakfast" under "Food".
 */
@Serializable
data class SubItem(
    val id: String,
    val name: String = "",
    val description: String = "",
    val cost: Double = 0.0,
    val remarks: String = ""
)
