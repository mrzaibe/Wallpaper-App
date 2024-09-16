package com.example.mvvmtask.ui.main.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mvvmtask.data.model.apimodel.WallPaperPhotos
import com.example.mvvmtask.ui.viewmodel.WallPaperViewModel
import com.example.mvvmtask.utils.Resource
import com.example.mvvmtask.utils.Status
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    wallPaperViewModel: WallPaperViewModel,
    onClickImage: (WallPaperPhotos) -> Unit
) {
    val photoListState =
        wallPaperViewModel.curatedPhotos.observeAsState(initial = Resource.loading(null))
    var isRefreshing by remember { mutableStateOf(false) }
    var context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            label = { Text("Search Wallpaper", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                .border(1.dp, Color(0xFF888888), shape = RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    handleEnterKey(text, wallPaperViewModel)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,

            )
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                wallPaperViewModel.refreshPhotos()
                isRefreshing = false
            }
        ) {
            when (photoListState.value.status) {
                Status.LOADING -> {
                    LoadingView()
                }

                Status.SUCCESS -> {
                    photoListState.value.data?.let { photos ->
                        PhotoList(
                            photos,
                            onClickImage
                        )
                    }
                }

                Status.ERROR -> {
                    PhotoList {}
                    ErrorView(photoListState.value.message ?: "An error occurred")
                }
            }
        }
    }

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
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun PhotoList(photos: List<WallPaperPhotos>? = null, onClickImage: (WallPaperPhotos) -> Unit?) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(top = 10.dp, bottom = 10.dp),
        columns = StaggeredGridCells.Fixed(2),
    ) {
        photos?.let {
            items(photos) { photo ->
                PhotoItem(photo, onClickImage)
            }
        }
    }
}

@Composable
fun PhotoItem(photo: WallPaperPhotos, onClickImage: (WallPaperPhotos) -> Unit?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClickImage(photo)
            }
    ) {
        Column {
            AsyncImage(
                model = photo.src.large,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = photo.photographer,
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

fun handleEnterKey(text: String, wallPaperViewModel: WallPaperViewModel) {
    wallPaperViewModel?.fetchSearchedPhotos(text)
    wallPaperViewModel.searchedText = text
}