package com.example.mvvmtask.data.repositoryImp

import com.example.mvvmtask.data.model.WallPaperPhotos
import com.example.mvvmtask.data.network.ApiService


class WallPaperRepository(private val apiService: ApiService) {

    suspend fun getCuratedPhotosList(
        perPage: Int,
        page: Int
    ): List<WallPaperPhotos> {
        return apiService
            .getCuratedPhotos(perPage, page)
            .photos

    }

    suspend fun getSearchedPhotosList(
        query: String,
        perPage: Int,
        page: Int
    ): List<WallPaperPhotos> {
        return apiService
            .searchPhotos(query,perPage, page)
            .photos

    }
}