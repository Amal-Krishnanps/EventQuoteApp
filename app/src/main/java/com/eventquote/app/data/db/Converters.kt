package com.eventquote.app.data.db

import androidx.room.TypeConverter
import com.eventquote.app.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for complex types stored as JSON.
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ---- EstimateService list ----

    @TypeConverter
    fun fromServicesList(services: List<EstimateService>): String =
        json.encodeToString(services)

    @TypeConverter
    fun toServicesList(data: String): List<EstimateService> =
        runCatching { json.decodeFromString<List<EstimateService>>(data) }.getOrDefault(emptyList())

    // ---- AdvancePayment list ----

    @TypeConverter
    fun fromAdvancePaymentList(payments: List<AdvancePayment>): String =
        json.encodeToString(payments)

    @TypeConverter
    fun toAdvancePaymentList(data: String): List<AdvancePayment> =
        runCatching { json.decodeFromString<List<AdvancePayment>>(data) }.getOrDefault(emptyList())

    // ---- String list (for attachments) ----

    @TypeConverter
    fun fromStringList(list: List<String>): String =
        json.encodeToString(list)

    @TypeConverter
    fun toStringList(data: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(data) }.getOrDefault(emptyList())

    // ---- Enums ----

    @TypeConverter
    fun fromEstimateStatus(status: EstimateStatus): String = status.name

    @TypeConverter
    fun toEstimateStatus(name: String): EstimateStatus =
        runCatching { EstimateStatus.valueOf(name) }.getOrDefault(EstimateStatus.DRAFT)

    @TypeConverter
    fun fromEventType(type: EventType): String = type.name

    @TypeConverter
    fun toEventType(name: String): EventType =
        runCatching { EventType.valueOf(name) }.getOrDefault(EventType.WEDDING)

    @TypeConverter
    fun fromDiscountType(type: DiscountType): String = type.name

    @TypeConverter
    fun toDiscountType(name: String): DiscountType =
        runCatching { DiscountType.valueOf(name) }.getOrDefault(DiscountType.FIXED)

    @TypeConverter
    fun fromPdfTemplate(template: PdfTemplate): String = template.name

    @TypeConverter
    fun toPdfTemplate(name: String): PdfTemplate =
        runCatching { PdfTemplate.valueOf(name) }.getOrDefault(PdfTemplate.ELEGANT)

    // ---- SubItemTemplate list ----

    @TypeConverter
    fun fromSubItemTemplateList(items: List<com.eventquote.app.model.SubItemTemplate>): String =
        json.encodeToString(items)

    @TypeConverter
    fun toSubItemTemplateList(data: String): List<com.eventquote.app.model.SubItemTemplate> =
        runCatching {
            json.decodeFromString<List<com.eventquote.app.model.SubItemTemplate>>(data)
        }.getOrDefault(emptyList())
}
