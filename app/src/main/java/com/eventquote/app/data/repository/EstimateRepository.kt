package com.eventquote.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eventquote.app.data.dao.EstimateDao
import com.eventquote.app.data.entity.EstimateEntity
import com.eventquote.app.data.entity.toDomain
import com.eventquote.app.data.entity.toEntity
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.EstimateStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for all estimate CRUD, search, and sequence number operations.
 */
class EstimateRepository(
    private val dao: EstimateDao,
    private val context: Context
) {
    private val sequenceKey = intPreferencesKey("estimate_sequence")

    // ---- Observe all estimates ----

    val allEstimates: Flow<List<Estimate>> = dao.getAllEstimates().map { list ->
        list.map { it.toDomain() }
    }

    // ---- Get by ID ----

    suspend fun getEstimateById(id: String): Estimate? =
        dao.getEstimateById(id)?.toDomain()

    fun observeEstimateById(id: String): Flow<Estimate?> =
        dao.observeEstimateById(id).map { it?.toDomain() }

    // ---- Save (insert or update) ----

    suspend fun saveEstimate(estimate: Estimate): Estimate {
        val toSave = if (estimate.id.isEmpty()) {
            // New estimate — generate ID and number
            val newId = UUID.randomUUID().toString()
            val estNumber = generateEstimateNumber()
            estimate.copy(id = newId, estimateNumber = estNumber)
        } else {
            estimate
        }
        dao.upsertEstimate(toSave.toEntity())
        return toSave
    }

    // ---- Delete ----

    suspend fun deleteEstimate(id: String) = dao.deleteEstimateById(id)

    // ---- Duplicate ----

    suspend fun duplicateEstimate(id: String): Estimate? {
        val original = dao.getEstimateById(id) ?: return null
        val newId = UUID.randomUUID().toString()
        val newNumber = generateEstimateNumber()
        val duplicate = original.copy(
            id = newId,
            estimateNumber = newNumber,
            status = EstimateStatus.DRAFT,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.upsertEstimate(duplicate)
        return duplicate.toDomain()
    }

    // ---- Status update ----

    suspend fun updateStatus(id: String, status: EstimateStatus) {
        dao.updateStatus(id, status)
    }

    // ---- Search ----

    fun searchEstimates(query: String): Flow<List<Estimate>> =
        if (query.isBlank()) allEstimates
        else dao.searchEstimates(query).map { list -> list.map { it.toDomain() } }

    // ---- Filter by status ----

    fun getEstimatesByStatus(status: EstimateStatus): Flow<List<Estimate>> =
        dao.getEstimatesByStatus(status).map { list -> list.map { it.toDomain() } }

    // ---- Customer history ----

    fun getEstimatesForCustomer(mobile: String, name: String): Flow<List<Estimate>> =
        dao.getEstimatesForCustomer(mobile, name).map { list -> list.map { it.toDomain() } }

    // ---- Statistics ----

    suspend fun getTotalCount(): Int = dao.getTotalEstimatesCount()

    // ---- Estimate number generation ----

    /**
     * Generates the next estimate number: EST-YYYY-NNNN
     * Sequence resets each year.
     */
    private suspend fun generateEstimateNumber(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val current = context.dataStore.data.first()[sequenceKey] ?: 0
        val next = current + 1
        context.dataStore.edit { prefs ->
            prefs[sequenceKey] = next
        }
        return "EST-$year-${next.toString().padStart(4, '0')}"
    }
}
