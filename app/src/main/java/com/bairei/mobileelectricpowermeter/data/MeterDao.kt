package com.bairei.mobileelectricpowermeter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    @Query("SELECT * FROM meter ORDER BY reading_date ASC")
    fun findAll(): Flow<List<Meter>>

    @Query("SELECT * FROM meter ORDER BY reading_date ASC")
    suspend fun findAllSuspend(): List<Meter>

    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg meter: Meter)

    @Delete
    suspend fun deleteByMeterEntry(meter: Meter)

    @Query("SELECT * FROM meter WHERE reading_date BETWEEN :dateFrom AND :dateTo ORDER BY reading_date ASC")
    fun findByReadingDateBetween(dateFrom: LocalDateTime, dateTo: LocalDateTime): Flow<List<Meter>>
}
