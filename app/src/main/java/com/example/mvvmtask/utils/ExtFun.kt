package com.example.mvvmtask.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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


fun Context.saveImage(
    scope: CoroutineScope,
    bitmap: Bitmap,
    quality: Int = 100,
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

fun urlToBitmap(
    scope: CoroutineScope,
    imageURL: String,
    context: Context,
    transformation: Transformation?=null,
    onSuccess: (bitmap: Bitmap) -> Unit,
    onError: (error: Throwable) -> Unit
) {
    var bitmap: Bitmap? = null
    val loadBitmap = scope.launch(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageURL)
            .transformations(transformation?:DefaultTransformation())
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        if (result is SuccessResult) {
            bitmap = (result.drawable as BitmapDrawable).bitmap
        } else if (result is ErrorResult) {
            cancel(result.throwable.localizedMessage ?: "ErrorResult", result.throwable)
        }
    }
    loadBitmap.invokeOnCompletion { throwable ->
        scope.launch(Dispatchers.Main) {
            bitmap?.let {
                onSuccess(it)
            } ?: throwable?.let {
                onError(it)
            } ?: onError(Throwable("Undefined Error"))
        }
    }
}

fun setWallpaper(context: Context, bitmap: Bitmap, flagSystem: Int) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wallpaperManager.setBitmap(bitmap,null,true,flagSystem)
        }else{
            wallpaperManager.setBitmap(bitmap)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle the exception (e.g., show an error message)
    }
}



