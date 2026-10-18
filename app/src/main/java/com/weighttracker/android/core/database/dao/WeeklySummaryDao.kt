package com.weighttracker.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.weighttracker.android.core.database.entity.WeeklySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklySummaryDao {

    @Upsert
    suspend fun upsert(summary: WeeklySummaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(summaries: List<WeeklySummaryEntity>)

    @Query("SELECT * FROM weekly_summaries WHERE userId = :userId ORDER BY year ASC, weekNumber ASC")
    fun getSummariesForUser(userId: String): Flow<List<WeeklySummaryEntity>>

    @Query("SELECT * FROM weekly_summaries WHERE userId = :userId AND year = :year AND weekNumber = :weekNumber LIMIT 1")
    suspend fun getSummary(userId: String, year: Int, weekNumber: Int): WeeklySummaryEntity?

    @Query("DELETE FROM weekly_summaries WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
