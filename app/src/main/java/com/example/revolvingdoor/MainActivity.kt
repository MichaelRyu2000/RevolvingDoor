package com.example.revolvingdoor

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.revolvingdoor.ui.theme.RevolvingDoorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RevolvingDoorTheme {
                val result = remember { mutableStateListOf<Uri>() }
                val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
                    result.addAll(it)
                }
                val resultList = result.toList()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestScreen(pickImages, resultList)
                }
            }
        }
    }
}

@Composable
fun TestScreen(pickImages: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>, imageList: List<Uri>, modifier: Modifier = Modifier) {
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
                   Log.d("rd", imageList[photo].toString())
                    AsyncImage(
                        model = imageList[photo],
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        placeholder = painterResource(R.drawable.missing_image),
                        modifier = Modifier
                            .size(150.dp)
                            .border(BorderStroke(1.dp, Color.Black)),
                    )
               }
           }
       }

       Spacer(modifier = Modifier.size(4.dp))
       Button(
           onClick = {
               pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
           },
           modifier = Modifier.weight(1f)
       ) {
           Text(
               text = "Choose picture(s)"
           )
       }
    }
}