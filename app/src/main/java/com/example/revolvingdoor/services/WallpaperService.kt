package com.example.revolvingdoor.services

import android.app.Service
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.revolvingdoor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WallpaperService: Service() {

    private var job: Job? = null
    private lateinit var wallpaperManager: WallpaperManager
    private var screenHeight = 0
    private var screenWidth = 0
    private val bitmapList = mutableListOf<Bitmap>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wallpaperManager = WallpaperManager.getInstance(this)
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
                val serviceContext = this
                val extras = intent.extras
                if (extras == null) {
                    Log.d("rd", "WallpaperService extras are empty")
                } else {
                    job = CoroutineScope(Dispatchers.IO).launch {
                        var index = 0
                        screenHeight = extras.getInt(WallpaperKeys.SCREEN_HEIGHT.toString())
                        screenWidth = extras.getInt(WallpaperKeys.SCREEN_WIDTH.toString())

                        extras.getStringArray(WallpaperKeys.CONTENT_URIS.toString())?.forEach { path ->
                            val inputStream = serviceContext.contentResolver.openInputStream(Uri.parse(path)) ?: serviceContext.resources.openRawResource(+ R.drawable.missing_image) // interesting method to change my drawable res to raw res
                            val imageBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()

                            val bitmap = Bitmap.createScaledBitmap(imageBitmap, screenWidth, screenHeight, true)
                            bitmapList.add(bitmap)
                        }
                        while(true) {
                            wallpaperManager.setBitmap(bitmapList[index])
                            if (index == bitmapList.size - 1) {
                                index = 0
                            } else {
                                index++
                            }
                            delay(10000)
                        }
                    }
                }

            }
            Actions.STOP.toString() -> {
                wallpaperManager.clear()
                job?.cancel()
                job = null
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        super.onDestroy()
    }

    private fun start() {
        val notification = NotificationCompat.Builder(
            this,
            "wallpaper_channel"
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RevolvingDoor is active")
            .setContentText("Displaying wallpapers...")
            .build()
            startForeground(1, notification)
    }

    enum class Actions {
        START, STOP
    }

    enum class WallpaperKeys {
        SCREEN_HEIGHT,
        SCREEN_WIDTH,
        CONTENT_URIS
    }
}

