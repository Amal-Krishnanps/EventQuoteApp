package com.eventquote.app.data.dao

import androidx.room.*
import com.eventquote.app.data.entity.EstimateEntity
import com.eventquote.app.model.EstimateStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EstimateDao {

    // ---- Insert / Update / Delete ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEstimate(estimate: EstimateEntity)

    @Delete
    suspend fun deleteEstimate(estimate: EstimateEntity)

    @Query("DELETE FROM estimates WHERE id = :id")
    suspend fun deleteEstimateById(id: String)

    // ---- Query All ----

    @Query("SELECT * FROM estimates ORDER BY createdAt DESC")
    fun getAllEstimates(): Flow<List<EstimateEntity>>

    @Query("SELECT * FROM estimates ORDER BY createdAt DESC")
    suspend fun getAllEstimatesOnce(): List<EstimateEntity>

    // ---- Query by ID ----

    @Query("SELECT * FROM estimates WHERE id = :id LIMIT 1")
    suspend fun getEstimateById(id: String): EstimateEntity?

    @Query("SELECT * FROM estimates WHERE id = :id LIMIT 1")
    fun observeEstimateById(id: String): Flow<EstimateEntity?>

    // ---- Search ----

    @Query("""
        SELECT * FROM estimates 
        WHERE customerName LIKE '%' || :query || '%'
           OR estimateNumber LIKE '%' || :query || '%'
           OR customerMobile LIKE '%' || :query || '%'
           OR venueName LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchEstimates(query: String): Flow<List<EstimateEntity>>

    // ---- Filter by status ----

    @Query("SELECT * FROM estimates WHERE status = :status ORDER BY createdAt DESC")
    fun getEstimatesByStatus(status: EstimateStatus): Flow<List<EstimateEntity>>

    // ---- Dashboard statistics ----

    @Query("SELECT COUNT(*) FROM estimates")
    suspend fun getTotalEstimatesCount(): Int

    @Query("SELECT COUNT(*) FROM estimates WHERE status = 'CONFIRMED'")
    suspend fun getConfirmedCount(): Int

    @Query("SELECT COUNT(*) FROM estimates WHERE status = 'COMPLETED'")
    suspend fun getCompletedCount(): Int

    // ---- Estimate number sequence ----

    @Query("SELECT COUNT(*) FROM estimates WHERE estimateNumber LIKE :prefix || '%'")
    suspend fun getEstimatesCountForYear(prefix: String): Int

    // ---- Sort options ----

    @Query("SELECT * FROM estimates ORDER BY createdAt ASC")
    fun getAllEstimatesOldestFirst(): Flow<List<EstimateEntity>>

    @Query("SELECT * FROM estimates ORDER BY functionDate ASC")
    fun getAllEstimatesByFunctionDate(): Flow<List<EstimateEntity>>

    // ---- Update status only ----

    @Query("UPDATE estimates SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: EstimateStatus, updatedAt: Long = System.currentTimeMillis())

    // ---- Customer history ----

    @Query("""
        SELECT * FROM estimates 
        WHERE customerMobile = :mobile OR customerName = :name
        ORDER BY createdAt DESC
    """)
    fun getEstimatesForCustomer(mobile: String, name: String): Flow<List<EstimateEntity>>
}
