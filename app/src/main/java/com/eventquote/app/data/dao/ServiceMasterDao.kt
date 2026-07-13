package com.eventquote.app.data.dao

import androidx.room.*
import com.eventquote.app.data.entity.ServiceMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceMasterDao {

    @Query("SELECT * FROM service_masters ORDER BY sortOrder ASC, name ASC")
    fun getAllServices(): Flow<List<ServiceMasterEntity>>

    @Query("SELECT * FROM service_masters ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllServicesOnce(): List<ServiceMasterEntity>

    @Query("SELECT * FROM service_masters WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteServices(): Flow<List<ServiceMasterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertService(service: ServiceMasterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServices(services: List<ServiceMasterEntity>)

    @Delete
    suspend fun deleteService(service: ServiceMasterEntity)

    @Query("DELETE FROM service_masters WHERE id = :id")
    suspend fun deleteServiceById(id: String)

    @Query("SELECT COUNT(*) FROM service_masters")
    suspend fun getServicesCount(): Int

    @Query("SELECT * FROM service_masters WHERE name LIKE '%' || :query || '%' ORDER BY sortOrder ASC")
    fun searchServices(query: String): Flow<List<ServiceMasterEntity>>

    @Query("UPDATE service_masters SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE service_masters SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
}
