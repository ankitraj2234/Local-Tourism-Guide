package com.example.localtourismguide

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.tools.build.jetifier.core.utils.Log
import com.example.localtourismguide.adapters.EventsAdapter
import com.example.localtourismguide.adapters.HotelsAdapter
import com.example.localtourismguide.adapters.TouristPlacesAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.localtourismguide.models.*

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var touristPlacesList: RecyclerView
    private lateinit var hotelsList: RecyclerView
    private lateinit var eventsList: RecyclerView
    private lateinit var weatherInfo: TextView
    private lateinit var locationName: TextView
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var humidityTextView: TextView
    private lateinit var visibilityTextView: TextView
    private lateinit var uvIndexTextView: TextView

    // Retrofit setup
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApiService = retrofit.create(WeatherApiService::class.java)
    private val apiKey = "435bb23ccee240c9b04101316242809" // Your WeatherAPI key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize OSMDroid Configuration
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        // Initialize views
        mapView = findViewById(R.id.osmMapView)
        weatherInfo = findViewById(R.id.weatherInfo)
        locationName = findViewById(R.id.locationName)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)
        humidityTextView = findViewById(R.id.humidityTextView)
        visibilityTextView = findViewById(R.id.visibilityTextView)
        uvIndexTextView = findViewById(R.id.uvIndexTextView)

        // Setup map view
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(9.0)
        mapView.controller.setCenter(GeoPoint(40.748817, -73.985428)) // Default to New York

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request Location Permission
        requestLocationPermission()

        // Initialize RecyclerViews for Tourist Places, Hotels, and Events
        initRecyclerViews()

        // Set default weather and location info
        weatherInfo.text = "Fetching weather..."
        locationName.text = "Fetching location..."
    }

    private fun initRecyclerViews() {
        touristPlacesList = findViewById(R.id.touristPlacesList)
        touristPlacesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        touristPlacesList.adapter = TouristPlacesAdapter(emptyList()) // Initialize with empty list

        hotelsList = findViewById(R.id.hotelsList)
        hotelsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hotelsList.adapter = HotelsAdapter(emptyList()) // Initialize with empty list

        eventsList = findViewById(R.id.eventsList)
        eventsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventsList.adapter = EventsAdapter(mutableListOf()) // Initialize with an empty mutable list
    }

    private fun fetchTouristPlacesData() {
        val touristPlaces = listOf(
            Venue("1", "Eiffel Tower", Location("Champ de Mars, Paris", 48.8584, 2.2945), listOf(Category("Landmark"))),
            Venue("2", "Louvre Museum", Location("Rue de Rivoli, Paris", 48.8606, 2.3376), listOf(Category("Museum"))),
            Venue("3", "Notre-Dame", Location("Paris", 48.852968, 2.349902), listOf(Category("Cathedral")))
        )
        (touristPlacesList.adapter as TouristPlacesAdapter).updateData(touristPlaces)
    }

    private fun fetchHotelsData() {
        val hotels = listOf(
            Venue("1", "Hotel A", Location("Address A", 48.0, 2.0), listOf(Category("Hotel"))),
            Venue("2", "Hotel B", Location("Address B", 49.0, 3.0), listOf(Category("Hotel"))),
            Venue("3", "Hotel C", Location("Address C", 50.0, 4.0), listOf(Category("Hotel")))
        )
        (hotelsList.adapter as HotelsAdapter).updateData(hotels)
    }

    private fun fetchEventsData() {
        val events = listOf(
            Venue("1", "Concert A", Location("Location A", 48.0, 2.0), listOf(Category("Concert"))),
            Venue("2", "Festival B", Location("Location B", 49.0, 3.0), listOf(Category("Festival"))),
            Venue("3", "Exhibition C", Location("Location C", 50.0, 4.0), listOf(Category("Exhibition")))
        )
        (eventsList.adapter as EventsAdapter).updateData(events)
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"  // Format as "latitude,longitude"
        val call = weatherApiService.getCurrentWeather(apiKey, location)

        call.enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val temp = it.current.temp_c
                        val condition = it.current.condition.text
                        val locationName = it.location.name

                        Log.d("WeatherAPI", "Humidity: ${it.current.humidity}, Visibility: ${it.current.vis_km}, UV: ${it.current.uv}")


                        // Update the UI with fetched weather
                        weatherInfo.text = "$condition, $temp°C"
                        this@MainActivity.locationName.text = locationName

                        // Update humidity, visibility, and UV index
                        humidityTextView.text = "Humidity: ${it.current.humidity}%"
                        visibilityTextView.text = "Visibility: ${it.current.vis_km} km"
                        uvIndexTextView.text = "UV Index: ${it.current.uv}"

                        runOnUiThread {
                            humidityTextView.text = "Humidity: ${it.current.humidity}%"
                            visibilityTextView.text = "Visibility: ${it.current.vis_km} km"
                            uvIndexTextView.text = "UV Index: ${it.current.uv}"
                        }
                        // Fetch 7-day weather forecast
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
                // Optionally show some error in UI if needed
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    Log.d("LocationUpdate", "New Location: Latitude: $latitude, Longitude: $longitude")

                    // Update map position
                    val userLocation = GeoPoint(latitude, longitude)
                    mapView.controller.setCenter(userLocation)

                    // Create a marker for the user's live location
                    val userMarker = Marker(mapView)
                    userMarker.position = userLocation
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    userMarker.title = "You are here"

                    // Clear existing overlays and add the new marker
                    mapView.overlays.clear()
                    mapView.overlays.add(userMarker)
                    mapView.invalidate()

                    // Fetch weather and other data
                    fetchWeatherData(latitude, longitude)
                    fetchTouristPlacesData()
                    fetchHotelsData()
                    fetchEventsData()
                }
            }
        }
    }
}
