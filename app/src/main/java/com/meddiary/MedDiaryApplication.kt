package com.meddiary

import android.app.Application
import com.meddiary.data.MedicalDatabase

class MedDiaryApplication : Application() {
    val database: MedicalDatabase by lazy { MedicalDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MedDiaryApplication
            private set
    }
}
