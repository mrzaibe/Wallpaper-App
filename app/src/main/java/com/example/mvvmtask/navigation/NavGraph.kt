package com.example.mvvmtask.navigation

import android.widget.Toast
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
import com.example.mvvmtask.data.model.apimodel.WallPaperPhotos
import com.example.mvvmtask.data.model.gallery.ImageData
import com.example.mvvmtask.ui.main.camera.CameraPhotos
import com.example.mvvmtask.ui.main.editImage.EditImageScreen
import com.example.mvvmtask.ui.main.gallery.GalleryImagesViewModel
import com.example.mvvmtask.ui.main.gallery.GalleryImagesViewer
import com.example.mvvmtask.ui.main.gallery.GalleryPhotos
import com.example.mvvmtask.ui.main.imageview.ImageViewer
import com.example.mvvmtask.ui.main.photos.PhotoListScreen
import com.example.mvvmtask.ui.main.saved.SavedImageView
import com.example.mvvmtask.ui.main.saved.SavedPhotosScreen
import com.example.mvvmtask.ui.main.splash.AnimatedSplashScreen
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.ui.viewmodel.WallPaperViewModel
import com.example.mvvmtask.utils.saveImage
import com.example.mvvmtask.utils.urlToBitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
                PhotoListScreen(wallPaperViewModel) {
                    val srcJson = Gson().toJson(it)
                    val encodedSrcJson = URLEncoder.encode(srcJson, "UTF-8")
                    navController.navigate(route = "ImageViewer/$encodedSrcJson")
                }
            }
            composable(Screen.GalleryPhotosScreen.route) {
                GalleryPhotos(galleryImagesViewModel) { value ->
                    val (imagesList, position) = value
                    galleryImagesViewModel.setImageData(imagesList)

                    navController.navigate("GalleryImageViewerScreen/$position")

                }
            }

            composable(Screen.GalleryImageScreen.route) { navBackStackEntry ->

                val position = navBackStackEntry.arguments?.getString("position")?.toInt() ?: 0

                GalleryImagesViewer(galleryImagesViewModel, position)
            }
            composable(Screen.CameraPhotosScreen.route) { CameraPhotos() }
            composable(Screen.Saved.route) {
                SavedPhotosScreen(savedPhotosViewModel, onClickApplyFilter = {
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val encodedSrcJson = URLEncoder.encode(jsonString, "UTF-8")
                    navController.navigate("SavedImageViewer/$encodedSrcJson")
                }, onClickEditImage = {
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val encodedSrcJson = URLEncoder.encode(jsonString, "UTF-8")
                    navController.navigate("EditImageScreen/$encodedSrcJson")
                })
            }
            composable(Screen.ImageViewer.route) { backStackEntry ->
                val arguments = requireNotNull(backStackEntry.arguments)
                val encodedSrcJson = arguments.getString("photoItem") ?: ""
                val srcJson = URLDecoder.decode(encodedSrcJson, "UTF-8")
                val src = Gson().fromJson(srcJson, WallPaperPhotos::class.java)
                ImageViewer(src) {
                    urlToBitmap(scope, src.src.large, context, onSuccess = {
                        context.saveImage(scope, it) { path ->
                            val savedPhotosEntity =
                                SavedPhotosEntity(src.id, path, src.photographer)
                            savedPhotosViewModel.insertPhotos(savedPhotosEntity)
                            Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                        }
                    }, onError = {
                        Toast.makeText(context, "Error to save image", Toast.LENGTH_SHORT).show()
                    })

                }
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
                    scope,
                    savedPhotosEntity = savedPhotosEntity,
                    savedPhotosViewModel,
                    navController
                ) {

                }
            }


        }
    }
}