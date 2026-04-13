package com.sandro.skycast.ui.theme

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.sandro.skycast.R

@OptIn(UnstableApi::class)
@Composable
fun VideoWeatherBackground(iconCode: String) {
    val context = LocalContext.current

    // 1. IMPROVED MAPPING: Using all 8 of your video assets correctly
    val videoResId = remember(iconCode) {
        val prefix = iconCode.take(2)
        val isNight = iconCode.endsWith("n")

        when (prefix) {
            // --- CLEAR & NIGHT ---
            "01" -> if (isNight) R.raw.night_bg else R.raw.clearsky_bg

            // --- CLOUDS ---
            "02" -> if (isNight) R.raw.night_bg else R.raw.cloudysunny_bg // Partly cloudy
            "03", "04" -> if (isNight) R.raw.night_bg else R.raw.cloudy_bg // Overcast

            // --- RAIN ---
            "09", "10" -> R.raw.rainyday_bg

            // --- THUNDER ---
            "11" -> R.raw.thunderstorm_bg

            // --- SNOW ---
            "13" -> R.raw.snowy_bg

            // --- ATMOSPHERE ---
            "50" -> R.raw.windy_bg

            else -> R.raw.clearsky_bg
        }
    }

    // 2. Initialize the player once
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
        }
    }

    // 3. Update video without destroying the player
    LaunchedEffect(videoResId) {
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$videoResId")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // 4. Cleanup
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}