package com.mockify.app

import android.app.Application
import com.mockify.app.service.VibratorService
import com.mockify.app.storage.StorageManager

class MockGpsApp : Application() {
    companion object {
        lateinit var shared: MockGpsApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        shared = this
        StorageManager.initialise(this)
        VibratorService.initialise(this)
    }

}