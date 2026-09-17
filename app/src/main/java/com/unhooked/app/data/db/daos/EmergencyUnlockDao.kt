package com.unhooked.app.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unhooked.app.data.db.entities.EmergencyUnlockEntity

@Dao
interface EmergencyUnlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unlock: EmergencyUnlockEntity): Long

    @Query("SELECT * FROM emergency_unlocks WHERE scheduleId = :scheduleId AND isActive = 1 LIMIT 1")
    suspend fun getActiveByScheduleId(scheduleId: Long): EmergencyUnlockEntity?

    @Query("UPDATE emergency_unlocks SET isActive = 0 WHERE scheduleId = :scheduleId")
    suspend fun deactivate(scheduleId: Long)

    @Query("DELETE FROM emergency_unlocks WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: Long)

    @Query("SELECT COUNT(*) FROM emergency_unlocks WHERE isActive = 1")
    suspend fun getActiveCount(): Int
}
