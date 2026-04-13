package com.sandro.skycast.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("name") val cityName: String,
    @SerialName("coord") val coord: CoordDto,
    @SerialName("main") val main: MainDto,
    @SerialName("weather") val weather: List<WeatherDescriptionDto>,
    @SerialName("timezone") val timezone: Long,
    @SerialName("dt") val dt: Long,
    @SerialName("sys") val sys: SysDto
)

@Serializable
data class CoordDto(
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double
)

@Serializable
data class SysDto(
    @SerialName("sunrise") val sunrise: Long,
    @SerialName("sunset") val sunset: Long
)

@Serializable
data class MainDto(
    @SerialName("temp") val temp: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    @SerialName("humidity") val humidity: Int
)

@Serializable
data class WeatherDescriptionDto(
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)

@Serializable
data class ForecastResponse(
    val list: List<ForecastItem>
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    @SerialName("dt_txt")
    val dtTxt: String
)
