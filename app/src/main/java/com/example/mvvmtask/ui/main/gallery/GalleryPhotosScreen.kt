package com.example.mvvmtask.ui.main.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.mvvmtask.data.model.gallery.ImageData
import com.example.mvvmtask.ui.main.saved.LoadingView
import com.example.mvvmtask.ui.viewmodel.GalleryImagesViewModel
import com.example.mvvmtask.utils.Status
import com.example.mvvmtask.utils.requestForGalleryPermission

@Composable
fun GalleryPhotos(
    galleryImagesViewModel: GalleryImagesViewModel,
    onClickImage: (Pair<List<ImageData>, Int>) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryImagesState by galleryImagesViewModel.galleryImages.collectAsState()
    val storagePermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        }

        else -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionGranted = true
            }
        }
        LaunchedEffect(permissionGranted) {
            if (permissionGranted) {
                galleryImagesViewModel.fetchImagesFromStorage(context)
            }
        }
        if (permissionGranted) {
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
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Storage permission is needed to access images.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.requestForGalleryPermission(storagePermissions) { isGranted ->
                        permissionGranted = isGranted
                    }
                }) {
                    Text("Request Permission")
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
                    .data(image.imagePath)
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
                text = image.title,
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