package com.example.mvvmtask.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity
import com.example.mvvmtask.data.repositoryImp.SavedPhotosRepository
import com.example.mvvmtask.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedPhotosViewModel(private var savedPhotosRepository: SavedPhotosRepository) : ViewModel() {

    private val _savedPhotosPhotos = MediatorLiveData<Resource<List<SavedPhotosEntity>>>()
    val savedPhotosPhotos: LiveData<Resource<List<SavedPhotosEntity>>> get() = _savedPhotosPhotos


    var savedPhotosEntity:SavedPhotosEntity?=null

    init {
        getAllSavedPhotos()
    }

    private fun getAllSavedPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            _savedPhotosPhotos.postValue(Resource.loading(null))
            try {
                val photos = savedPhotosRepository.getAllSavedPhotos()
                _savedPhotosPhotos.addSource(photos) { allPhotos ->
                    _savedPhotosPhotos.value = Resource.success(allPhotos)
                }
            } catch (e: Exception) {
                _savedPhotosPhotos.postValue(Resource.error(null, "${e.message} SomeThing Went Wrong"))
            }
        }
    }

    fun insertPhotos(savedPhotosEntity: SavedPhotosEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            savedPhotosRepository.insertPhotos(savedPhotosEntity)
        }
    }
}