package com.eventquote.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eventquote.app.model.ServiceMaster
import com.eventquote.app.model.SubItem
import com.eventquote.app.model.SubItemTemplate
import java.util.UUID

/**
 * Room entity for a master service template.
 */
@Entity(tableName = "service_masters")
data class ServiceMasterEntity(
    @PrimaryKey val id: String,
    val name: String = "",
    val description: String = "",
    val defaultAmount: Double = 0.0,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    // JSON list of SubItemTemplate via TypeConverter
    val defaultSubItems: List<SubItemTemplate> = emptyList()
)

// ---- Mapper functions ----

fun ServiceMasterEntity.toDomain(): ServiceMaster = ServiceMaster(
    id = id, name = name, description = description,
    defaultAmount = defaultAmount, isFavorite = isFavorite,
    sortOrder = sortOrder, defaultSubItems = defaultSubItems
)

fun ServiceMaster.toEntity(): ServiceMasterEntity = ServiceMasterEntity(
    id = id, name = name, description = description,
    defaultAmount = defaultAmount, isFavorite = isFavorite,
    sortOrder = sortOrder, defaultSubItems = defaultSubItems
)

/**
 * Convert a ServiceMasterEntity to an EstimateService (when user selects it in estimate).
 */
fun ServiceMasterEntity.toEstimateService(): com.eventquote.app.model.EstimateService =
    com.eventquote.app.model.EstimateService(
        id = UUID.randomUUID().toString(),
        name = name,
        description = description,
        amount = defaultAmount,
        subItems = defaultSubItems.map { template ->
            SubItem(
                id = UUID.randomUUID().toString(),
                name = template.name,
                description = template.description,
                cost = template.defaultCost
            )
        }
    )
