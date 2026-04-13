package com.sandro.skycast.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: WeatherViewModel) {
    val savedCities by viewModel.savedCities.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val isSystemDark = isSystemInDarkTheme()

    val citySuggestions = remember(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            listOf("London", "New York", "Tokyo", "Paris", "Berlin", "Tbilisi", "Batumi", "Rome", "Madrid", "Zestaponi", "Kutaisi")
                .filter { it.contains(searchQuery, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refreshAllCities(savedCities.map { it.name }) }
    )

    // This runs as soon as the Dashboard opens
    LaunchedEffect(savedCities) {
        if (savedCities.isNotEmpty()) {
            savedCities.forEach { city ->
                // Only fetch if we don't already have the weather
                // OR if you want it to be fresh every time the screen opens
                if (viewModel.citiesWeather[city.name] == null) {
                    viewModel.fetchWeather(city.name)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemDark) Color(0xFF131313) else Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "SkyCast", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (isSystemDark) Color.White else Color.Black)

            // Unit Switcher
            Box(
                modifier = Modifier
                    .width(90.dp).height(40.dp)
                    .background(if (isSystemDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable { viewModel.toggleUnits() }.padding(4.dp)
            ) {
                val xOffset by animateDpAsState(if (viewModel.isCelsius) 0.dp else 40.dp, label = "")
                Surface(
                    modifier = Modifier.offset(x = xOffset).fillMaxHeight().width(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSystemDark) Color(0xFF2C2C2C) else Color.White,
                    shadowElevation = 2.dp
                ) {}
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("°C", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSystemDark) Color.White else Color.Black)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("°F", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSystemDark) Color.White else Color.Black)
                    }
                }
            }
        }

        // --- SEARCH BAR ---
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { query ->
                active = false
                searchQuery = ""
                viewModel.fetchWeather(query)
                viewModel.selectedCityIndex = -1 // Trigger Preview
                viewModel.selectedCity = query
                navController.navigate("details")
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search city...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = SearchBarDefaults.colors(containerColor = if (isSystemDark) Color(0xFF242424) else Color(0xFFF5F5F5))
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                items(citySuggestions) { city ->
                    ListItem(
                        headlineContent = { Text(city, color = if (isSystemDark) Color.White else Color.Black) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.addCityToDatabase(city); searchQuery = ""; active = false }) {
                                Icon(Icons.Default.Add, null, tint = Color(0xFF4A90E2))
                            }
                        },
                        modifier = Modifier.clickable {
                            active = false
                            searchQuery = ""
                            viewModel.fetchWeather(city)
                            viewModel.selectedCityIndex = -1
                            viewModel.selectedCity = city
                            navController.navigate("details")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CITY LIST ---
        Box(modifier = Modifier.weight(1f).pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(items = savedCities, key = { it.name }) { cityEntity ->
                    val city = cityEntity.name
                    val index = savedCities.indexOfFirst { it.name == city }
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { viewModel.removeCity(city); true } else false }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = { SwipeBackground(dismissState) },
                        enableDismissFromStartToEnd = false
                    ) {
                        CityCard(cityName = city, viewModel = viewModel) {
                            viewModel.selectedCityIndex = index
                            viewModel.fetchWeather(city)
                            navController.navigate("details")
                        }
                    }
                }
            }
            PullRefreshIndicator(viewModel.isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red.copy(alpha = 0.8f) else Color.Transparent
    Box(modifier = Modifier.fillMaxSize().background(color, RoundedCornerShape(24.dp)).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
        Icon(Icons.Default.Delete, null, tint = Color.White)
    }
}

@Composable
fun CityCard(cityName: String, viewModel: WeatherViewModel, onClick: () -> Unit) {
    val weather = viewModel.citiesWeather[cityName]
    val isNight = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth().height(115.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isNight) Color(0xFF1E293B) else Color(0xFF4A90E2)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cityName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                val displayTime = if (weather != null) viewModel.getCityTime(weather.timezone) else ""
                if (displayTime.isNotEmpty()) {
                    Text(
                        text = displayTime,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = weather?.weather?.firstOrNull()?.description?.uppercase() ?: "UPDATING...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            // The temp will flip from "--" to the real degrees automatically
            Text(
                text = if (weather != null) viewModel.formatTemp(weather.main.temp) else "--",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraLight
            )
        }
    }
}