package com.example.mvvmtask.ui.main.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.utils.Resource
import com.example.mvvmtask.utils.Status

@Composable
fun SavedPhotosScreen(
    wallPaperViewModel: SavedPhotosViewModel,
    onClickViewImage: (Int) -> Unit,
    onClickEditImage: (SavedPhotosEntity) -> Unit,
    onClickApplyFilter: (SavedPhotosEntity) -> Unit
) {

    val openDialog = remember { mutableStateOf(false) }

    val photoListState =
        wallPaperViewModel.savedPhotosPhotos.observeAsState(initial = Resource.loading(null))

    when (photoListState.value.status) {
        Status.LOADING -> {
            LoadingView()
        }

        Status.SUCCESS -> {
            photoListState.value.data?.let { photos ->
                if (photos.isEmpty()) {
                    ErrorView("No Saved Images found")
                }
                PhotoList(photos, openDialog, wallPaperViewModel)
            } ?: run {
                ErrorView("No Saved Images found")
            }
        }

        Status.ERROR -> {
            ErrorView(photoListState.value.message ?: "An error occurred")
        }
    }

    EditImageDialog(
        openDialog = openDialog,
        onClickViewImage,
        onClickEditImage,
        onClickApplyFilter,
        wallPaperViewModel
    )
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message, color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun PhotoList(
    photos: List<SavedPhotosEntity>,
    openDialog: MutableState<Boolean>,
    savedPhotosViewModel: SavedPhotosViewModel,
) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 10.dp).offset(x = 0.dp, y = (-16).dp),
        columns = StaggeredGridCells.Fixed(2),
    ) {
        items(photos) { photo ->
            PhotoItem(photo, openDialog, savedPhotosViewModel)
        }
    }
}

@Composable
fun PhotoItem(
    photo: SavedPhotosEntity,
    openDialog: MutableState<Boolean>,
    savedPhotosViewModel: SavedPhotosViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                savedPhotosViewModel.savedPhotosEntity = photo
                openDialog.value = true
            }
    ) {
        Column {
            AsyncImage(
                model = photo.imagePath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(), contentScale = ContentScale.FillWidth
            )
            Text(
                color = Color.White,
                text = photo.title.toString(),
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
fun EditImageDialog(
    openDialog: MutableState<Boolean>,
    onClickViewImage: (Int) -> Unit,
    onClickEditImage: (SavedPhotosEntity) -> Unit,
    onClickApplyFilter: (SavedPhotosEntity) -> Unit,
    savedPhotosViewModel: SavedPhotosViewModel,
) {
    if (openDialog.value) {
        AlertDialog(
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = "Edit Image")
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.White)
                            .border(1.dp, Color(0xFF888888), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                savedPhotosViewModel.savedPhotosEntity?.let {
                                     val position=savedPhotosViewModel.savedPhotosPhotos.value?.data?.indexOf(it)?:0
                                    onClickViewImage(position)
                                }
                                openDialog.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Image",
                            modifier = Modifier
                                .padding(10.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.White)
                            .border(1.dp, Color(0xFF888888), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                savedPhotosViewModel.savedPhotosEntity?.let { onClickEditImage(it) }
                                openDialog.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit Image",
                            modifier = Modifier
                                .padding(10.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(color = Color.White)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF888888), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                savedPhotosViewModel.savedPhotosEntity?.let { onClickApplyFilter(it) }

                                openDialog.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Apply Filter",
                            modifier = Modifier
                                .padding(10.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 8.dp)
                            .width(70.dp)
                            .height(3.dp)
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(50)
                            )
                            .align(Alignment.CenterHorizontally)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(color = Color.White)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF888888), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                openDialog.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .padding(10.dp)
                        )
                    }
                }
            },
            // No buttons
            confirmButton = {},
            dismissButton = {}
        )
    }
}
