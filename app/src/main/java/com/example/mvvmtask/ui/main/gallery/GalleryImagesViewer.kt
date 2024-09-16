package com.example.mvvmtask.ui.main.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun GalleryImagesViewer(galleryImagesViewModel: GalleryImagesViewModel, position: Int) {

    val galleryImagesState by galleryImagesViewModel.galleryImages.collectAsState()
    galleryImagesState.data?.let {
        if (it.isNotEmpty())
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(galleryImagesState.data?.get(position)?.uri)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                alignment = Alignment.Center
            )
    }

}
