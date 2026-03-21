package com.andeshub

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.andeshub.routes.AppNavigation
import com.andeshub.ui.theme.AndesHubTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

    private val _nfcCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val nfcCredentials: StateFlow<Pair<String, String>?> = _nfcCredentials

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        setContent {
            AndesHubTheme {
                AppNavigation(nfcCredentials = nfcCredentials)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        Log.d("NFC", "Foreground dispatch activado")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("NFC", "onNewIntent: ${intent.action}")
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        Log.d("NFC", "handleNfcIntent: ${intent?.action}")

        // Leer desde EXTRA_NDEF_MESSAGES (el más confiable con foreground dispatch)
        val rawMessages = intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (!rawMessages.isNullOrEmpty()) {
            try {
                val message = rawMessages[0] as NdefMessage
                val payload = message.records.firstOrNull()?.payload ?: return
                // MIME type records: payload es directamente el contenido sin metadata
                val content = String(payload, Charsets.UTF_8)
                Log.d("NFC", "payload raw: $content")
                parseCredentials(content)
                return
            } catch (e: Exception) {
                Log.e("NFC", "Error leyendo NDEF messages: ${e.message}")
            }
        }

        // Fallback: leer desde el Tag directamente
        val tag = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        val ndef = Ndef.get(tag) ?: return

        try {
            ndef.connect()
            val payload = ndef.ndefMessage?.records?.firstOrNull()?.payload ?: return
            val content = String(payload, Charsets.UTF_8)
            Log.d("NFC", "payload fallback: $content")
            parseCredentials(content)
        } catch (e: Exception) {
            Log.e("NFC", "Error: ${e.message}")
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private fun parseCredentials(content: String) {
        val parts = content.split(":")
        if (parts.size >= 2) {
            val email = parts[0].trim()
            val password = parts.drop(1).joinToString(":").trim() // por si el password tiene ':'
            Log.d("NFC", "email: $email, password: $password")
            _nfcCredentials.value = Pair(email, password)
        } else {
            Log.e("NFC", "Formato inválido: $content")
        }
    }
    fun clearNfcCredentials() {
        _nfcCredentials.value = null
    }
}