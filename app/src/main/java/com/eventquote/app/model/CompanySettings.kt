package com.eventquote.app.model

/**
 * Full domain model for a Company's settings.
 * Configured once and stamped on every PDF.
 */
data class CompanySettings(
    val id: Int = 1,
    val name: String = "",
    val logoPath: String = "",       // absolute path to copied logo image
    val address: String = "",
    val phone: String = "",
    val whatsAppNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val website: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val accountHolderName: String = "",
    val qrCodePath: String = "",     // absolute path to QR code image
    val signaturePath: String = "",  // absolute path to signature image
    val defaultTerms: String = DEFAULT_TERMS
) {
    companion object {
        const val DEFAULT_TERMS = """1. This quotation is valid for 30 days from the date of issue.
2. 50% advance payment is required to confirm the booking.
3. Balance payment must be cleared before the event date.
4. Cancellation policy: 50% of advance is non-refundable.
5. Any additional requirements will be charged separately.
6. The company is not responsible for delays caused by unforeseen circumstances.
7. All disputes are subject to local jurisdiction."""
    }
}
