import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.mvvmtask.R
import com.example.mvvmtask.ui.viewmodel.GalleryImagesViewModel
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.utils.ProgressDialog
import com.example.mvvmtask.utils.compressAndSaveImage
import com.example.mvvmtask.utils.showToast
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryImagesViewer(
    savedPhotosViewModel: SavedPhotosViewModel = getViewModel(),
    galleryImagesViewModel: GalleryImagesViewModel,
    position: Int, from: String = "none"
) {

    val galleryImages = if (from != "none") {
        val savedImagesState by savedPhotosViewModel.savedPhotosPhotos.observeAsState()
        savedImagesState?.data ?: emptyList()
    } else {
        val galleryImagesState by galleryImagesViewModel.galleryImages.collectAsState()
        galleryImagesState.data ?: emptyList()
    }

    val pagerState = rememberPagerState()
    var currentPage by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    var titleDialog by remember { mutableStateOf("Images Compressing") }
    var showDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0 to 0) }
    var job by remember { mutableStateOf<Job?>(null) }



    if (galleryImages.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${currentPage + 1} / ${galleryImages.size}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp),
                color = Color.Black,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
            )
            var scale by remember {
                mutableFloatStateOf(1f)
            }
            val offset by remember {
                mutableStateOf(Offset.Zero)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                }
                HorizontalPager(
                    count = galleryImages.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .transformable(state)
                ) { page ->
                    GalleryImageCardPager(galleryImages[page].imagePath)
                }
                LaunchedEffect(key1 = position) {
                    if (galleryImages.isNotEmpty() && position in galleryImages.indices) {
                        pagerState.scrollToPage(position)
                    }
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        currentPage = page
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(colors = ButtonDefaults.buttonColors(Color.Transparent), modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .background(
                        Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp)
                    ), onClick = {
                    if (job?.isActive == true) {
                        context.showToast(context.getString(R.string.a_job_is_already_running))
                        return@Button
                    }
                    showDialog = true
                    titleDialog = context.getString(R.string.image_saving)
                    job = compressAndSaveImage(
                        listOf(galleryImages[currentPage]),
                        quality = 100,
                        onSavedImages = {
                            CoroutineScope(Dispatchers.IO).launch {
                                savedPhotosViewModel.insertMultiplePhotos(it)
                            }
                        },
                        onProgressUpdate = { processed, total ->
                            progress = processed to total
                        },
                        onCompletion = {
                            context.showToast(context.getString(R.string.image_saved))
                            showDialog = false
                            job?.cancel()
                        },
                        onError = { errorMessage ->
                            showDialog = false
                            context.showToast(errorMessage)
                        })
                }) {
                    Text(
                        text = stringResource(R.string.save),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp)
                        .background(
                            Color(0xFF000000),
                            shape = RoundedCornerShape(12.dp)
                        ), onClick = {
                        if (job?.isActive == true) {
                            context.showToast(context.getString(R.string.a_job_is_already_running))
                            return@Button
                        }
                        showDialog = true
                        titleDialog = context.getString(R.string.images_saving)
                        job = compressAndSaveImage(galleryImages, quality = 100, onSavedImages = {
                            savedPhotosViewModel.insertMultiplePhotos(it)
                        }, onProgressUpdate = { processed, total ->
                            progress = processed to total
                        }, onCompletion = {
                            context.showToast(context.getString(R.string.images_saved))
                            showDialog = false
                            job?.cancel()
                        }, onError = { errorMessage ->
                            showDialog = false
                            context.showToast(errorMessage)
                        })

                    }) {
                    Text(
                        text = stringResource(R.string.save_all),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(colors = ButtonDefaults.buttonColors(Color.Transparent), modifier = Modifier
                    .weight(1f)
                    .background(
                        Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp)
                    ), onClick = {
                    if (job?.isActive == true) {
                        context.showToast(context.getString(R.string.a_job_is_already_running))
                        return@Button
                    }
                    showDialog = true
                    titleDialog = context.getString(R.string.image_compressing)
                    job = compressAndSaveImage(
                        listOf(galleryImages[currentPage]),
                        onSavedImages = {
                            CoroutineScope(Dispatchers.IO).launch {
                                savedPhotosViewModel.insertMultiplePhotos(it)
                            }
                        },
                        onProgressUpdate = { processed, total ->
                            progress = processed to total
                        },
                        onCompletion = {
                            context.showToast(context.getString(R.string.image_compressed))
                            showDialog = false
                        },
                        onError = { errorMessage ->
                            showDialog = false
                            context.showToast(errorMessage)
                        })

                }) {
                    Text(
                        text = stringResource(R.string.compress),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(colors = ButtonDefaults.buttonColors(Color.Transparent), modifier = Modifier
                    .weight(1f)
                    .background(
                        Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp)
                    ), onClick = {
                    if (job?.isActive == true) {
                        context.showToast(context.getString(R.string.a_job_is_already_running))
                        return@Button
                    }
                    showDialog = true
                    titleDialog = context.getString(R.string.images_compressing)
                    job = compressAndSaveImage(galleryImages, onSavedImages = {
                        savedPhotosViewModel.insertMultiplePhotos(it)
                    }, onProgressUpdate = { processed, total ->
                        progress = processed to total
                    }, onCompletion = {
                        context.showToast(context.getString(R.string.images_compressed))
                        showDialog = false
                    }, onError = { errorMessage ->
                        showDialog = false
                        context.showToast(errorMessage)
                    })

                }) {
                    Text(
                        text = stringResource(R.string.compress_all),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }

    if (showDialog) {
        ProgressDialog(
            titleDialog,
            progress = progress.first,
            total = progress.second,
            onDismiss = {
                job?.cancel() // Cancel the job when the dialog is dismissed
                showDialog = false
            }
        )
    }
}

@Composable
fun GalleryImageCardPager(imagePath: String) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imagePath).size(Size.ORIGINAL)
                .crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            alignment = Alignment.Center,
            contentScale = ContentScale.FillWidth
        )


}
