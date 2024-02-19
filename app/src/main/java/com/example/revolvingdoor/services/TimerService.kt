package com.example.revolvingdoor.services

import android.app.ActivityManager
import android.app.IntentService
import android.app.Notification
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.example.revolvingdoor.R


class TimerService : IntentService("TimerService") { // SERVICE IS A CONTEXT!
    private var listUri: MutableList<Uri> = mutableListOf<Uri>()
    private lateinit var wallpaperManager: WallpaperManager
    private var screenHeight = 0
    private var screenWidth = 0
    private var randoNumber = 0

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        wallpaperManager = WallpaperManager.getInstance(this)
        val extras = intent?.extras
        Log.d("rd", "Service onStartCommand called!")
        if (extras == null) {
            Log.d("rd", "Service extras are null")
        } else {
            intent.extras!!.getStringArray("uriList")?.forEach {
                listUri.add(Uri.parse(it))
            }
            screenHeight = intent.extras!!.getInt("screenHeight")
            screenWidth = intent.extras!!.getInt("screenWidth")
        }

        runCatching {
            var index = 0
            while(true) {
                try {
                    val inputStream = this.contentResolver.openInputStream(listUri[index]) ?: this.resources.openRawResource(+ R.drawable.missing_image) // interesting method to change my drawable res to raw res
                    // val missingBitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.missing_image)
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    val bitmap = Bitmap.createScaledBitmap(imageBitmap, screenWidth, screenHeight, true)
                    wallpaperManager.setBitmap(bitmap)
                    if (index == listUri.size - 1) {
                        index = 0
                    } else {
                        index++
                    }
                    Thread.sleep(10000)
                } catch (e: InterruptedException) {
                    // TODO
                    e.printStackTrace()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        wallpaperManager.clear()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}