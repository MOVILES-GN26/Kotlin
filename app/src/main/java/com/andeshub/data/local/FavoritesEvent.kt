package com.andeshub.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object FavoritesEvent {
    private val _favoriteChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val favoriteChanged: SharedFlow<Unit> = _favoriteChanged

    fun notifyChanged() {
        _favoriteChanged.tryEmit(Unit)
    }
}