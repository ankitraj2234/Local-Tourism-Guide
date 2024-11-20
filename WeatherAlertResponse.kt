package com.example.localtourismguide.models

data class WeatherAlertResponse(
    val alerts: Alerts?
)

data class Alerts(
    val alert: List<Alert>?
)

data class Alert(
    val headline: String,
    val severity: String,
    val description: String,
    val effective: String,
    val expires: String
)
