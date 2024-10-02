package com.example.localtourismguide

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val categories: List<Category>
)

data class Location(
    val address: String,
    val lat: Double,
    val lng: Double
)

data class Category(
    val name: String
)