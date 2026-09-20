package com.example

import android.app.Application
import android.util.Log
import com.example.data.repository.BhashaSetuRepository
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BhashaSetuApplication : Application() {
    lateinit var repository: BhashaSetuRepository
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.w("BhashaSetuApp", "Firebase initialization skipped/fallback: ${e.message}")
        }

        repository = BhashaSetuRepository(this)
        
        // Seed initial offline dictionaries and sample curriculum data in background
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializePreloadedDataIfNeeded()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_COMPLETE -> {
                Log.w("BhashaSetuApp", "System memory pressure event ($level) on device. Triggering memory cache purge.")
                System.gc()
            }
            else -> {
                Log.d("BhashaSetuApp", "Trim memory level: $level")
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("BhashaSetuApp", "System critical onLowMemory triggered! Purging temporary caches.")
        System.gc()
    }
}

