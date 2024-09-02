package com.example.mvvmtask.ui.main.editImage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.utils.applyBrightnessContrast
import com.example.mvvmtask.utils.saveImage
import kotlinx.coroutines.CoroutineScope

@Composable
fun EditImageScreen(
    scope: CoroutineScope,
    savedPhotosEntity: SavedPhotosEntity?,
    savedPhotosViewModel: SavedPhotosViewModel,
    navController: NavHostController,
    onSaveImage: () -> Unit
) {
    var brightness by remember { mutableFloatStateOf(1f) }  // Initial brightness level
    var contrast by remember { mutableFloatStateOf(1f) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val originalBitmap = remember {
        BitmapFactory.decodeFile(savedPhotosEntity?.imagePath)
    }

    val context = LocalContext.current
    LaunchedEffect(brightness, contrast) {
        editedBitmap = applyBrightnessContrast(originalBitmap, brightness, contrast)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        editedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                contentScale = ContentScale.Crop
            )
        }

        // Brightness Slider
        Text(text = "Brightness", modifier = Modifier.padding(top = 16.dp))
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..2f,  // Adjust the range as needed
            modifier = Modifier.fillMaxWidth()
        )

        // Contrast Slider
        Text(text = "Contrast", modifier = Modifier.padding(top = 16.dp))
        Slider(
            value = contrast,
            onValueChange = { contrast = it },
            valueRange = 0f..2f,  // Adjust the range as needed
            modifier = Modifier.fillMaxWidth()
        )

        // Save Button
        Button(
            onClick = {
                editedBitmap?.let { bitmap ->
                    context.saveImage(scope, bitmap) { path ->
                        val updatedEntity = savedPhotosEntity?.copy(imagePath = path)
                        updatedEntity?.let {
                            savedPhotosViewModel.insertPhotos(it)
                        }
                        Toast.makeText(context, "Image Saved Successfully", Toast.LENGTH_SHORT)
                            .show()
                        navController.popBackStack()
                    }
                    onSaveImage()
                }
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(10.dp, 10.dp)
                .background(
                    Color(0xFF000000),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                color = Color.White,
                text = "Save Image", style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }


}