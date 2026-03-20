package com.andeshub.data.model

data class RecordInteractionRequest(
    val product_id: String,
    val seller_id: String?
)

data class ProductStats(
    val views: Int,
    val last_viewed: String?,
    val last_user_id: String?
)
