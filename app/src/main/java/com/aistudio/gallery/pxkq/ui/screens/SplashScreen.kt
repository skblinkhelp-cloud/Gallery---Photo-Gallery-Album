package com.aistudio.gallery.pxkq.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aistudio.gallery.pxkq.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val alpha = remember { Animatable(0f) }
  val scale = remember { Animatable(0.82f) }
  val glowAlpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    launch {
      alpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
      )
    }
    launch {
      scale.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
      )
    }
    launch {
      glowAlpha.animateTo(
        targetValue = 0.35f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
      )
    }

    delay(1300)
    onSplashFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Subtle Glowing Aura Behind Logo
    Box(
      modifier = Modifier
        .size(220.dp)
        .scale(scale.value * 1.15f)
        .alpha(glowAlpha.value)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF1A73E8).copy(alpha = 0.4f),
              Color(0xFF8AB4F8).copy(alpha = 0.15f),
              Color.Transparent
            )
          )
        )
    )

    // Centered Logo
    Image(
      painter = painterResource(id = R.drawable.app_logo),
      contentDescription = "Gallery Logo",
      modifier = Modifier
        .size(160.dp)
        .scale(scale.value)
        .alpha(alpha.value)
    )
  }
}
