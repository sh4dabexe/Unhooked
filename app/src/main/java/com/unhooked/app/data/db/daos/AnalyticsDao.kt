package com.unhooked.app.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unhooked.app.data.db.entities.BlockedAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedAttempt(attempt: BlockedAttemptEntity): Long

    @Query("SELECT * FROM blocked_attempts ORDER BY timestamp DESC LIMIT 100")
    fun getRecentBlockedAttemptsFlow(): Flow<List<BlockedAttemptEntity>>

    @Query("SELECT COUNT(*) FROM blocked_attempts WHERE timestamp >= :sinceMs")
    suspend fun getBlockedCountSince(sinceMs: Long): Int

    @Query("SELECT COUNT(*) FROM blocked_attempts WHERE timestamp >= :sinceMs")
    fun getBlockedCountSinceFlow(sinceMs: Long): Flow<Int>

    @Query("DELETE FROM blocked_attempts WHERE timestamp < :olderThanMs")
    suspend fun purgeOldAttempts(olderThanMs: Long)
}
