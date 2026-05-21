package com.andeshub.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDraftDao {
    @Query("SELECT * FROM product_drafts ORDER BY timestamp DESC")
    fun getAllDrafts(): Flow<List<ProductDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: ProductDraftEntity)

    @Delete
    suspend fun deleteDraft(draft: ProductDraftEntity)

    @Query("DELETE FROM product_drafts WHERE id = :draftId")
    suspend fun deleteDraftById(draftId: Long)

    @Query("SELECT * FROM product_drafts WHERE id = :draftId")
    suspend fun getDraftById(draftId: Long): ProductDraftEntity?
}
