package com.example.mvvmtask.utils

import android.Manifest
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.Transformation
import com.example.mvvmtask.R
import com.example.mvvmtask.data.database.entities.ImageEntity
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.data.model.apimodel.WallPaperPhotos
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


fun Context.hasInternetConnection(): Boolean {
    var hasInternetConnection = false
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
            hasInternetConnection = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
    } else {
        hasInternetConnection =
            connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo?.isConnected == true
    }
    return hasInternetConnection
}

fun Context.saveCapturedImage(
    bitmap: Bitmap,
    onSavedSuccess: ((String?) -> Unit)
) {
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    requestForStoragePermission(permissions) {
        val name = "Image${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$SAVED_FOLDER")
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val outputStream = resolver.openOutputStream(it)
            outputStream?.use { outImage ->
                val absolutePath = getRealPathFromURI(it, this)
                onSavedSuccess(absolutePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outImage)
                showToast(getString(R.string.successfully_saved_image))
            }
        }
    }
}

fun Context.compressAndSaveImageToMediaStore(
    bitmap: Bitmap,
    onResult: (String?) -> Unit
) {
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    requestForStoragePermission(permissions) { granted ->
        if (granted) {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$SAVED_FOLDER")
                }

                val imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                imageUri?.let { uri ->
                    val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                    outputStream?.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    uri.toString()
                }
            } else {
                val externalStorageDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val savedFolderDir = File(externalStorageDir, SAVED_FOLDER)

                if (!savedFolderDir.exists()) {
                    savedFolderDir.mkdirs()
                }

                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(savedFolderDir, fileName)
                FileOutputStream(destinationFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                destinationFile.absolutePath
            }
            onResult.invoke(result)
        } else {
            onResult.invoke(null)
        }

    }
}

fun Context.requestForCameraPermission(
    permissions: Array<String>,
    onGranted: ((Boolean) -> Unit)? = null
) {
    Permissions.check(this, permissions, null, null, object : PermissionHandler() {
        override fun onGranted() {
            onGranted?.invoke(true)
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
            super.onDenied(context, deniedPermissions)
            onGranted?.invoke(false)
        }

        override fun onBlocked(
            context: Context?, blockedList: ArrayList<String>?
        ): Boolean {
            return super.onBlocked(context, blockedList)
        }

        override fun onJustBlocked(
            context: Context?,
            justBlockedList: ArrayList<String>?,
            deniedPermissions: ArrayList<String>?
        ) {
            super.onJustBlocked(context, justBlockedList, deniedPermissions)
        }
    })

}

fun Context.requestForStoragePermission(
    permissions: Array<String>,
    onComposeGranted: ((Boolean) -> Unit)? = null,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onComposeGranted?.invoke(true)
        return
    }
    Permissions.check(this, permissions, null, null, object : PermissionHandler() {
        override fun onGranted() {
            onComposeGranted?.invoke(true)
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
            super.onDenied(context, deniedPermissions)
            onComposeGranted?.invoke(false)
        }

        override fun onBlocked(
            context: Context?, blockedList: ArrayList<String>?
        ): Boolean {
            return super.onBlocked(context, blockedList)
        }

        override fun onJustBlocked(
            context: Context?,
            justBlockedList: ArrayList<String>?,
            deniedPermissions: ArrayList<String>?
        ) {
            super.onJustBlocked(context, justBlockedList, deniedPermissions)
        }
    })

}

fun Context.requestForGalleryPermission(
    permissions: Array<String>,
    onComposeGranted: ((Boolean) -> Unit)? = null,
) {

    Permissions.check(this, permissions, null, null, object : PermissionHandler() {
        override fun onGranted() {
            onComposeGranted?.invoke(true)
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
            super.onDenied(context, deniedPermissions)
            onComposeGranted?.invoke(false)
        }

        override fun onBlocked(
            context: Context?, blockedList: ArrayList<String>?
        ): Boolean {
            return super.onBlocked(context, blockedList)
        }

        override fun onJustBlocked(
            context: Context?,
            justBlockedList: ArrayList<String>?,
            deniedPermissions: ArrayList<String>?
        ) {
            super.onJustBlocked(context, justBlockedList, deniedPermissions)
        }
    })

}

fun getRealPathFromURI(uri: Uri, context: Context): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = it.getString(columnIndex)
        }
    }
    return filePath
}

fun compressAndSaveImage(
    sourcePath: List<ImageEntity>,
    quality: Int = 50,
    onSavedImages: (List<SavedPhotosEntity>) -> Unit,
    onProgressUpdate: ((Int, Int) -> Unit)? = null,
    onCompletion: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null
): Job {
    val job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    val imagesList = mutableListOf<SavedPhotosEntity>()
    coroutineScope.launch {
        try {
            sourcePath.forEachIndexed { index, source ->
                if (!job.isActive) return@forEachIndexed
                val name = "Image${System.currentTimeMillis()}.jpg"
                val options = BitmapFactory.Options().apply {
                    // Set this to true to get image dimensions without loading into memory
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(source.imagePath, options)
                options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeFile(source.imagePath, options)
                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        onError?.invoke("Failed to decode image at ${source.imagePath}")
                    }
                    return@forEachIndexed
                }
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                var destinationFile = File(directory, SAVED_FOLDER)
                if (!destinationFile.exists()) {
                    destinationFile.mkdirs()
                }
                destinationFile = File(destinationFile, name)
                if (destinationFile.exists()) {
                    return@forEachIndexed
                }
                FileOutputStream(destinationFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                }
                imagesList.add(SavedPhotosEntity(null, destinationFile.absolutePath, name))
                onProgressUpdate?.invoke(index + 1, sourcePath.size)
            }
            withContext(Dispatchers.Main) {
                onSavedImages.invoke(imagesList)
                onCompletion?.invoke()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError?.invoke(e.message ?: "An error occurred")
                Log.d(TAG, "compressAndSaveImage: ${e.message}")
            }
        }
    }
    return job
}

fun Context.compressAndSaveLiveImage(
    sourcePath: List<WallPaperPhotos>,
    quality: Int = 50,
    onSavedImages: (List<SavedPhotosEntity>) -> Unit,
    onProgressUpdate: ((Int, Int) -> Unit)? = null,
    onCompletion: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null
): Job {
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var bitmap: Bitmap? = null
    val job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    val imagesList = mutableListOf<SavedPhotosEntity>()
    requestForStoragePermission(permissions) { granted ->
        coroutineScope.launch {
            try {
                sourcePath.forEachIndexed { index, source ->
                    if (!job.isActive) return@forEachIndexed
                    val name = "Image${System.currentTimeMillis()}.jpg"
                    bitmap = urlToBitmapSuspend(
                        source.src.large,
                        this@compressAndSaveLiveImage,
                    )
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            onError?.invoke("Failed to decode image at $source")
                        }
                        return@forEachIndexed
                    }
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    var destinationFile = File(directory, SAVED_FOLDER)
                    if (!destinationFile.exists()) {
                        destinationFile.mkdirs()
                    }
                    destinationFile = File(destinationFile, name)
                    if (destinationFile.exists()) {
                        return@forEachIndexed
                    }
                    FileOutputStream(destinationFile).use { outputStream ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                    }
                    imagesList.add(SavedPhotosEntity(null, destinationFile.absolutePath, name))
                    onProgressUpdate?.invoke(index + 1, sourcePath.size)
                }
                withContext(Dispatchers.Main) {
                    onSavedImages.invoke(imagesList)
                    onCompletion?.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError?.invoke(e.message ?: "An error occurred")
                    Log.d(TAG, "compressAndSaveImage: ${e.message}")
                }
            }
        }
    }
    return job
}

@Composable
fun ProgressDialog(
    title: String,
    progress: Int,
    total: Int,
    onDismiss: () -> Unit
) {
    val displayProgress = if (total > 0) (progress / total.toFloat()).coerceIn(0f, 1f) else 0f
    val displayText = if (title.contains("sav", ignoreCase = true)) {
        "Saved $progress of $total images"
    } else {
        "Compressed $progress of $total images"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = displayProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(displayText)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }, properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

fun compressAndSaveImage(
    context: Context,
    sourcePath: List<ImageEntity>,
    quality: Int = 50
): List<Uri> {
    val uris = mutableListOf<Uri>()
    CoroutineScope(Dispatchers.IO).launch {
        sourcePath.forEach { source ->
            val name = "Image${System.currentTimeMillis()}.jpg"
            val externalStorageDir = "/storage/emulated/0/Pictures/$SAVED_FOLDER"
            val destinationFile = File(externalStorageDir, name)

            if (destinationFile.parentFile?.exists() == false) {
                destinationFile.parentFile?.mkdirs()
            }

            val bitmap = BitmapFactory.decodeFile(source.imagePath)
            FileOutputStream(destinationFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destinationFile
            )
            var absolutePath: String? = null
            uris.add(fileUri)
        }

    }
    return uris
}

fun Context.saveImage(
    bitmap: Bitmap,
    quality: Int = 50,
    onSavedSuccess: ((String?) -> Unit)
): String {
    val myDir = File(applicationContext.filesDir, "GalleryApp")
    if (myDir.exists().not()) myDir.mkdirs()
    val fileName = "Image${System.currentTimeMillis()}.jpg"
    val file = File(myDir, fileName)
    return if (file.exists()) {
        onSavedSuccess?.invoke(file.path)
        file.path
    } else {
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos)
            fos.flush()
            fos.close()
            onSavedSuccess?.invoke(file.path)
            file.path
        } catch (e: Exception) {
            onSavedSuccess?.invoke(null)
            //logEvents("Error in saving icon $e")
            ""
        }
    }
}

suspend fun urlToBitmapSuspend(
    imageURL: String,
    context: Context,
    transformation: Transformation? = null
): Bitmap? {
    return withContext(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageURL)
            .transformations(transformation ?: DefaultTransformation())
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        if (result is SuccessResult) {
            (result.drawable as BitmapDrawable).bitmap
        } else {
            null
        }
    }
}

fun urlToBitmap(
    imageURL: String,
    context: Context,
    transformation: Transformation? = null,
    onSuccess: (Bitmap) -> Unit,
    onError: (Throwable) -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageURL)
                .transformations(transformation ?: DefaultTransformation())
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                withContext(Dispatchers.Main) {
                    onSuccess(bitmap)
                }
            } else if (result is ErrorResult) {
                throw result.throwable
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onError(throwable)
            }
        }
    }
}

fun setWallpaper(context: Context, bitmap: Bitmap, flagSystem: Int) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wallpaperManager.setBitmap(bitmap, null, true, flagSystem)
        } else {
            wallpaperManager.setBitmap(bitmap)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle the exception (e.g., show an error message)
    }
}

suspend fun loadScaledBitmap(imagePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true // Don't load the actual bitmap yet
            }
            BitmapFactory.decodeFile(imagePath, options)

            // Calculate the scaling factor
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false // Now load the bitmap

            BitmapFactory.decodeFile(imagePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun logMessage(message: String) {
    Log.d(TAG, "--->: $message")
}