package com.eventquote.app.model

/**
 * Status of an estimate/quotation with display properties.
 */
enum class EstimateStatus(
    val displayName: String,
    val colorHex: String
) {
    DRAFT("Draft", "#9E9E9E"),
    QUOTATION_SENT("Quotation Sent", "#2196F3"),
    CONFIRMED("Confirmed", "#4CAF50"),
    CANCELLED("Cancelled", "#F44336"),
    COMPLETED("Completed", "#9C27B0")
}

/**
 * Type of event being organized.
 */
enum class EventType(val displayName: String) {
    WEDDING("Wedding"),
    ENGAGEMENT("Engagement"),
    RECEPTION("Reception"),
    BIRTHDAY("Birthday"),
    HOUSE_WARMING("House Warming"),
    CORPORATE("Corporate Event"),
    ANNIVERSARY("Anniversary"),
    BABY_SHOWER("Baby Shower"),
    OTHER("Other")
}

/**
 * How discount is applied — fixed rupee amount or percentage.
 */
enum class DiscountType(val displayName: String) {
    FIXED("Fixed Amount (₹)"),
    PERCENTAGE("Percentage (%)")
}

/**
 * GST rate options.
 */
enum class TaxRate(val displayName: String, val rate: Double) {
    NONE("No Tax", 0.0),
    GST_5("GST 5%", 5.0),
    GST_12("GST 12%", 12.0),
    GST_18("GST 18%", 18.0),
    CUSTOM("Custom", -1.0)  // -1 means user enters custom value
}

/**
 * Available PDF templates.
 */
enum class PdfTemplate(val displayName: String) {
    ELEGANT("Elegant"),
    MODERN("Modern"),
    MINIMAL("Minimal"),
    TRADITIONAL("Traditional")
}
