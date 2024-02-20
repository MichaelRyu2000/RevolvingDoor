package com.example.revolvingdoor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
// import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.example.revolvingdoor.services.WallpaperService
import com.example.revolvingdoor.ui.theme.RevolvingDoorTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("rd", "onCreate called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        setContent {
            RevolvingDoorTheme {
                val (screenWidth, screenHeight) = getDeviceWidthAndHeight()
                val result = rememberMutableStateListOf<Uri>() // note this for issues regarding imageList
                val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
                    result.addAll(it)
                }
                val resultList = result.toList()
                // WallpaperManager forces activities to be recreated
                // more info here: https://commonsware.com/blog/2021/10/31/android-12-wallpaper-changes-recreate-activities.html
               // val wallpaperManager = WallpaperManager.getInstance(LocalContext.current)
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestScreen(
                        pickImages = pickImages,
                        imageList = resultList,
                        screenHeight = screenHeight,
                        screenWidth = screenWidth,
                        )
                }
            }
        }
    }
}

@Composable
fun TestScreen(
    pickImages: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    imageList: List<Uri>,
    screenHeight: Int,
    screenWidth: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
   Column(
       horizontalAlignment = Alignment.CenterHorizontally,
       modifier = modifier.fillMaxSize()
    ) {
       if (imageList.isEmpty()) {
           Text(
               text = "Please choose some pictures",
               modifier = Modifier.weight(4f)
           )
       } else {
           LazyVerticalGrid(
               columns= GridCells.Fixed(2),
               modifier = Modifier.weight(4f)
           ) {
               items(imageList.size) {photo ->
                   AsyncImage (
                       model = imageList[photo],
                       contentDescription = null,
                       contentScale = ContentScale.FillBounds,
                       placeholder = painterResource(R.drawable.missing_image),
                       modifier = Modifier
                           .size(150.dp)
                           .border(BorderStroke(1.dp, Color.Black))
                   )
               }
           }
       }
       Spacer(modifier = Modifier.size(4.dp))
       Button(
           onClick = {
               Intent(context, WallpaperService::class.java).also {
                   val stringList = mutableListOf<String>()
                   imageList.forEach { uri ->
                       stringList.add(uri.toString())
                   }

                   it.putExtra(
                       WallpaperService.WallpaperKeys.SCREEN_HEIGHT.toString(),
                       screenHeight
                   )
                   it.putExtra(WallpaperService.WallpaperKeys.SCREEN_WIDTH.toString(), screenWidth)
                   it.putExtra(
                       WallpaperService.WallpaperKeys.CONTENT_URIS.toString(),
                       stringList.toTypedArray()
                   )
                   it.action = WallpaperService.Actions.START.toString()
                   context.startService(it)
               }
           },
           modifier = Modifier.weight(0.5f)
       ) {
           Text(text = "Display Wallpaper")
       }
       Spacer(modifier = Modifier.size(4.dp))
       Button(
           onClick = {
               Intent(context, WallpaperService::class.java).also {
                   it.action = WallpaperService.Actions.STOP.toString()
                   context.startService(it)
               }

           },
           modifier = Modifier.weight(0.5f)
       ) {
           Text(
               text = "Reset wallpaper"
           )
       }
       Spacer(modifier = Modifier.size(16.dp))
       Button(
           onClick = {
               pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
           },
           modifier = Modifier.weight(0.5f)
       ) {
           Text(
               text = "Choose picture(s)"
           )
       }
    }
}

// https://stackoverflow.com/questions/68885154/using-remembersaveable-with-mutablestatelistof
@Composable
fun <T: Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(
        saver = listSaver(
            save = { stateList ->
                if (stateList.isNotEmpty()) {
                    val first = stateList.first()
                    if (!canBeSaved(first)) {
                        throw IllegalStateException("${first::class} cannot be saved. By default only types which can be stored in the Bundle class can be saved.")
                    }
                }
                stateList.toList()
            },
            restore = { it.toMutableStateList() }
        )
    ) {
        elements.toList().toMutableStateList()
    }
}

//@Composable
//// https://stackoverflow.com/questions/6410364/how-to-scale-bitmap-to-screen-size
//private fun decodeStream(imageUri: Uri): Bitmap? {
//    var inputStream = LocalContext.current.contentResolver.openInputStream(imageUri) ?: LocalContext.current.resources.openRawResource(+ R.drawable.missing_image)
//    //decode image size
//    val o = BitmapFactory.Options()
//    o.inJustDecodeBounds = true
//    BitmapFactory.decodeStream(inputStream, null, o)
//    //Find the correct scale value. It should be the power of 2.
//    val requiredSize = 70
//    var widthTmp = o.outWidth
//    var heightTmp = o.outHeight
//    var scale = 1
//    while (true) {
//        if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize) break
//        widthTmp /= 2
//        heightTmp /= 2
//        scale*=2
//    }
//    Log.d("rd", "scale: " + scale.toString())
//    inputStream.close()
//    inputStream = LocalContext.current.contentResolver.openInputStream(imageUri) ?: LocalContext.current.resources.openRawResource(+ R.drawable.missing_image)
//    //decode with inSampleSize
//    val o2 = BitmapFactory.Options()
//    o2.inSampleSize = 4
//    val output = BitmapFactory.decodeStream(inputStream, null, o2)
//    inputStream.close()
//    return output
//}

//@Composable
//fun getScreenHeight(): Int {
//    val density = LocalContext.current.resources.displayMetrics.density
//    Log.d("rd", "ScreenHeightDp: " + LocalConfiguration.current.screenHeightDp.toString())
//    return (LocalConfiguration.current.screenHeightDp * density + 0.5f).toInt()
//}
//
//@Composable
//fun getScreenWidth(): Int {
//    val density = LocalContext.current.resources.displayMetrics.density
//    Log.d("rd", "ScreenWidthDp: " + LocalConfiguration.current.screenWidthDp.toString())
//    return (LocalConfiguration.current.screenWidthDp * density + 0.5f).toInt()
//}


//private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
//    var imageScaled = image
//    return if (maxHeight > 0 && maxWidth > 0) {
//        val width = imageScaled.width
//        val height = imageScaled.height
//        val ratioBitmap = width.toFloat() / height.toFloat()
//        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
//        var finalWidth = maxWidth
//        var finalHeight = maxHeight
//        if (ratioMax > ratioBitmap) {
//            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
//        } else {
//            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
//        }
//        imageScaled = Bitmap.createScaledBitmap(imageScaled, finalWidth, finalHeight, true)
//        Log.d("rd", "ratioBitmap: " + ratioBitmap.toString())
//        Log.d("rd", "finalWidth: " + finalWidth.toString() + " finalHeight: " + finalHeight.toString())
//
//        imageScaled
//    } else {
//        image
//    }
//}

//fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
//    TypedValue.COMPLEX_UNIT_DIP,
//    dp.toFloat(),
//    resources.displayMetrics
//)

@Composable
private fun getDeviceWidthAndHeight(): Pair<Int, Int>{
    val metrics = DisplayMetrics()
    val windowManager = LocalContext.current.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getRealMetrics(metrics)
    return Pair(metrics.widthPixels, metrics.heightPixels)
}
