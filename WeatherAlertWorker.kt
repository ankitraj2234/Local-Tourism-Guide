package com.example.localtourismguide

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.localtourismguide.models.*

class WeatherAlertWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val apiKey = "083846d6ba804161a6b182033241710"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApiService = retrofit.create(WeatherApiService::class.java)

    override fun doWork(): Result {
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val location = "$latitude,$longitude"

        val call = weatherApiService.getWeatherAlerts(apiKey, location)
        val response = call.execute()

        if (response.isSuccessful) {
            val alertResponse = response.body()
            alertResponse?.alerts?.alert?.let { alerts ->
                if (alerts.isNotEmpty()) {
                    // Trigger notification for the first alert
                    val context = applicationContext
                    val mainActivity = MainActivity() // Or use notification utility
                    mainActivity.showWeatherAlertNotification(alerts[0])
                }
            }
        }
        return Result.success()
    }
}
