package com.example.mvvmtask.ui.main.gallery

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.size.Size
import com.example.mvvmtask.data.model.gallery.ImageData
import com.example.mvvmtask.ui.main.photos.LoadingView
import com.example.mvvmtask.utils.Status

@Composable
fun GalleryPhotos(
    galleryImagesViewModel: GalleryImagesViewModel,
    onClickImage: (Pair<List<ImageData>, Int>) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryImagesState by galleryImagesViewModel.galleryImages.collectAsState()

    val storagePermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        }

        else -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    // Check permissions when the composable is first launched
    LaunchedEffect(Unit) {
        val allPermissionsGranted = storagePermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            galleryImagesViewModel.fetchImagesFromStorage(context)
            permissionGranted = true
        } else {
            permissionRequested = true
            showRationale = storagePermissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
            }
            permanentlyDenied = storagePermissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            permission
                        )
            }
        }
    }

    // Launcher for requesting permissions
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionGranted = permissions.all { it.value }
            if (permissionGranted) {
                galleryImagesViewModel.fetchImagesFromStorage(context)
            } else {
                showRationale = storagePermissions.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        permission
                    )
                }
                permanentlyDenied = storagePermissions.all { permission ->
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                permission
                            )
                }
            }
        }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!permissionGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!permissionGranted && permissionRequested) {
                    if (showRationale) {
                        Text("Storage permission is needed. Go to settings to enable.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    } else {
                        Text("Storage permission is needed to access images.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            launcher.launch(storagePermissions)
                        }) {
                            Text("Request Permission")
                        }
                    }
                }
            }
        } else {
            when (galleryImagesState.status) {
                Status.SUCCESS -> {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .padding(top = 10.dp, bottom = 10.dp),
                        columns = StaggeredGridCells.Fixed(2)
                    ) {
                        galleryImagesState.data?.let { images ->
                            items(images.size) { index ->
                                ImageCard(images, index, onClickImage)
                            }
                        } ?: run {
                            item {
                                Text("No images available.")
                            }
                        }
                    }
                }

                Status.LOADING -> {
                    LoadingView()
                }

                Status.ERROR -> {
                    Text("Error: ${galleryImagesState.message}")
                }
            }
        }
    }
}

@Composable
fun ImageCard(
    imageList: List<ImageData>,
    position: Int,
    onClickImage: (Pair<List<ImageData>, Int>) -> Unit
) {
    val image = imageList[position]
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable {
            onClickImage(imageList to position)
        }) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.uri)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Text(
                text = image.name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000000)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun optimizedImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()
}
