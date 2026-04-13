package com.sandro.skycast.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.maps.android.compose.*
import com.sandro.skycast.BuildConfig
import java.net.URL

@Composable
fun PrecipitationMapScreen(cityName: String, viewModel: WeatherViewModel, onBack: () -> Unit) {
    val apiKey = BuildConfig.WEATHER_API_KEY

    val weatherData = viewModel.citiesWeather[cityName]
    val location = if (weatherData != null) {
        LatLng(weatherData.coord.lat, weatherData.coord.lon)
    } else LatLng(41.7151, 44.8271)

    // Zoom 5.0f is the "Sweet Spot" for seeing large rain clouds clearly
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 5.0f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // TERRAIN (browns/tans) provides better contrast for BLUE rain than HYBRID (green/blue)
            properties = MapProperties(mapType = MapType.TERRAIN)
        ) {
            val radarTileProvider = remember {
                object : UrlTileProvider(256, 256) {
                    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                        val s = "https://tile.openweathermap.org/map/precipitation_new/$zoom/$x/$y.png?appid=$apiKey"
                        return try { URL(s) } catch (e: Exception) { null }
                    }
                }
            }

            // --- THE TRICK: STACKING LAYERS ---
            // We call the overlay twice. This makes "light rain" look 2x thicker.
            repeat(2) {
                TileOverlay(
                    tileProvider = radarTileProvider,
                    transparency = 0f, // 0 = fully opaque (solid colors)
                    zIndex = 100f      // Ensures rain is ALWAYS on top of roads
                )
            }
        }

        // --- FLOATING CLOSE BUTTON ---
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, "Close", tint = Color.White)
        }
    }
}