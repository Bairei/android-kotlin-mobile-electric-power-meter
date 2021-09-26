package com.bairei.mobileelectricpowermeter.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(entities = [Meter::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class)
abstract class MeterRoomDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao

    private class MeterDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch { populateDatabase(database.meterDao()) }
            }
        }

        suspend fun populateDatabase(meterDao: MeterDao) {
            val daos = meterDao.findAllSuspend()
            if (daos.isEmpty()) {
                Log.i(
                    "MeterRoomDatabase",
                    "populateDatabase: no data found, populating it with mock data"
                )
                var meter =
                    Meter(readingDate = LocalDateTime.of(2021, 9, 24, 19, 9), meterReading = 5454)
                meterDao.insert(meter)
                meter =
                    Meter(readingDate = LocalDateTime.of(2021, 9, 25, 19, 4), meterReading = 5425)
                meterDao.insert(meter)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MeterRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MeterRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeterRoomDatabase::class.java,
                    "meter_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}