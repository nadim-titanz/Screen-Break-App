package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepScheduleDao {
    @Query("SELECT * FROM sleep_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<SleepScheduleEntity>>

    @Query("SELECT * FROM sleep_schedules ORDER BY id ASC")
    suspend fun getAllSchedulesList(): List<SleepScheduleEntity>

    @Query("SELECT * FROM sleep_schedules WHERE isEnabled = 1")
    fun getEnabledSchedules(): Flow<List<SleepScheduleEntity>>

    @Query("SELECT * FROM sleep_schedules WHERE isEnabled = 1")
    suspend fun getEnabledSchedulesList(): List<SleepScheduleEntity>

    @Query("SELECT * FROM sleep_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): SleepScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: SleepScheduleEntity): Long

    @Update
    suspend fun update(schedule: SleepScheduleEntity)

    @Delete
    suspend fun delete(schedule: SleepScheduleEntity)

    @Query("DELETE FROM sleep_schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sleep_schedules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)
}
