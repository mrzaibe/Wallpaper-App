package com.example.mvvmtask.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.data.model.WallPaperPhotos
import com.example.mvvmtask.data.repositoryImp.WallPaperRepository
import com.example.mvvmtask.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class WallPaperViewModel(private var wallPaperRepository: WallPaperRepository) : ViewModel() {



    private val _curatedPhotos = MutableLiveData<Resource<List<WallPaperPhotos>>>()
    val curatedPhotos: LiveData<Resource<List<WallPaperPhotos>>> get() = _curatedPhotos

    var searchedText: String?=null


    init {
        fetchCuratedPhotos()
    }

    private fun fetchCuratedPhotos(perPageImages: Int = 30, page: Int = 1) {
        viewModelScope.launch {
            _curatedPhotos.postValue(Resource.loading(null))
            try {
                val photos = wallPaperRepository.getCuratedPhotosList(perPageImages, page)
                _curatedPhotos.postValue(Resource.success(photos))
            } catch (e: Exception) {
                _curatedPhotos.postValue(Resource.error(null, "You need proper internet connection for themes to load successfully!"))
            }
        }
    }

     fun fetchSearchedPhotos(query:String,perPageImages: Int = 30, page: Int = 1) {
        viewModelScope.launch {
            _curatedPhotos.postValue(Resource.loading(null))
            try {
                val photos = wallPaperRepository.getSearchedPhotosList(query,perPageImages, page)
                _curatedPhotos.postValue(Resource.success(photos))
            } catch (e: Exception) {
                _curatedPhotos.postValue(Resource.error(null, "You need proper internet connection for themes to load successfully!"))
            }
        }
    }


    fun refreshPhotos(perPageImages: Int = 30, page: Int = 1) {
        if (searchedText.isNullOrEmpty()){
            fetchCuratedPhotos(perPageImages, page)
        }else{
            fetchSearchedPhotos(searchedText?:"")
        }
    }


}