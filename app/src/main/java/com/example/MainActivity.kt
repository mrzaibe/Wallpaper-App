package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mvvmtask.navigation.BottomBar
import com.example.mvvmtask.navigation.Screen
import com.example.mvvmtask.navigation.SetupNavGraph
import com.example.mvvmtask.ui.theme.MVVMTaskTheme
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.ui.viewmodel.WallPaperViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val viewModel by inject<WallPaperViewModel>()
    private val savedPhotosViewModel by inject<SavedPhotosViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MVVMTaskTheme {
                App(wallPaperViewModel = viewModel, savedPhotosViewModel = savedPhotosViewModel)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(wallPaperViewModel: WallPaperViewModel, savedPhotosViewModel: SavedPhotosViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Saved)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Scaffold(
        modifier = Modifier.background(Color.White),
        bottomBar = {
            if (currentRoute in items.map { it.route }) {
                BottomBar(navController = navController)
            }
        }
    ) {
        Column(modifier = Modifier.padding(it).background(Color.White)) { // Pass padding from Scaffold to Column
            if (currentRoute in items.map { item -> item.route }) {
                Text(
                    text = "HD Wallpapers",
                    modifier = Modifier.padding(20.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            SetupNavGraph(
                navController = navController,
                startDestination = Screen.Splash.route,
                wallPaperViewModel,
                savedPhotosViewModel = savedPhotosViewModel
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MVVMTaskTheme {
        //PhotoListScreen(wallPaperViewModel = viewModel)
    }
}