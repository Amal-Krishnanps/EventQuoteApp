package com.eventquote.app.model

/**
 * Full domain model for an Estimate/Quotation.
 * This is the in-memory representation used by ViewModels and UI.
 */
data class Estimate(
    val id: String = "",
    val estimateNumber: String = "",
    val quotationDate: Long = System.currentTimeMillis(),
    val validityDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,

    // Customer
    val customerName: String = "",
    val customerAddress: String = "",
    val customerCity: String = "",
    val customerDistrict: String = "",
    val customerState: String = "",
    val customerPincode: String = "",
    val customerMobile: String = "",
    val customerWhatsApp: String = "",
    val customerEmail: String = "",

    // Event
    val eventType: EventType = EventType.WEDDING,
    val functionDate: Long = System.currentTimeMillis(),
    val functionTime: String = "",
    val venueName: String = "",
    val venueAddress: String = "",
    val eventCoordinator: String = "",
    val guestCount: Int = 0,
    val eventNotes: String = "",

    // Services
    val services: List<EstimateService> = emptyList(),

    // Pricing
    val discountType: DiscountType = DiscountType.FIXED,
    val discountValue: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxRate: Double = 0.0,         // actual percentage, e.g. 18.0 for 18%

    // Advance Payments
    val advancePayments: List<AdvancePayment> = emptyList(),

    // Terms & Notes
    val termsConditions: String = "",
    val notes: String = "",

    // Attachments (file paths stored locally)
    val attachments: List<String> = emptyList(),

    // Meta
    val status: EstimateStatus = EstimateStatus.DRAFT,
    val templateId: PdfTemplate = PdfTemplate.ELEGANT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Sum of all service amounts */
    val subtotal: Double
        get() = services.sumOf { it.totalCost }

    /** Calculated discount amount */
    val discountAmount: Double
        get() = when (discountType) {
            DiscountType.FIXED -> discountValue
            DiscountType.PERCENTAGE -> subtotal * discountValue / 100.0
        }

    /** Amount after discount */
    val amountAfterDiscount: Double
        get() = subtotal - discountAmount

    /** Calculated tax amount */
    val taxAmount: Double
        get() = if (taxEnabled && taxRate > 0) amountAfterDiscount * taxRate / 100.0 else 0.0

    /** Grand total = after discount + tax */
    val grandTotal: Double
        get() = amountAfterDiscount + taxAmount

    /** Sum of all advance payments */
    val totalAdvancePaid: Double
        get() = advancePayments.sumOf { it.amount }

    /** Balance remaining */
    val balanceAmount: Double
        get() = grandTotal - totalAdvancePaid
}
