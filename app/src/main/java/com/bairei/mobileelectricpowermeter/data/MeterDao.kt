package com.bairei.mobileelectricpowermeter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MeterDao {
    @Query("SELECT * FROM meter")
    fun findAll(): Flow<List<Meter>>

    @Query("SELECT * FROM meter")
    suspend fun findAllSuspend(): List<Meter>

    @Insert(onConflict = REPLACE)
    suspend fun insert(meter: Meter)

    @Delete
    suspend fun deleteByMeterEntry(meter: Meter)

    @Query("SELECT * FROM meter WHERE reading_date BETWEEN :dateFrom AND :dateTo")
    fun findByReadingDateBetween(dateFrom: LocalDateTime, dateTo: LocalDateTime): Flow<List<Meter>>
}