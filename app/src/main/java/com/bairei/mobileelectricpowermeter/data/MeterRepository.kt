package com.bairei.mobileelectricpowermeter.data

import androidx.annotation.WorkerThread

class MeterRepository(private val meterDao: MeterDao) {
    val allMeterReadings = meterDao.findAll()
    val latest30MeterReadings = meterDao.findLatest30Readings()

    @WorkerThread
    suspend fun insert(vararg meter: Meter) {
        meterDao.insert(*meter)
    }

    @WorkerThread
    suspend fun delete(meter: Meter) {
        meterDao.deleteByMeterEntry(meter)
    }
}
