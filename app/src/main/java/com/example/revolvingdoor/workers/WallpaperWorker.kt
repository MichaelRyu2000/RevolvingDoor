package com.example.revolvingdoor.workers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperWorker(
    private val appContext: Context,
    private val params: WorkerParameters
): CoroutineWorker(appContext, params) {

    private lateinit var wallpaperManager: WallpaperManager

    override suspend fun doWork(): Result {
        try {
            Thread.sleep(60000)
            return withContext(Dispatchers.IO) {
                wallpaperManager = WallpaperManager.getInstance(appContext)
                val uriList = mutableListOf<Uri>()
                val screenHeight = params.inputData.getInt(SCREEN_HEIGHT, 0)
                val screenWidth = params.inputData.getInt(SCREEN_WIDTH,0)
                params.inputData.getStringArray(KEY_CONTENT_URIS)?.forEach {
                    uriList.add(Uri.parse(it))
                }
                // consider compression operations here
                val bytes = appContext.contentResolver.openInputStream(uri)?.use {
                    it.readBytes()
                } ?: return@withContext Result.failure()

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true)
                wallpaperManager.setBitmap(scaledBitmap)
                return@withContext Result.success()
            }
        } catch(e: InterruptedException) {
            Log.d("rd", "Interrupted exception in wallpaperworker caught!")
        }
        return Result.success()
    }

    companion object {
        const val KEY_CONTENT_URIS = "KEY_CONTENT_URIS"
        const val SCREEN_HEIGHT = "SCREEN_HEIGHT"
        const val SCREEN_WIDTH = "SCREEN_WIDTH"
    }
}