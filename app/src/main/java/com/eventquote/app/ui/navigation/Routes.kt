package com.eventquote.app.ui.navigation

/**
 * Type-safe route definitions for Navigation Compose.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val COMPANY_SETTINGS = "company_settings"
    const val NEW_ESTIMATE = "new_estimate"
    const val EDIT_ESTIMATE = "edit_estimate/{estimateId}"
    const val SAVED_ESTIMATES = "saved_estimates"
    const val SERVICES_MASTER = "services_master"
    const val BACKUP = "backup"
    const val ABOUT = "about"

    /** Create a route to edit a specific estimate */
    fun editEstimate(estimateId: String) = "edit_estimate/$estimateId"
}
