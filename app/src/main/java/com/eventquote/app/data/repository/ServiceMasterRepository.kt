package com.eventquote.app.data.repository

import com.eventquote.app.data.dao.ServiceMasterDao
import com.eventquote.app.data.entity.ServiceMasterEntity
import com.eventquote.app.data.entity.toDomain
import com.eventquote.app.data.entity.toEntity
import com.eventquote.app.model.ServiceMaster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for master service templates.
 */
class ServiceMasterRepository(private val dao: ServiceMasterDao) {

    val allServices: Flow<List<ServiceMaster>> =
        dao.getAllServices().map { list -> list.map { it.toDomain() } }

    val favoriteServices: Flow<List<ServiceMaster>> =
        dao.getFavoriteServices().map { list -> list.map { it.toDomain() } }

    suspend fun getAllServicesOnce(): List<ServiceMaster> =
        dao.getAllServicesOnce().map { it.toDomain() }

    suspend fun saveService(service: ServiceMaster) {
        dao.upsertService(service.toEntity())
    }

    suspend fun deleteService(id: String) = dao.deleteServiceById(id)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) =
        dao.updateFavorite(id, isFavorite)

    suspend fun updateSortOrder(id: String, sortOrder: Int) =
        dao.updateSortOrder(id, sortOrder)

    suspend fun reorderServices(services: List<ServiceMaster>) {
        services.forEachIndexed { index, service ->
            dao.updateSortOrder(service.id, index)
        }
    }

    fun searchServices(query: String): Flow<List<ServiceMaster>> =
        if (query.isBlank()) allServices
        else dao.searchServices(query).map { list -> list.map { it.toDomain() } }

    suspend fun getServicesCount(): Int = dao.getServicesCount()
}
