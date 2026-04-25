package com.andeshub.data.model

import com.google.gson.annotations.SerializedName

enum class ProductVisitSource(val value: String) {
    @SerializedName("home") HOME("home"),
    @SerializedName("catalog") CATALOG("catalog"),
    @SerializedName("favorites") FAVORITES("favorites")
}

data class ProductVisitRequest(
    @SerializedName("product_id") val productId: String,
    val source: String // Using String to match backend enum string values
)

data class VisitPercentages(
    val home: Double,
    val catalog: Double,
    val favorites: Double
)

data class VisitCounts(
    val home: Int,
    val catalog: Int,
    val favorites: Int
)

data class ProductVisitStats(
    val total: Int,
    val percentages: VisitPercentages,
    val counts: VisitCounts
)
