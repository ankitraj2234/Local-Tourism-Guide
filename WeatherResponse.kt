package com.example.localtourismguide.models

data class WeatherResponse(
    val location: Location,
    val current: CurrentWeather
)

data class Location(
    val name: String
)

data class CurrentWeather(
    val temp_c: Double,
    val condition: Condition,
    val humidity: Int,
    val vis_km: Double,
    val uv: Double
)




