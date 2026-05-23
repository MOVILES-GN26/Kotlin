package com.andeshub.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDraftDao {
    @Query("SELECT * FROM product_drafts ORDER BY timestamp DESC")
    fun getAllDrafts(): Flow<List<ProductDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: ProductDraftEntity)

    @Update
    suspend fun updateDraft(draft: ProductDraftEntity)

    @Delete
    suspend fun deleteDraft(draft: ProductDraftEntity)

    @Query("DELETE FROM product_drafts WHERE id = :draftId")
    suspend fun deleteDraftById(draftId: Long)

    @Query("SELECT * FROM product_drafts WHERE id = :draftId")
    suspend fun getDraftById(draftId: Long): ProductDraftEntity?

    @Query("SELECT COUNT(*) FROM product_drafts")
    suspend fun getDraftsCount(): Int

    @Query("DELETE FROM product_drafts WHERE id IN (SELECT id FROM product_drafts ORDER BY timestamp ASC LIMIT 1)")
    suspend fun deleteOldestDraft()
    
    @Query("SELECT * FROM product_drafts WHERE isReadyToSync = 1")
    suspend fun getDraftsReadyToSync(): List<ProductDraftEntity>

    @Query("UPDATE product_drafts SET isReadyToSync = :ready WHERE id = :draftId")
    suspend fun updateSyncStatus(draftId: Long, ready: Boolean)

    // ESTRATEGIA [B]: Gestión de almacenamiento local
    @Transaction
    suspend fun insertWithLimit(draft: ProductDraftEntity, limit: Int = 10) {
        val currentCount = getDraftsCount()
        if (currentCount >= limit) {
            deleteOldestDraft()
        }
        insertDraft(draft)
    }
}
