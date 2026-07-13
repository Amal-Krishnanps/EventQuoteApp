package com.eventquote.app

import android.app.Application
import com.eventquote.app.data.db.AppDatabase
import com.eventquote.app.data.repository.CompanyRepository
import com.eventquote.app.data.repository.EstimateRepository
import com.eventquote.app.data.repository.ServiceMasterRepository
import com.eventquote.app.viewmodel.ViewModelFactory

/**
 * Application class — initializes the database and repositories.
 * Provides the ViewModelFactory as a singleton.
 */
class EventQuoteApplication : Application() {

    // ---- Lazy-initialized dependencies ----

    val database by lazy { AppDatabase.getInstance(this) }

    val companyRepository by lazy {
        CompanyRepository(database.companySettingsDao())
    }

    val estimateRepository by lazy {
        EstimateRepository(database.estimateDao(), this)
    }

    val serviceMasterRepository by lazy {
        ServiceMasterRepository(database.serviceMasterDao())
    }

    val viewModelFactory by lazy {
        ViewModelFactory(
            estimateRepository = estimateRepository,
            companyRepository = companyRepository,
            serviceMasterRepository = serviceMasterRepository
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Touch database to trigger creation and seeding
        database.openHelper.writableDatabase
    }
}
