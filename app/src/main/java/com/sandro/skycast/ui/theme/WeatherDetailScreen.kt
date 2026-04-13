package com.sandro.skycast.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.sandro.skycast.R
import com.sandro.skycast.data.ForecastItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherDetailScreen(viewModel: WeatherViewModel, navController: NavController, onOpenDashboard: () -> Unit) {
    val savedCities by viewModel.savedCities.collectAsState()
    val isPreviewCity = viewModel.selectedCityIndex == -1

    val pagerState = rememberPagerState(
        initialPage = if (isPreviewCity) 0 else (viewModel.selectedCityIndex + 1).coerceAtLeast(0),
        pageCount = { if (isPreviewCity) 1 else savedCities.size + 1 }
    )

    LaunchedEffect(pagerState.currentPage, savedCities, isPreviewCity) {
        if (isPreviewCity) {
            viewModel.selectedCity?.let { viewModel.fetchWeather(it) }
        } else {
            if (pagerState.currentPage == 0) {
                viewModel.fetchCurrentLocationWeather()
            } else {
                val cityName = savedCities.getOrNull(pagerState.currentPage - 1)?.name
                cityName?.let { viewModel.fetchWeather(it) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. PAGER CONTENT ---
        val flingBehavior = PagerDefaults.flingBehavior(state = pagerState, snapPositionalThreshold = 0.2f)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            key = { index -> if (index == 0 && !isPreviewCity) "MyLocation" else if (isPreviewCity) "Preview" else savedCities[index-1].name }
        ) { pageIndex ->
            val cityName = if (isPreviewCity) viewModel.selectedCity
            else if (pageIndex == 0) "My Location"
            else savedCities.getOrNull(pageIndex - 1)?.name

            if (cityName != null) CityWeatherView(cityName = cityName, viewModel = viewModel)
        }

        // --- 2. TOP BAR (ADD/CANCEL) ---
        // Visible ONLY during search preview. Forced to front.
        if (isPreviewCity) {
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).graphicsLayer { translationY = 0f }.zIndex(2f),
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onOpenDashboard) { Text("Cancel", color = Color.White, fontSize = 17.sp) }
                    Text(text = viewModel.selectedCity ?: "", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.selectedCity?.let { viewModel.addCityToDatabase(it) }; onOpenDashboard() }) {
                        Text("Add", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. THE GLASS FOOTER (MAP, DOTS, LIST) ---
        // Visible ONLY for saved cities
        if (!isPreviewCity) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp).height(64.dp).zIndex(2f)
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // LEFT: MAP
                    Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))) {
                        IconButton(onClick = {
                            val currentCity = if (pagerState.currentPage == 0) "My Location"
                            else savedCities[pagerState.currentPage - 1].name

                            // 2. Navigate to the map centered on that city
                            navController.navigate("map/$currentCity") }) {
                            Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    // CENTER: PILL
                    Surface(modifier = Modifier.width(140.dp).height(46.dp), shape = RoundedCornerShape(23.dp), color = Color.White.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))) {
                        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = if (pagerState.currentPage == 0) Color.White else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp).padding(end = 4.dp).graphicsLayer(rotationZ = -45f))
                            repeat(savedCities.size) { index ->
                                Box(modifier = Modifier.padding(horizontal = 3.dp).size(if (pagerState.currentPage == index + 1) 7.dp else 5.dp).background(color = if (pagerState.currentPage == index + 1) Color.White else Color.White.copy(alpha = 0.4f), shape = CircleShape))
                            }
                        }
                    }
                    // RIGHT: LIST
                    Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))) {
                        IconButton(onClick = onOpenDashboard) {
                            Icon(Icons.AutoMirrored.Filled.List, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityWeatherView(cityName: String, viewModel: WeatherViewModel) {
    val weather = viewModel.citiesWeather[cityName]
    val hourlyItems = viewModel.hourlyForecasts[cityName] ?: emptyList()
    val dailyItems = viewModel.dailyForecasts[cityName] ?: emptyList()
    val iconCode = weather?.weather?.firstOrNull()?.icon

    Box(modifier = Modifier.fillMaxSize()) {
        if (iconCode != null) {
            VideoWeatherBackground(iconCode = iconCode)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF1B1B1F)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.3f))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = cityName, fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Normal)
                    Text(
                        text = if (weather != null) viewModel.formatTemp(weather.main.temp) else "--",
                        fontSize = 96.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraLight
                    )
                    if (weather != null) {
                        Text(
                            text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "H:${viewModel.formatTemp(weather.main.tempMax)}  L:${viewModel.formatTemp(weather.main.tempMin)}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (weather != null) {
                item { Spacer(modifier = Modifier.height(40.dp)) }
                item { SummaryTextBox("It's ${weather.weather.firstOrNull()?.description} currently. Humidity is ${weather.main.humidity}%.") }
                item {
                    Surface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("HOURLY FORECAST", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                items(hourlyItems) { item -> HourlyCard(item, viewModel) }
                            }
                        }
                    }
                }
                item {
                    Surface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("5-DAY FORECAST", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                            dailyItems.forEach { item -> DailyRow(item, viewModel) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTextBox(text: String) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, color = Color.White, modifier = Modifier.padding(16.dp), fontSize = 15.sp, lineHeight = 20.sp)
    }
}

@Composable
fun AnimatedWeatherIcon(iconCode: String, modifier: Modifier = Modifier) {
    val resId = when {
        iconCode == "01d" -> R.raw.sunny
        iconCode == "01n" -> R.raw.night
        iconCode.startsWith("02") -> R.raw.partly_cloudy
        iconCode.startsWith("03") || iconCode.startsWith("04") -> R.raw.cloudy
        iconCode.startsWith("09") || iconCode.startsWith("10") -> R.raw.rainy
        iconCode.startsWith("11") -> R.raw.thunder
        iconCode.startsWith("13") -> R.raw.snow
        iconCode.startsWith("50") -> R.raw.mist
        else -> R.raw.sunny
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever, modifier = modifier)
}

@Composable
fun HourlyCard(item: ForecastItem, viewModel: WeatherViewModel) {
    val time = item.dtTxt.substring(11, 16)
    val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(time, color = Color.White, fontSize = 14.sp)
        AnimatedWeatherIcon(iconCode = iconCode, modifier = Modifier.size(45.dp))
        Text(viewModel.formatTemp(item.main.temp), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyRow(item: ForecastItem, viewModel: WeatherViewModel) {
    val date = java.util.Date(item.dt * 1000L)
    val dayName = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(date)
    val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = dayName, modifier = Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
        AnimatedWeatherIcon(iconCode = iconCode, modifier = Modifier.size(35.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = viewModel.formatTemp(item.main.temp), modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}