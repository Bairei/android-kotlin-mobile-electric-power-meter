package com.bairei.mobileelectricpowermeter

import android.app.Application
import com.bairei.mobileelectricpowermeter.data.MeterRepository
import com.bairei.mobileelectricpowermeter.data.MeterRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ElectricPowerMeterApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { MeterRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MeterRepository(database.meterDao()) }
}