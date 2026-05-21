package com.andeshub.data.local

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "product_drafts")
data class ProductDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val price: String,
    val category: String,
    val condition: String,
    val location: String,
    val imageUri: String?, // Guardamos la URI como String
    val storeId: String?,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
