package com.example.localtourismguide.models


data class ForecastResponse(
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val avgtemp_c: Double,
    val condition: Condition
)
data class Condition(
    val text: String  // Ensure this field is defined
)