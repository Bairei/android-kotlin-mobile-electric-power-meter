package com.bairei.mobileelectricpowermeter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "meter")
data class Meter(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    var id: Long? = null,
    @ColumnInfo(name = "business_id")
    val businessId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "reading_date")
    val readingDate: LocalDateTime,
    @ColumnInfo(name = "meter_reading")
    val meterReading: Int
)
