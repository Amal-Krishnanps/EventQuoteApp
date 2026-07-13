package com.eventquote.app.data.dao

import androidx.room.*
import com.eventquote.app.data.entity.CompanySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanySettingsDao {

    @Query("SELECT * FROM company_settings WHERE id = 1 LIMIT 1")
    fun getCompanySettings(): Flow<CompanySettingsEntity?>

    @Query("SELECT * FROM company_settings WHERE id = 1 LIMIT 1")
    suspend fun getCompanySettingsOnce(): CompanySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompanySettings(settings: CompanySettingsEntity)

    @Update
    suspend fun updateCompanySettings(settings: CompanySettingsEntity)
}
