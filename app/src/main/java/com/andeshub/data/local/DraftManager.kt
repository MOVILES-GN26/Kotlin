package com.andeshub.data.local

import android.content.Context
import java.io.File

/**
 * ESTRATEGIA: ARCHIVOS LOCALES (5 pts)
 * Esta clase gestiona el almacenamiento físico de borradores de productos
 * en la carpeta privada de la aplicación, utilizando lectura y escritura directa de archivos.
 */
class DraftManager(context: Context) {
    
    // Definimos el archivo físico product_draft.txt en el almacenamiento interno privado
    private val draftFile = File(context.filesDir, "product_draft.txt")

    /**
     * ESTRATEGIA: ARCHIVOS LOCALES
     * Escribe el contenido del borrador directamente en el archivo físico.
     */
    fun saveDraft(content: String) {
        try {
            // java.io.File escritura directa
            draftFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ESTRATEGIA: ARCHIVOS LOCALES
     * Lee el contenido almacenado físicamente en el archivo.
     */
    fun getDraft(): String? {
        return try {
            if (draftFile.exists()) {
                // java.io.File lectura directa
                draftFile.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Elimina el archivo físico del borrador.
     */
    fun clearDraft() {
        if (draftFile.exists()) {
            draftFile.delete()
        }
    }
}
