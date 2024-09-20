package com.example.mvvmtask.ui.main.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mvvmtask.ui.viewmodel.SavedPhotosViewModel
import com.example.mvvmtask.utils.CameraPreviewScreen
import com.example.mvvmtask.utils.requestForCameraPermission

@Composable
fun CameraPhotos(savedPhotosViewModel: SavedPhotosViewModel) {
    var permissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        }
    }

    if (permissionGranted) {
        CameraPreviewScreen(savedPhotosViewModel)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera permission is needed to access Camera features.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                context.requestForCameraPermission(cameraPermissions) { isGranted ->
                    permissionGranted = isGranted
                }
            }) {
                Text("Request Permission")
            }
        }
    }
}