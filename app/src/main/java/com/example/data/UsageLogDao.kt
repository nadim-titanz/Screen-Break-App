package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<UsageLogEntity>>

    @Query("SELECT COUNT(*) FROM usage_logs WHERE actionType = 'REMINDER_SHOWN'")
    fun getReminderCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM usage_logs WHERE actionType = 'REMINDER_SUPPRESSED'")
    fun getSuppressedCountFlow(): Flow<Int>

    @Insert
    suspend fun insert(log: UsageLogEntity)

    @Query("DELETE FROM usage_logs")
    suspend fun clearLogs()
}
