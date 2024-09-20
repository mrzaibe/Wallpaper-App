package com.example.mvvmtask.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmtask.data.model.gallery.ImageData
import com.example.mvvmtask.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class GalleryImagesViewModel(context: Context) : ViewModel() {
    private val _galleryImages = MutableStateFlow<Resource<List<ImageData>>>(Resource.loading(null))
    val galleryImages: StateFlow<Resource<List<ImageData>>> = _galleryImages

    private val _allImageViewList = MutableStateFlow<List<ImageData>>(emptyList())
    val allImageViewList: MutableStateFlow<List<ImageData>> = _allImageViewList


    fun setImageData(data: List<ImageData>) {
        _allImageViewList.value = data
    }

    fun fetchImagesFromStorage(context: Context) {
        Log.d("TAG", "getImagesFromDevice:")
        viewModelScope.launch(Dispatchers.IO) {
            _galleryImages.value = Resource.loading(null)
            try {
                val images = getImagesFromDevice(context)
                _galleryImages.value = Resource.success(images)
            } catch (e: Exception) {
                _galleryImages.value = Resource.error(null, e.message)
            }
        }
    }

    private fun getImagesFromDevice(context: Context): List<ImageData> {
        val images = mutableListOf<ImageData>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val data = it.getString(dataColumn)
                images.add(ImageData(id, name, data))
            }
        }

        return images
    }

    private fun getMediaFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

}