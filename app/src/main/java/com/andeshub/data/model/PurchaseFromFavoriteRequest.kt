package com.andeshub.data.model

data class PurchaseFromFavoriteRequest(
    val product_id: String,
    val was_favorited: Boolean
)