package com.unhooked.app.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unhooked.app.data.db.entities.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessionsFlow(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE endAt > :nowMs AND completed = 0 ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveFocusSession(nowMs: Long = System.currentTimeMillis()): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE endAt > :nowMs AND completed = 0 ORDER BY startedAt DESC LIMIT 1")
    fun getActiveFocusSessionFlow(nowMs: Long = System.currentTimeMillis()): Flow<FocusSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("UPDATE focus_sessions SET completed = 1 WHERE id = :sessionId")
    suspend fun markSessionCompleted(sessionId: Long)

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE startedAt >= :sinceMs AND completed = 1")
    suspend fun getTotalFocusMinutesSince(sinceMs: Long): Int?
}
