package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.*

@Composable
fun CyberBackground(
    wallpaperName: String = "Cyber Gold",
    dimLevel: Float = 0.0f,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val isCustomGalleryWallpaper = wallpaperName.startsWith("file:") ||
            wallpaperName.startsWith("content:") ||
            wallpaperName.startsWith("/")

    val wallpaperDrawableRes = when {
        wallpaperName.equals("Pro Studio", ignoreCase = true) -> R.drawable.wallpaper_pro_studio_1786159481584
        wallpaperName.equals("Neon Glass", ignoreCase = true) -> R.drawable.wallpaper_neon_glass_1786159495602
        wallpaperName.equals("Gold Matrix", ignoreCase = true) -> R.drawable.wallpaper_gold_matrix_1786159510045
        wallpaperName.equals("Royal Velvet", ignoreCase = true) -> R.drawable.wallpaper_royal_velvet_1786159525036
        wallpaperName.equals("Cyber Circuit", ignoreCase = true) -> R.drawable.wallpaper_cyber_circuit_1786159540301
        wallpaperName.equals("Dark Obsidian", ignoreCase = true) -> R.drawable.wallpaper_dark_obsidian_1786159554951
        else -> R.drawable.img_cyber_bg_1785167084570 // "Cyber Gold"
    }

    // Direct 0-100% dim level response: 0.0f = full 100% bright image, 1.0f = fully dimmed pitch dark
    val imageAlpha = (1.0f - dimLevel).coerceIn(0.0f, 1.0f)
    val overlayDarkness = (dimLevel * 0.95f).coerceIn(0.0f, 0.98f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F)) // Pitch dark base color
    ) {
        // High Quality Crisp Background Image Rendering
        if (isCustomGalleryWallpaper) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaperName)
                    .crossfade(true)
                    .build(),
                contentDescription = "Custom HD Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha)
            )
        } else {
            Image(
                painter = painterResource(id = wallpaperDrawableRes),
                contentDescription = "Cyber HD Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha)
            )
        }

        // Adjustable Dark Overlay Box based on dimLevel slider
        if (dimLevel > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayDarkness))
            )
        }

        content()
    }
}
