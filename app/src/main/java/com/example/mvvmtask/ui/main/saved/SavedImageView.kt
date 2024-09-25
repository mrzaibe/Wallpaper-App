package com.example.mvvmtask.ui.main.saved

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mvvmtask.R
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.utils.Filters
import com.example.mvvmtask.utils.setWallpaper
import com.example.mvvmtask.utils.urlToBitmap
import kotlinx.coroutines.CoroutineScope


@Composable
fun SavedImageView(
    scope: CoroutineScope,
    savedPhotosEntity: SavedPhotosEntity?
) {
    val showFilters = remember { mutableStateOf(false) }
    val selectedFilter = remember { mutableStateOf(Filters.Default) }
    val openDialog = remember { mutableStateOf(false) }
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        var scale by remember {
            mutableFloatStateOf(1f)
        }
        val offset by remember {
            mutableStateOf(Offset.Zero)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.9f)
        ) {
            val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(savedPhotosEntity?.imagePath)
                    .transformations(selectedFilter.value.filterType)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state)

            )
        }

        AnimatedVisibility(
            visible = showFilters.value,
            enter = slideInVertically(initialOffsetY = { it / 2 }),
            exit = slideOutVertically(targetOffsetY = { 0 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .background(Color(0x33000000))
                    .padding(vertical = 10.dp)
            ) {
                items(Filters.entries) { filter ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.LightGray,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(150.dp)
                            .clickable {
                                selectedFilter.value = filter
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(savedPhotosEntity?.imagePath)
                                    .transformations(filter.filterType)
                                    .build(),
                                contentDescription = filter.filtersTitle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            // Filter name
                            Text(
                                text = filter.filtersTitle,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF000000)),
                                maxLines = 1,
                                color = Color.White,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
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
                    urlToBitmap(
                        savedPhotosEntity?.imagePath ?: "",
                        context,
                        transformation = selectedFilter.value.filterType,
                        onSuccess = { loadedBitmap ->
                            bitmap.value = loadedBitmap
                            openDialog.value = true
                        },
                        onError = {}, scope
                    )
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
                    text = stringResource(R.string.set_wallpaper),
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
                    showFilters.value = !showFilters.value
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
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (openDialog.value) {
            SetWallPaperDialog(
                openDialog = openDialog,
                bitmap = bitmap.value
            )
        }
    }

}

@Composable
fun SetWallPaperDialog(
    openDialog: MutableState<Boolean>,
    bitmap: Bitmap?
) {
    val context = LocalContext.current
    if (openDialog.value) {
        AlertDialog(
            containerColor = Color.White,
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(16.dp)),
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = stringResource(R.string.do_you_want_to_set_this_image_as_wallpaper))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.set_to_home_screen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (bitmap != null) {
                                    setWallpaper(context, bitmap, WallpaperManager.FLAG_SYSTEM)
                                }
                                openDialog.value = false
                            }
                            .padding(horizontal = 5.dp, vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.set_to_lock_screen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (bitmap != null) {
                                    setWallpaper(context, bitmap, WallpaperManager.FLAG_LOCK)
                                }
                                openDialog.value = false
                            }
                            .padding(horizontal = 5.dp, vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.both),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (bitmap != null) {
                                    setWallpaper(
                                        context,
                                        bitmap,
                                        WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                                    )
                                }
                                openDialog.value = false
                            }
                            .padding(horizontal = 5.dp, vertical = 8.dp)
                    )
                }
            },
            // No buttons
            confirmButton = {},
            dismissButton = {}
        )
    }
}

