package com.example.localtourismguide

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class FavoritePlacesActivity : AppCompatActivity() {

    private lateinit var locationNameTextView: TextView
    private lateinit var uploadImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritePlacesAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var selectedImage: ByteArray? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_places)

        dbHelper = DatabaseHelper(this)

        locationNameTextView = findViewById(R.id.locationNameTextView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        addButton = findViewById(R.id.addButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Retrieve current location name from the Intent
        val currentLocationName = intent.getStringExtra("currentLocationName")
        locationNameTextView.text = currentLocationName ?: "Unknown Location"

        // Get the logged-in username
        val loggedInUsername = getLoggedInUsername() ?: "default_username"

        // Set up the RecyclerView with the user's favorite places
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavoritePlacesAdapter(
            dbHelper.getAllFavoritePlaces(loggedInUsername),
            onItemClick = { favoritePlace ->
                // Navigate to MemoriesActivity on item click
                openMemoriesActivity(loggedInUsername, favoritePlace.locationName)
            },
            onDeleteClick = { favoritePlace ->
                // Confirm delete on long click
                confirmDeletePlace(favoritePlace, loggedInUsername)
            }
        )
        recyclerView.adapter = adapter

        // Image upload button listener
        uploadImageButton.setOnClickListener {
            selectImage()
        }

        // Add button listener to add favorite place to the database
        addButton.setOnClickListener {
            val locationName = locationNameTextView.text.toString()
            selectedImage?.let { imageBytes ->
                if (!dbHelper.isFavoritePlaceExists(loggedInUsername, locationName)) {
                    dbHelper.addFavoritePlace(loggedInUsername, locationName, imageBytes)
                    refreshFavoritePlaces(loggedInUsername)
                } else {
                    Toast.makeText(this, "Location Already Added", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to open MemoriesActivity
    private fun openMemoriesActivity(username: String, locationName: String) {
        val intent = Intent(this, MemoriesActivity::class.java).apply {
            putExtra("username", username)
            putExtra("locationName", locationName)
        }
        startActivity(intent)
    }

    // Function to select an image from the gallery
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                handleImageUri(uri)
            }
        }
    }

    // Handle the selected image and compress it
    private fun handleImageUri(uri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        selectedImage = compressImage(bitmap)
        selectedImageView.setImageBitmap(bitmap)
        selectedImageView.visibility = ImageView.VISIBLE // Ensure the image is visible
    }

    // Compress the image
    private fun compressImage(bitmap: Bitmap): ByteArray {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)
        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        resizedBitmap.recycle()
        return stream.toByteArray()
    }

    // Refresh the RecyclerView data for the specific user
    private fun refreshFavoritePlaces(username: String = getLoggedInUsername() ?: "default_username") {
        adapter.updateData(dbHelper.getAllFavoritePlaces(username))
        recyclerView.scrollToPosition(0)
    }

    // Function to get the logged-in username from shared preferences
    private fun getLoggedInUsername(): String? {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPref.getString("loggedInUsername", null)
    }

    // Override onBackPressed to return to MainActivity when back button is pressed
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

    private fun confirmDeletePlace(favoritePlace: FavoritePlace, username: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Favorite Place")
            .setMessage("Are you sure you want to delete this place?")
            .setPositiveButton("Yes") { _, _ ->
                dbHelper.deleteFavoritePlace(favoritePlace.id)  // Assume id is a unique identifier for favorite places
                refreshFavoritePlaces(username)  // Pass the username here
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
