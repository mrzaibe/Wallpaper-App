package com.example.mvvmtask.data.model

data class WallPaperModel(
    val next_page: String,
    val page: Int,
    val per_page: Int,
    val photos: List<WallPaperPhotos>,
    val total_results: Int
)