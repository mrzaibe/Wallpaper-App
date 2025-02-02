package com.example.mvvmtask.navigation

import com.example.mvvmtask.R


sealed class Screen(
    val route: String,
    val icon_inactive: Int? = null,
    val icon_active: Int? = null
) {
    object Splash : Screen(route = "splash")
    object Home : Screen(
        route = "PhotoListScreen",
        icon_inactive = R.drawable.ic_home_inactive,
        icon_active = R.drawable.ic_home_active
    )

    object Saved : Screen(
        route = "savedScreen",
        icon_inactive = R.drawable.ic_bookmark_inactive,
        icon_active = R.drawable.ic_bookmark_active
    )

    object ImageViewer : Screen(route = "ImageViewer/{photoItem}")
    object SavedImageViewer : Screen(route = "SavedImageViewer/{savedItem}")
    object EditImageScreen : Screen(route = "EditImageScreen/{savedItemEdit}")
}