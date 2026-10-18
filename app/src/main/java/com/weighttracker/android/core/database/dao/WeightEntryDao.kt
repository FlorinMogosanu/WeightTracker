package com.weighttracker.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.weighttracker.android.core.database.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Upsert
    suspend fun upsert(entry: WeightEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: WeightEntryEntity): Long

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getEntriesForUser(userId: String): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesForUserBetween(userId: String, startDate: Long, endDate: Long): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getEntryForUserAndDate(userId: String, date: Long): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC LIMIT 1")
    fun getLatestEntry(userId: String): Flow<WeightEntryEntity?>

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM weight_entries WHERE userId = :userId AND date < :cutoffDate ORDER BY date ASC")
    suspend fun getEntriesOlderThan(userId: String, cutoffDate: Long): List<WeightEntryEntity>

    @Query("DELETE FROM weight_entries WHERE userId = :userId AND date < :cutoffDate")
    suspend fun deleteEntriesOlderThan(userId: String, cutoffDate: Long)
}
