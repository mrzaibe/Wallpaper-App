package com.example.mvvmtask.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mvvmtask.R
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun CameraPreviewScreen(savedPhotosViewModel: SavedPhotosViewModel) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showBlackScreen by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        if (showBlackScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        } else {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        }

        capturedImageBitmap?.let { bitmap ->
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.captured_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 10.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            context.compressAndSaveImageToMediaStore(bitmap) {
                                savedPhotosViewModel.insertPhotos(
                                    SavedPhotosEntity(
                                        imagePath = it.toString(),
                                        title = File(it ?: "").name
                                    )
                                )
                            }
                            capturedImageBitmap = null
                            showBlackScreen = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(vertical = 10.dp)
                            .background(
                                Color(0xFF000000),
                                shape = RoundedCornerShape(12.dp)
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.save_image),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Button(
                        onClick = {
                            capturedImageBitmap = null
                            showBlackScreen = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        modifier = Modifier
                            .weight(0.7f)
                            .padding(vertical = 10.dp)
                            .background(
                                Color(0xFF000000),
                                shape = RoundedCornerShape(12.dp)
                            ),
                    ) {
                        Text(
                            color = Color.White,
                            text = stringResource(R.string.retake),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } ?: run {
            Button(
                onClick = {
                    captureImageInMemory(imageCapture, context) { bitmap ->
                        showBlackScreen = true
                        capturedImageBitmap = bitmap
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp)
                    .background(
                        Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                Text(
                    color = Color.White,
                    text = stringResource(R.string.capture_image),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }


private fun captureImageInMemory(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Bitmap) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {

                val rotationDegrees = image.imageInfo.rotationDegrees
                val bitmap = image.toBitmap()
                val rotatedBitmap = bitmap.rotate(rotationDegrees.toFloat())
                onImageCaptured(rotatedBitmap)
                logMessage("Successfully captured image")
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                logMessage("Failed: $exception")
            }
        }
    )
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}

private fun saveCapturedImage(
    context: Context,
    bitmap: Bitmap,
    savedPhotosViewModel: SavedPhotosViewModel
) {
    val name = "CameraImage${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$SAVED_FOLDER")
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        val outputStream = resolver.openOutputStream(it)
        outputStream?.use { outImage ->
            savedPhotosViewModel.insertPhotos(
                SavedPhotosEntity(
                    imagePath = it.toString(),
                    title = name
                )
            )
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outImage)
            context.showToast(context.getString(R.string.successfully_saved_image))
        }
    }
}

/*private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Uri) -> Unit
) {
    val name = "CameraImage${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$SAVED_FOLDER")
        }
    }
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let { uri ->
                    onImageCaptured(uri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                logMessage("Failed: $exception")
            }
        })
}*/

/*private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

private fun saveCapturedImage(context: Context, uri: Uri) {
    val wallpaperFolderName = SAVED_FOLDER

    val downloadsDirectory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val wallpaperDirectory = File(downloadsDirectory, wallpaperFolderName)
    if (!wallpaperDirectory.exists()) {
        val created = wallpaperDirectory.mkdirs()
        if (created) {
            logMessage("Created $wallpaperFolderName")
        } else {
            logMessage("Failed to create Wallpaper folder")
            return
        }
    }
    val contentValues = ContentValues().apply {
        put(
            MediaStore.MediaColumns.DISPLAY_NAME,
            "WallpaperImage_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$wallpaperFolderName"
            )
        } else {
            val file = File(wallpaperDirectory, "WallpaperImage_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.DATA, file.absolutePath)
        }
    }

    val resolver = context.contentResolver
    val imageUri: Uri? =
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    imageUri?.let { newUri ->
        val outputStream: OutputStream? = resolver.openOutputStream(newUri)

        if (outputStream != null) {
            try {
                val inputStream = resolver.openInputStream(uri)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                context.showToast("Image saved successfully at: $newUri")
            } catch (e: Exception) {
                context.showToast("Failed to save the image: $e")
            } finally {
                outputStream.close()
            }
        } else {
            context.showToast("Failed to get output stream for saving the image")
        }
    } ?: run {
        context.showToast("Failed to save image, Uri is null")
    }
}*/


/*
private fun captureImage(imageCapture: ImageCapture, context: Context) {
    val name = "CameraxImage${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
    }
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                println("Successs")
            }

            override fun onError(exception: ImageCaptureException) {
                println("Failed $exception")
            }

        })
}*/
