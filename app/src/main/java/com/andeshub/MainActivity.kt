package com.andeshub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.andeshub.data.ConnectivityObserver
import com.andeshub.data.NetworkConnectivityObserver
import com.andeshub.data.local.SessionManager
import com.andeshub.data.local.ThemePreferences
import com.andeshub.routes.AppNavigation
import com.andeshub.ui.components.ConnectivityStatusView
import com.andeshub.ui.theme.AndesHubTheme
import com.andeshub.worker.ProductActivityWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {

    private val _nfcCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val nfcCredentials: StateFlow<Pair<String, String>?> = _nfcCredentials

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val sessionManager = SessionManager(applicationContext)
        if (sessionManager.isLoggedIn()) {
            val workRequest = PeriodicWorkRequestBuilder<ProductActivityWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                ProductActivityWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        setContent {
            val connectivityObserver = remember { NetworkConnectivityObserver(applicationContext) }
            val status by connectivityObserver.observe().collectAsState(initial = ConnectivityObserver.Status.Available)

            val themePreferences = remember { ThemePreferences(applicationContext) }
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemePreferences.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemePreferences.DARK  -> true
                ThemePreferences.LIGHT -> false
                else                   -> isSystemInDarkTheme()
            }

            AndesHubTheme(darkTheme = darkTheme) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ConnectivityStatusView(status = status)
                    AppNavigation(nfcCredentials = nfcCredentials)
                }
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


        val rawMessages = intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (!rawMessages.isNullOrEmpty()) {
            try {
                val message = rawMessages[0] as NdefMessage
                val payload = message.records.firstOrNull()?.payload ?: return

                val content = String(payload, Charsets.UTF_8)
                Log.d("NFC", "payload raw: $content")
                parseCredentials(content)
                return
            } catch (e: Exception) {
                Log.e("NFC", "Error leyendo NDEF messages: ${e.message}")
            }
        }


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
            val password = parts.drop(1).joinToString(":").trim()
            Log.d("NFC", "email: $email, password: $password")
            _nfcCredentials.value = Pair(email, password)
        } else {
            Log.e("NFC", "Formato inválido: $content")
        }
    }
    fun clearNfcCredentials() {
        _nfcCredentials.value = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ProductActivityWorker.CHANNEL_ID,
            "Product Activity",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for views and favorites on your listings"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
