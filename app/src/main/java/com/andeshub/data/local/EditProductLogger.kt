package com.andeshub.data.local

import android.content.Context
import org.json.JSONObject
import java.io.File

object EditProductLogger {

    private const val DRAFT_FILE_PREFIX = "edit_product_draft_"

    fun saveDraft(
        context: Context,
        productId: String,
        title: String,
        description: String,
        category: String,
        location: String,
        price: String,
        condition: String
    ) {
        val file = File(context.filesDir, "$DRAFT_FILE_PREFIX$productId.json")
        val draft = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("category", category)
            put("location", location)
            put("price", price)
            put("condition", condition)
        }
        file.writeText(draft.toString())
        android.util.Log.d("EditProductLogger", "Draft saved for product $productId")
    }

    fun loadDraft(context: Context, productId: String): EditProductDraft? {
        val file = File(context.filesDir, "$DRAFT_FILE_PREFIX$productId.json")
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            EditProductDraft(
                title = json.optString("title", ""),
                description = json.optString("description", ""),
                category = json.optString("category", ""),
                location = json.optString("location", ""),
                price = json.optString("price", ""),
                condition = json.optString("condition", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraft(context: Context, productId: String) {
        val file = File(context.filesDir, "$DRAFT_FILE_PREFIX$productId.json")
        if (file.exists()) file.delete()
        android.util.Log.d("EditProductLogger", "Draft cleared for product $productId")
    }
}

data class EditProductDraft(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val price: String,
    val condition: String
)