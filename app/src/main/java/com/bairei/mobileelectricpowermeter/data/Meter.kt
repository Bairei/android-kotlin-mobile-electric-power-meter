package com.bairei.mobileelectricpowermeter.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable {

    fun asPrettyString(): String {
        return "${dateTimeFormatter.format(readingDate)} - ${meterReadingAsDecimalString()} kWh"
    }

    fun asRawString(): String {
        return "${rawDateTimeFormatter.format(readingDate)} ${
        meterReadingAsDecimalString().replace(
            ".",
            ","
        )
        }"
    }

    private fun meterReadingAsDecimalString() = String.format(
        "%.1f",
        meterReading.toBigDecimal(MathContext.DECIMAL64).divide(
            BigDecimal.TEN
        )
    )

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        private val rawDateTimeFormatter = DateTimeFormatter.ofPattern("HHmm ddMM")
    }
}
