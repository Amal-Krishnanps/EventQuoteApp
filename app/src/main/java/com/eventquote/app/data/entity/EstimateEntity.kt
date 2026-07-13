package com.eventquote.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eventquote.app.model.*

/**
 * Room entity for an estimate. Complex lists are stored as JSON via TypeConverters.
 */
@Entity(tableName = "estimates")
data class EstimateEntity(
    @PrimaryKey val id: String,
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

    // Services (JSON via TypeConverter)
    val services: List<EstimateService> = emptyList(),

    // Pricing
    val discountType: DiscountType = DiscountType.FIXED,
    val discountValue: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxRate: Double = 0.0,

    // Advance Payments (JSON via TypeConverter)
    val advancePayments: List<AdvancePayment> = emptyList(),

    // Terms & Notes
    val termsConditions: String = "",
    val notes: String = "",

    // Attachments (JSON list of file paths)
    val attachments: List<String> = emptyList(),

    // Meta
    val status: EstimateStatus = EstimateStatus.DRAFT,
    val templateId: PdfTemplate = PdfTemplate.ELEGANT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ---- Mapper functions ----

fun EstimateEntity.toDomain(): Estimate = Estimate(
    id = id, estimateNumber = estimateNumber,
    quotationDate = quotationDate, validityDate = validityDate,
    customerName = customerName, customerAddress = customerAddress,
    customerCity = customerCity, customerDistrict = customerDistrict,
    customerState = customerState, customerPincode = customerPincode,
    customerMobile = customerMobile, customerWhatsApp = customerWhatsApp,
    customerEmail = customerEmail,
    eventType = eventType, functionDate = functionDate, functionTime = functionTime,
    venueName = venueName, venueAddress = venueAddress,
    eventCoordinator = eventCoordinator, guestCount = guestCount, eventNotes = eventNotes,
    services = services,
    discountType = discountType, discountValue = discountValue,
    taxEnabled = taxEnabled, taxRate = taxRate,
    advancePayments = advancePayments,
    termsConditions = termsConditions, notes = notes, attachments = attachments,
    status = status, templateId = templateId, createdAt = createdAt, updatedAt = updatedAt
)

fun Estimate.toEntity(): EstimateEntity = EstimateEntity(
    id = id, estimateNumber = estimateNumber,
    quotationDate = quotationDate, validityDate = validityDate,
    customerName = customerName, customerAddress = customerAddress,
    customerCity = customerCity, customerDistrict = customerDistrict,
    customerState = customerState, customerPincode = customerPincode,
    customerMobile = customerMobile, customerWhatsApp = customerWhatsApp,
    customerEmail = customerEmail,
    eventType = eventType, functionDate = functionDate, functionTime = functionTime,
    venueName = venueName, venueAddress = venueAddress,
    eventCoordinator = eventCoordinator, guestCount = guestCount, eventNotes = eventNotes,
    services = services,
    discountType = discountType, discountValue = discountValue,
    taxEnabled = taxEnabled, taxRate = taxRate,
    advancePayments = advancePayments,
    termsConditions = termsConditions, notes = notes, attachments = attachments,
    status = status, templateId = templateId, createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)
