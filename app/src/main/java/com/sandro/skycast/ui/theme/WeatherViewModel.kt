package com.sandro.skycast.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandro.skycast.data.CityDao
import com.sandro.skycast.data.CityEntity
import com.sandro.skycast.data.ForecastItem
import com.sandro.skycast.data.RetrofitClient
import com.sandro.skycast.data.WeatherDto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.sandro.skycast.BuildConfig

class WeatherViewModel(private val cityDao: CityDao) : ViewModel() {

     val apiKey = BuildConfig.WEATHER_API_KEY

    val savedCities: StateFlow<List<CityEntity>> = cityDao.getAllCities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var isCelsius by mutableStateOf(true)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    val citiesWeather = mutableStateMapOf<String, WeatherDto?>()
    val hourlyForecasts = mutableStateMapOf<String, List<ForecastItem>>()
    val dailyForecasts = mutableStateMapOf<String, List<ForecastItem>>()

    var selectedCityIndex by mutableStateOf(0)
    var selectedCity by mutableStateOf<String?>(null)

    var currentLocationWeather by mutableStateOf<WeatherDto?>(null)
        private set

    // --- THE KEY FIX: AUTO-LOAD ON START ---
    init {
        viewModelScope.launch {
            // This observes the database. Every time the app opens or a city is added,
            // this block runs and fetches the weather automatically.
            savedCities.collect { cities ->
                if (cities.isNotEmpty()) {
                    // Only fetch weather for cities we don't already have in memory
                    val citiesToFetch = cities.map { it.name }.filter { !citiesWeather.containsKey(it) }
                    if (citiesToFetch.isNotEmpty()) {
                        refreshAllCities(citiesToFetch)
                    }
                }
            }
        }
        // Also fetch the local weather immediately
        fetchCurrentLocationWeather()
    }

    fun fetchCurrentLocationWeather() {
        // Tbilisi Default
        fetchWeatherByLocation(41.7151, 44.8271)
    }

    fun fetchWeatherByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentWeatherByLocation(
                    lat, lon, apiKey, "metric"
                )
                currentLocationWeather = response
                citiesWeather["My Location"] = response
                fetchForecastByLocation(lat, lon)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchForecastByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.get5DayForecastByLocation(lat, lon, apiKey)
                hourlyForecasts["My Location"] = response.list.take(12)
                dailyForecasts["My Location"] = response.list.filter { it.dtTxt.contains("12:00:00") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleUnits() {
        isCelsius = !isCelsius
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentWeather(city, apiKey, "metric")
                citiesWeather[city] = response
                fetchForecast(city)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchForecast(city: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.get5DayForecast(city, apiKey)
                hourlyForecasts[city] = response.list.take(12)
                dailyForecasts[city] = response.list.filter { it.dtTxt.contains("12:00:00") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshAllCities(cities: List<String>) {
        viewModelScope.launch {
            isRefreshing = true
            try {
                cities.forEach { city ->
                    val weather = RetrofitClient.apiService.getCurrentWeather(city, apiKey, "metric")
                    citiesWeather[city] = weather
                    // Fetch forecast in parallel to speed things up
                    launch { fetchForecast(city) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun addCityToDatabase(cityName: String) {
        viewModelScope.launch {
            if (!savedCities.value.any { it.name.equals(cityName, true) }) {
                cityDao.insertCity(CityEntity(cityName))
                fetchWeather(cityName)
            }
        }
    }

    fun removeCity(cityName: String) {
        viewModelScope.launch {
            cityDao.deleteCity(CityEntity(cityName))
            citiesWeather.remove(cityName)
            hourlyForecasts.remove(cityName)
            dailyForecasts.remove(cityName)
        }
    }

    fun formatTemp(temp: Double): String {
        return if (isCelsius) {
            "${temp.toInt()}°C"
        } else {
            val fahrenheit = (temp * 1.8) + 32
            "${fahrenheit.toInt()}°F"
        }
    }

    fun addCityAndNavigate(cityName: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            // 1. Save to Room
            if (!savedCities.value.any { it.name.equals(cityName, true) }) {
                cityDao.insertCity(CityEntity(cityName))
            }

            // 2. Fetch data immediately so it's ready when the screen slides in
            fetchWeather(cityName)

            // 3. Find where the city is in the list to get the right Page index
            // We add +1 because "My Location" is always index 0
            val currentList = cityDao.getAllCities().stateIn(viewModelScope).value
            val dbIndex = currentList.indexOfFirst { it.name.equals(cityName, true) }

            if (dbIndex != -1) {
                selectedCityIndex = dbIndex
                // Tell the UI to navigate to page (dbIndex + 1)
                onComplete(dbIndex + 1)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCityTime(timezoneOffset: Long): String {
        // We get the current time in UTC
        val utcTime = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)

        // We apply the city's offset (seconds from UTC)
        val cityOffset = java.time.ZoneOffset.ofTotalSeconds(timezoneOffset.toInt())
        val cityTime = utcTime.withOffsetSameInstant(cityOffset)

        // Format to 24-hour style (e.g., 22:45)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        return cityTime.format(formatter)
    }
}