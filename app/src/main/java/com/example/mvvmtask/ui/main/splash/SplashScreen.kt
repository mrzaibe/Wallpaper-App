package com.example.mvvmtask.ui.main.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mvvmtask.R
import com.example.mvvmtask.navigation.Screen
import kotlinx.coroutines.delay


@Composable
fun AnimatedSplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000
        ),
        label = "alpha animation"
    )

    /* val window = (LocalContext.current as Activity).window
     window.statusBarColor = White.toArgb()*/

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Splash(alpha = alphaAnim.value)
}

@Composable
fun Splash(alpha: Float) {
    Box(
        modifier = Modifier
            .background(White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha = alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp),
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = stringResource(R.string.logo_icon_description),
            )
            Text(
                textAlign = TextAlign.End,
                style = TextStyle(
                    color = Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight(600)
                ),
                text = stringResource(R.string.splash_title)
            )

        }

    }
}
