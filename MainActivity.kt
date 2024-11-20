package com.example.localtourismguide

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification.Builder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.localtourismguide.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherInfo: TextView
    private lateinit var locationName: TextView
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var humidityTextView: TextView
    private lateinit var visibilityTextView: TextView
    private lateinit var uvIndexTextView: TextView

    private lateinit var dbHelper: DatabaseHelper
    private var currentLocationName: String? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApiService = retrofit.create(WeatherApiService::class.java)
    private val apiKey = "b0c588486cc5499aab971154241911"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        mapView = findViewById(R.id.osmMapView)
        weatherInfo = findViewById(R.id.weatherInfo)
        locationName = findViewById(R.id.locationName)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)
        humidityTextView = findViewById(R.id.humidityTextView)
        visibilityTextView = findViewById(R.id.visibilityTextView)
        uvIndexTextView = findViewById(R.id.uvIndexTextView)

        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(10.0)
        mapView.controller.setCenter(GeoPoint(40.748817, -73.985428))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermission()

        weatherInfo.text = "Fetching weather..."
        locationName.text = "Fetching location..."

        // Initialize Toolbar
        val addFavoriteButton: FloatingActionButton = findViewById(R.id.addFavoriteButton)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val profileImage = findViewById<CircleImageView>(R.id.profileImage)
        profileImage.setOnClickListener {
            showLogoutDialog()
        }

        val username = getLoggedInUsername()

        Log.d("MainActivity", "Logged in username: $username")

        // Fetch and set profile image
        if (username != null) {
            val profilePictureBlob = fetchUserProfileImageFromDatabase(username)
            Log.d("MainActivity", "Profile image blob size: ${profilePictureBlob?.size}")

            profilePictureBlob?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                profileImage.setImageBitmap(bitmap)
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_user_profile) // Placeholder image
            Toast.makeText(
                this,
                "User not logged in. Please log in to view your profile.",
                Toast.LENGTH_SHORT
            ).show()
            redirectToLogin()
        }

        // Open FavoritePlacesActivity when addFavoriteButton is clicked
        addFavoriteButton.setOnClickListener {
            val intent = Intent(this, FavoritePlacesActivity::class.java)
            intent.putExtra("currentLocationName", currentLocationName) // Pass current location name if needed
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.forecastRecyclerView)
        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        recyclerView.startAnimation(slideUpAnimation)
        val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)

        fabMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_fab, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_alerts -> {
                        showWeatherAlertDialog()
                        true
                    }
                    R.id.menu_feedback -> {
                        // Pass the current location name and logged-in username to FeedbackActivity
                        val locationName = currentLocationName ?: "Unknown Location"
                        val username = getLoggedInUsername() ?: "Guest"
                        val feedbackIntent = Intent(this, FeedbackActivity::class.java)
                        feedbackIntent.putExtra("locationName", locationName)
                        feedbackIntent.putExtra("username", username)
                        startActivity(feedbackIntent)
                        true
                    }


                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    private fun showWeatherAlertDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Weather Alerts")

        val locationName = currentLocationName ?: "Unknown Location"
        val latitude = 12.9716 // Replace with dynamic latitude
        val longitude = 77.5946 // Replace with dynamic longitude

        // Launch a coroutine on the Main thread
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch alerts using the suspend function
                val weatherAlerts = fetchWeatherAlerts(latitude, longitude)

                val message = if (weatherAlerts.isNotEmpty()) {
                    weatherAlerts.joinToString("\n")
                } else {
                    "No Alerts"
                }

                // Update dialog with the alerts or no-alerts message
                alertDialog.setMessage("Location: $locationName\n\n$message")
                alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                alertDialog.create().show()
            } catch (e: Exception) {
                // Handle any errors during the fetch
                alertDialog.setMessage("Failed to fetch weather alerts.\nPlease try again.")
                alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                alertDialog.create().show()
                Log.e("WeatherAlertDialog", "Error fetching weather alerts", e)
            }
        }
    }




    // Function to get the logged-in username from shared preferences
    private fun getLoggedInUsername(): String? {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPref.getString("loggedInUsername", null)
    }



    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Do you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Perform logout action
    private fun performLogout() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    // Redirect to login page
    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun fetchUserProfileImageFromDatabase(username: String): ByteArray? {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        var imageBlob: ByteArray? = null

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_IMAGE),
            "${DatabaseHelper.COLUMN_USERNAME} = ?",
            arrayOf(username),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE))
        }
        cursor.close()
        return imageBlob
    }



    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val call = weatherApiService.getCurrentWeather(apiKey, location)

        call.enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val temp = it.current.temp_c
                        val condition = it.current.condition.text
                        val locationName = it.location.name
                        currentLocationName = it.location.name
                        Log.d("WeatherAPI", "Humidity: ${it.current.humidity}, Visibility: ${it.current.vis_km}, UV: ${it.current.uv}")


                        weatherInfo.text = "$condition, $temp°C"
                        this@MainActivity.locationName.text = locationName


                        humidityTextView.text = "Humidity: ${it.current.humidity}%"
                        visibilityTextView.text = "Visibility: ${it.current.vis_km} km"
                        uvIndexTextView.text = "UV Index: ${it.current.uv}"

                        runOnUiThread {
                            humidityTextView.text = "Humidity: ${it.current.humidity}%"
                            visibilityTextView.text = "Visibility: ${it.current.vis_km} km"
                            uvIndexTextView.text = "UV Index: ${it.current.uv}"
                        }
                        fetchWeatherForecast(location)
                    }
                } else {
                    weatherInfo.text = "Failed to fetch weather"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherInfo.text = "Error: ${t.message}"
                Log.e("WeatherAPI", "Failed to fetch weather data", t)
            }

        })
    }
    private fun fetchWeatherForecast(location: String) {
        val forecastCall = weatherApiService.getWeatherForecast(apiKey, location)
        forecastCall.enqueue(object : retrofit2.Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: retrofit2.Response<ForecastResponse>) {
                if (response.isSuccessful) {
                    val forecastResponse = response.body()
                    forecastResponse?.let {
                        val forecastDays = it.forecast.forecastday
                        forecastRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        forecastRecyclerView.adapter = ForecastAdapter(forecastDays)
                    }
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Log.e("WeatherAPI", "Failed to fetch weather forecast", t)
            }

        })
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                val geoPoint = GeoPoint(latitude, longitude)

                mapView.controller.setCenter(geoPoint)
                mapView.controller.animateTo(geoPoint)

                val userMarker = Marker(mapView)
                userMarker.position = geoPoint
                userMarker.title = "Your Location"
                userMarker.setIcon(getDrawable(R.drawable.ic_users_location))
                mapView.overlays.add(userMarker)
                mapView.invalidate()



                fetchWeatherData(latitude, longitude)
                scheduleWeatherAlertWorker(latitude, longitude)

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val alerts = fetchWeatherAlerts(latitude, longitude)
                        if (alerts.isNotEmpty()) {
                            Log.d("WeatherAlerts", "Alerts: ${alerts.joinToString()}")
                        } else {
                            Log.d("WeatherAlerts", "No alerts found")
                        }
                    } catch (e: Exception) {
                        Log.e("WeatherAlerts", "Error fetching weather alerts", e)
                    }
                }
            }
        }
    }



    private suspend fun fetchWeatherAlerts(latitude: Double, longitude: Double): List<String> {
        val location = "$latitude,$longitude"
        return suspendCoroutine { continuation ->
            val call = weatherApiService.getWeatherAlerts(apiKey, location)

            call.enqueue(object : retrofit2.Callback<WeatherAlertResponse> {
                override fun onResponse(call: Call<WeatherAlertResponse>, response: retrofit2.Response<WeatherAlertResponse>) {
                    if (response.isSuccessful) {
                        val alertResponse = response.body()
                        alertResponse?.alerts?.alert?.let { alerts ->
                            if (alerts.isNotEmpty()) {
                                continuation.resume(alerts.map { it.description }) // Resume with list of alert descriptions
                                showWeatherAlertNotification(alerts[0]) // Show the first alert as a notification
                            } else {
                                continuation.resume(emptyList()) // No alerts
                            }
                        } ?: continuation.resume(emptyList()) // No alerts in response
                    } else {
                        Log.e("WeatherAPI", "No alerts found or request failed: ${response.message()}")
                        continuation.resume(emptyList())
                    }
                }

                override fun onFailure(call: Call<WeatherAlertResponse>, t: Throwable) {
                    Log.e("WeatherAPI", "Failed to fetch weather alerts", t)
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    @SuppressLint("NewApi")
    fun showWeatherAlertNotification(alert: Alert) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Create notification channel for Android 8.0 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "WeatherAlertChannel",
                "Weather Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = Builder(this, "WeatherAlertChannel")
            .setContentTitle("Weather Alert: ${alert.headline}")
            .setContentText(alert.description)
            .setSmallIcon(R.drawable.ic_weather_alert)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun scheduleWeatherAlertWorker(latitude: Double, longitude: Double) {
        val workManager = androidx.work.WorkManager.getInstance(this)

        val inputData = androidx.work.Data.Builder()
            .putDouble("latitude", latitude)
            .putDouble("longitude", longitude)
            .build()

        val workRequest = androidx.work.PeriodicWorkRequestBuilder<WeatherAlertWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).setInputData(inputData).build()

        workManager.enqueue(workRequest)
    }


}