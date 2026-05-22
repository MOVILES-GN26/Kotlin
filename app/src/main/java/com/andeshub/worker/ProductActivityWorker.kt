package com.andeshub.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andeshub.MainActivity
import com.andeshub.R
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.NotificationPreferences
import com.andeshub.data.local.SessionManager
import com.andeshub.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ProductActivityWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME       = "product_activity_worker"
        const val CHANNEL_ID      = "product_activity"
        private var notificationId = 1000
    }

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(appContext)

        val sellerId     = sessionManager.getUserId()     ?: return Result.success()
        val accessToken  = sessionManager.getAccessToken() ?: return Result.success()

        RetrofitClient.setToken(accessToken)

        val api   = RetrofitClient.apiService
        val dao   = AppDatabase.getInstance(appContext).productDao()
        val prefs = NotificationPreferences(appContext)

        val products = dao.getProductsBySeller(sellerId)
        if (products.isEmpty()) return Result.success()

        val notifyViews     = prefs.notifyViews.first()
        val notifyFavorites = prefs.notifyFavorites.first()

        val prevViewCounts = prefs.getPreviousViewCounts().toMutableMap()
        val prevFavCounts  = prefs.getPreviousFavCounts().toMutableMap()

        val newViewCounts = mutableMapOf<String, Int>()
        val newFavCounts  = mutableMapOf<String, Int>()

        withContext(Dispatchers.IO) {
            coroutineScope {
                products.forEach { product ->
                    val statsDeferred = async {
                        try { api.getProductStats(product.id) } catch (e: Exception) { null }
                    }
                    val favDeferred = async {
                        try { api.getFavoritesCount(product.id) } catch (e: Exception) { null }
                    }

                    val stats    = statsDeferred.await()
                    val favCount = favDeferred.await()

                    val currentViews = stats?.views ?: 0
                    val currentFavs  = favCount?.count ?: 0

                    newViewCounts[product.id] = currentViews
                    newFavCounts[product.id]  = currentFavs

                    val prevViews = prevViewCounts[product.id] ?: 0
                    val prevFavs  = prevFavCounts[product.id]  ?: 0

                    if (notifyViews && currentViews > prevViews) {
                        val diff = currentViews - prevViews
                        sendNotification(
                            title = "New views on \"${product.title}\"",
                            message = "$diff new ${if (diff == 1) "person" else "people"} viewed your listing"
                        )
                    }

                    if (notifyFavorites && currentFavs > prevFavs) {
                        val diff = currentFavs - prevFavs
                        sendNotification(
                            title = "New favorites on \"${product.title}\"",
                            message = "$diff ${if (diff == 1) "person" else "people"} added your listing to favorites"
                        )
                    }
                }
            }
        }

        prefs.savePreviousViewCounts(newViewCounts)
        prefs.savePreviousFavCounts(newFavCounts)

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId++, notification)
    }
}
