package com.eventquote.app.data.repository

import com.eventquote.app.data.dao.CompanySettingsDao
import com.eventquote.app.data.entity.CompanySettingsEntity
import com.eventquote.app.model.CompanySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for company settings.
 * Single source of truth mapping entity ↔ domain model.
 */
class CompanyRepository(private val dao: CompanySettingsDao) {

    val companySettings: Flow<CompanySettings?> = dao.getCompanySettings().map { entity ->
        entity?.toDomain()
    }

    suspend fun getCompanySettingsOnce(): CompanySettings? =
        dao.getCompanySettingsOnce()?.toDomain()

    suspend fun saveCompanySettings(settings: CompanySettings) {
        dao.saveCompanySettings(settings.toEntity())
    }

    // ---- Mappers ----

    private fun CompanySettingsEntity.toDomain(): CompanySettings = CompanySettings(
        id = id, name = name, logoPath = logoPath, address = address,
        phone = phone, whatsAppNumber = whatsAppNumber, email = email,
        gstNumber = gstNumber, website = website, bankName = bankName,
        accountNumber = accountNumber, ifscCode = ifscCode,
        accountHolderName = accountHolderName, qrCodePath = qrCodePath,
        signaturePath = signaturePath, defaultTerms = defaultTerms
    )

    private fun CompanySettings.toEntity(): CompanySettingsEntity = CompanySettingsEntity(
        id = id, name = name, logoPath = logoPath, address = address,
        phone = phone, whatsAppNumber = whatsAppNumber, email = email,
        gstNumber = gstNumber, website = website, bankName = bankName,
        accountNumber = accountNumber, ifscCode = ifscCode,
        accountHolderName = accountHolderName, qrCodePath = qrCodePath,
        signaturePath = signaturePath, defaultTerms = defaultTerms
    )
}
