package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IgnoredAppDao {
    @Query("SELECT * FROM ignored_apps WHERE isIgnored = 1 ORDER BY appName ASC")
    fun getIgnoredApps(): Flow<List<IgnoredAppEntity>>

    @Query("SELECT * FROM ignored_apps")
    fun getAll(): Flow<List<IgnoredAppEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM ignored_apps WHERE packageName = :packageName AND isIgnored = 1)")
    suspend fun isAppIgnored(packageName: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM ignored_apps WHERE packageName = :packageName AND isIgnored = 1)")
    fun isAppIgnoredFlow(packageName: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setIgnored(entity: IgnoredAppEntity)

    @Query("DELETE FROM ignored_apps WHERE packageName = :packageName")
    suspend fun remove(packageName: String)

    @Query("DELETE FROM ignored_apps")
    suspend fun clearAll()
}
