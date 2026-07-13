package com.eventquote.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for company settings.
 * Single row (id=1 always) — updated, never inserted more than once.
 */
@Entity(tableName = "company_settings")
data class CompanySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val logoPath: String = "",
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
    val qrCodePath: String = "",
    val signaturePath: String = "",
    val defaultTerms: String = ""
)
