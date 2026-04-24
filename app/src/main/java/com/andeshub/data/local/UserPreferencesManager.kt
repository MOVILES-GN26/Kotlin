package com.andeshub.data.local

import android.content.Context

/**
 * ESTRATEGIA: PREFERENCES (5 pts)
 * Esta clase se encarga de persistir configuraciones de la interfaz de usuario
 * y preferencias de filtrado.
 */
class UserPreferencesManager(context: Context) {
    
    // Usamos un archivo de preferencias específico para la configuración de UI
    private val prefs = context.getSharedPreferences("ui_settings_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda si el usuario prefiere ver los productos en cuadrícula (true) o lista (false).
     */
    fun setGridViewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_grid_view", enabled).apply()
    }

    /**
     * Recupera la preferencia de visualización. Por defecto es true (cuadrícula).
     */
    fun isGridViewEnabled(): Boolean {
        return prefs.getBoolean("is_grid_view", true)
    }

    /**
     * Guarda la última categoría seleccionada en el catálogo.
     */
    fun saveLastCategory(category: String?) {
        prefs.edit().putString("last_selected_category", category).apply()
    }

    /**
     * Recupera la última categoría seleccionada.
     */
    fun getLastCategory(): String? {
        return prefs.getString("last_selected_category", null)
    }
}
