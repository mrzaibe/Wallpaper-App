package com.example.mvvmtask.ui.main.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mvvmtask.utils.CameraPreviewScreen

@Composable
fun CameraPhotos() {
    var permissionGranted by remember { mutableStateOf(false) }
    val permissionRequested by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    // Request the camera permissions
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions.all { it.value }
        if (!permissionGranted) {
            showRationale = cameraPermissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
            }
            permanentlyDenied = cameraPermissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
            }
        }
    }

    // Trigger the permission request once
    LaunchedEffect(Unit) {
        val allPermissionsGranted = cameraPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            permissionGranted = true
        } else {
            launcher.launch(cameraPermissions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // If permission is granted, show the camera preview
            permissionGranted -> {
                CameraPreviewScreen()
            }

            // If permission is not granted but rationale should be shown
            showRationale -> {
                Text("Camera permission is needed. Go to settings to enable.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            }

            // If permission is requested but not yet granted
            permissionRequested && !permissionGranted -> {
                Text("Camera permission is needed to access Camera features.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    launcher.launch(cameraPermissions)
                }) {
                    Text("Request Permission")
                }
            }
        }
    }
}