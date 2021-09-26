package com.bairei.mobileelectricpowermeter.data

import androidx.annotation.WorkerThread

class MeterRepository(private val meterDao: MeterDao) {
    val allMeterReadings = meterDao.findAll()

    @WorkerThread
    suspend fun insert(meter: Meter) {
        meterDao.insert(meter)
    }
}