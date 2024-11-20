package com.example.localtourismguide

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.localtourismguide.models.WeatherResponse
import com.example.localtourismguide.models.ForecastResponse
import com.example.localtourismguide.models.WeatherAlertResponse


interface WeatherApiService {

    @GET("v1/current.json")
    fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String
    ): Call<WeatherResponse>

    @GET("v1/forecast.json")
    fun getWeatherForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7
    ): Call<ForecastResponse>

    @GET("v1/alerts.json")
    fun getWeatherAlerts(
        @Query("key") apiKey: String,
        @Query("q") location: String
    ): Call<WeatherAlertResponse>


}

