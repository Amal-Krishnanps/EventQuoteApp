package com.eventquote.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a single advance payment entry.
 * Multiple entries can be added per estimate (Cash, UPI, Card, etc.)
 */
@Serializable
data class AdvancePayment(
    val id: String,
    val mode: PaymentMode = PaymentMode.CASH,
    val amount: Double = 0.0,
    val reference: String = "",
    val date: String = ""
)

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque"),
    OTHER("Other")
}
