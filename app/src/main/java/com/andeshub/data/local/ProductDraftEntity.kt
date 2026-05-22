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
    val imageUri: String?, // URI de la imagen original
    val localImagePath: String? = null, // Ruta de la imagen procesada y guardada localmente
    val storeId: String?,
    val isReadyToSync: Boolean = false, // Para el requerimiento (d)
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
