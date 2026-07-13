package com.eventquote.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eventquote.app.data.dao.CompanySettingsDao
import com.eventquote.app.data.dao.EstimateDao
import com.eventquote.app.data.dao.ServiceMasterDao
import com.eventquote.app.data.entity.CompanySettingsEntity
import com.eventquote.app.data.entity.EstimateEntity
import com.eventquote.app.data.entity.ServiceMasterEntity
import com.eventquote.app.model.DefaultServices
import com.eventquote.app.model.SubItemTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Main Room database for EventQuote app.
 * Version 1 — initial schema.
 */
@Database(
    entities = [
        CompanySettingsEntity::class,
        EstimateEntity::class,
        ServiceMasterEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun companySettingsDao(): CompanySettingsDao
    abstract fun estimateDao(): EstimateDao
    abstract fun serviceMasterDao(): ServiceMasterDao

    companion object {
        const val DATABASE_NAME = "event_quote_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Seeds default service master data on first database creation.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed is done asynchronously after DB is created
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDefaultServices(database.serviceMasterDao())
                }
            }
        }

        private suspend fun seedDefaultServices(dao: ServiceMasterDao) {
            val defaults = DefaultServices.getDefaultMasterServices()
            val entities = defaults.mapIndexed { index, (serviceName, subItemNames) ->
                ServiceMasterEntity(
                    id = UUID.randomUUID().toString(),
                    name = serviceName,
                    sortOrder = index,
                    defaultSubItems = subItemNames.mapIndexed { subIdx, subName ->
                        SubItemTemplate(
                            id = UUID.randomUUID().toString(),
                            name = subName
                        )
                    }
                )
            }
            dao.upsertServices(entities)
        }
    }
}
