package com.example.mvvmtask.navigation

import GalleryImagesViewer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.ui.main.camera.CameraPhotos
import com.example.mvvmtask.ui.main.editImage.EditImageScreen
import com.example.mvvmtask.ui.main.gallery.GalleryPhotos
import com.example.mvvmtask.ui.main.photos.ImageViewer
import com.example.mvvmtask.ui.main.photos.PhotoListScreen
import com.example.mvvmtask.ui.main.saved.SavedImageView
import com.example.mvvmtask.ui.main.saved.SavedPhotosScreen
import com.example.mvvmtask.ui.main.splash.AnimatedSplashScreen
import com.example.mvvmtask.ui.viewmodel.GalleryImagesViewModel
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.ui.viewmodel.WallPaperViewModel
import com.example.mvvmtask.utils.SAVED
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun SetupNavGraph(
    padding: PaddingValues,
    navController: NavHostController,
    startDestination: String,
    wallPaperViewModel: WallPaperViewModel,
    savedPhotosViewModel: SavedPhotosViewModel,
    galleryImagesViewModel: GalleryImagesViewModel

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(padding)) {
        NavHost(
            navController,
            startDestination = startDestination
        ) {
            composable(Screen.Splash.route) { AnimatedSplashScreen(navController) }
            composable(Screen.LivePhotos.route) {
                PhotoListScreen(wallPaperViewModel) { position ->
                    navController.navigate(route = "ImageViewer/$position")
                }
                BackHandler {
                    navController.popBackStack()
                }
            }
            composable(Screen.ImageViewer.route) { backStackEntry ->
                val position = backStackEntry.arguments?.getString("position")?.toInt() ?: 0
                ImageViewer(wallPaperViewModel, position) /*{ image ->
                    urlToBitmap(scope, image.src.large, context, onSuccess = {
                        context.compressAndSaveImageToMediaStore(it) { path ->
                            val savedPhotosEntity =
                                SavedPhotosEntity(image.id, path, image.photographer)
                            savedPhotosViewModel.insertPhotos(savedPhotosEntity)
                            Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                        }
                    }, onError = {
                        Toast.makeText(context, "Error to save image", Toast.LENGTH_SHORT).show()
                    })*/

//                }
            }
            composable(Screen.GalleryPhotosScreen.route) {
                GalleryPhotos(galleryImagesViewModel) { value ->
                    val (imagesList, position) = value
                    galleryImagesViewModel.setImageData(imagesList)
                    val from = "none"
                    navController.navigate("GalleryImageViewerScreen/$position/$from")
                }
            }

            composable(Screen.GalleryImageScreen.route) { navBackStackEntry ->

                val position = navBackStackEntry.arguments?.getString("position")?.toInt() ?: 0
                val openFrom = navBackStackEntry.arguments?.getString("OPEN_FROM") ?: ""

                GalleryImagesViewer(
                    galleryImagesViewModel = galleryImagesViewModel,
                    position = position,
                    from = openFrom
                )
            }
            composable(Screen.CameraPhotosScreen.route) { CameraPhotos(savedPhotosViewModel) }
            composable(Screen.Saved.route) {
                SavedPhotosScreen(savedPhotosViewModel, onClickApplyFilter = {
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val encodedSrcJson = URLEncoder.encode(jsonString, "UTF-8")
                    navController.navigate("SavedImageViewer/$encodedSrcJson")
                }, onClickViewImage = {
                    navController.navigate("GalleryImageViewerScreen/$it/$SAVED")
                }, onClickEditImage = {
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val encodedSrcJson = URLEncoder.encode(jsonString, "UTF-8")
                    navController.navigate("EditImageScreen/$encodedSrcJson")
                })
            }


            composable(Screen.SavedImageViewer.route) { navBackStackEntry ->
                val jsonString = navBackStackEntry.arguments?.getString("savedItem")
                val srcJson = URLDecoder.decode(jsonString, "UTF-8")
                val gson = Gson()
                val savedPhotosEntity = gson.fromJson(srcJson, SavedPhotosEntity::class.java)
                SavedImageView(scope, savedPhotosEntity = savedPhotosEntity)
            }

            composable(Screen.EditImageScreen.route) { navBackStackEntry ->
                val jsonString = navBackStackEntry.arguments?.getString("savedItemEdit")
                val srcJson = URLDecoder.decode(jsonString, "UTF-8")
                val gson = Gson()
                val savedPhotosEntity = gson.fromJson(srcJson, SavedPhotosEntity::class.java)
                EditImageScreen(
                    savedPhotosEntity = savedPhotosEntity,
                    savedPhotosViewModel,
                    navController
                ) {

                }
            }


        }
    }
}