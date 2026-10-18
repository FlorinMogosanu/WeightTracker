package com.weighttracker.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.weighttracker.android.core.database.dao.WeightEntryDao
import com.weighttracker.android.core.database.dao.WeeklySummaryDao
import com.weighttracker.android.core.database.entity.WeightEntryEntity
import com.weighttracker.android.core.database.entity.WeeklySummaryEntity

@Database(
    entities = [
        WeightEntryEntity::class,
        WeeklySummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun weeklySummaryDao(): WeeklySummaryDao

    @Transaction
    open suspend fun executePruneAndArchive(
        userId: String,
        cutoffEpochDay: Long,
        summaries: List<WeeklySummaryEntity>
    ) {
        if (summaries.isNotEmpty()) {
            weeklySummaryDao().upsertAll(summaries)
        }
        weightEntryDao().deleteEntriesOlderThan(userId, cutoffEpochDay)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weight_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
