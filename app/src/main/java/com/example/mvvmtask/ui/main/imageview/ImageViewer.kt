package com.example.mvvmtask.ui.main.imageview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mvvmtask.data.model.apimodel.WallPaperPhotos

@Composable
fun ImageViewer(wallPaperPhotos: WallPaperPhotos?, onClickDownloadBtn: () -> Unit) {

    val context = LocalContext.current
    Column {
        AsyncImage(
            model = wallPaperPhotos?.src?.large,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.9f)
        )

        Button(
            onClick = {
                onClickDownloadBtn()
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 10.dp)
                .background(
                    Color(0xFF000000),
                    shape = RoundedCornerShape(12.dp)
                ),
        ) {
            Text(
                color = Color.White,
                text = "Download",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

}
