package com.example.mvvmtask.data.network

import com.example.mvvmtask.data.model.apimodel.WallPaperModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("curated")
    suspend fun getCuratedPhotos(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): WallPaperModel

    @GET("search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): WallPaperModel

}