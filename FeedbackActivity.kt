package com.example.localtourismguide

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class FeedbackActivity : AppCompatActivity() {

    // Declare views and variables
    private lateinit var textField: EditText
    private lateinit var uploadImageButton: Button
    private lateinit var addFeedbackButton: Button
    private lateinit var myFeedbackRecyclerView: RecyclerView
    private lateinit var otherFeedbackRecyclerView: RecyclerView
    private lateinit var selectedImageView: ImageView
    private var selectedfeedImage: ByteArray? = null

    private lateinit var myFeedbackAdapter: FeedbackAdapter
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var username: String
    private lateinit var locationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Initialize views
        textField = findViewById(R.id.feedback_text)
        uploadImageButton = findViewById(R.id.upload_image_button)
        addFeedbackButton = findViewById(R.id.add_feedback_button)
        myFeedbackRecyclerView = findViewById(R.id.my_feedback_recycler_view)
        otherFeedbackRecyclerView = findViewById(R.id.other_feedback_recycler_view)
        selectedImageView = findViewById(R.id.selected_image_view)
        selectedImageView.visibility = View.GONE // Initially hide the ImageView


        val clearImageButton: Button = findViewById(R.id.clear_image_button)
        clearImageButton.setOnClickListener {
            selectedImageView.setImageResource(R.drawable.ic_placeholder_image)
            selectedImageView.visibility = View.GONE
            selectedfeedImage = null
        }

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Get passed data
        username = intent.getStringExtra("username") ?: "Guest"
        locationName = intent.getStringExtra("locationName") ?: "Unknown Location"

        // Set up RecyclerViews
        myFeedbackRecyclerView.layoutManager = LinearLayoutManager(this)
        otherFeedbackRecyclerView.layoutManager = LinearLayoutManager(this)

        myFeedbackAdapter = FeedbackAdapter(
            mutableListOf(),
            isMyFeedback = true,
            databaseHelper = databaseHelper
        )
        feedbackAdapter = FeedbackAdapter(
            mutableListOf(),
            isMyFeedback = false,
            databaseHelper = databaseHelper
        ) { locationName, averageRating ->
            // Update the UI element for average rating
            val averageRatingTextView: TextView = findViewById(R.id.average_rating_text_view)
            averageRatingTextView.text = String.format("Average Rating: %.1f", averageRating)

        }


        myFeedbackRecyclerView.adapter = myFeedbackAdapter
        otherFeedbackRecyclerView.adapter = feedbackAdapter


        // Load existing feedback
        loadFeedback()

        // Image selection logic
        val imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    val inputStream = uri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    selectedImageView.setImageBitmap(bitmap)
                    selectedImageView.visibility = View.VISIBLE // Make the image view visible

                    // Compress image
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                    selectedfeedImage = outputStream.toByteArray()
                }
            }

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        // Add feedback logic
        addFeedbackButton.setOnClickListener {
            val feedbackText = textField.text.toString().trim()
            if (feedbackText.isEmpty() && selectedfeedImage == null) {
                Toast.makeText(this, "Please provide text or image for feedback.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val feedbackId = textField.tag as? Int
            if (feedbackId != null) {
                // Update existing feedback
                val success = databaseHelper.updateFeedback(feedbackId, feedbackText, selectedfeedImage)
                if (success) {
                    Toast.makeText(this, "Feedback updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update feedback", Toast.LENGTH_SHORT).show()
                }
                textField.tag = null // Clear the tag
            } else {
                // Add new feedback
                val feedback = Feedback(
                    username = username,
                    locationName = locationName,
                    text = feedbackText,
                    image = selectedfeedImage
                )
                val success = databaseHelper.addFeedback(feedback)
                if (success) {
                    myFeedbackAdapter.addFeedback(feedback)
                    Toast.makeText(this, "Feedback added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add feedback", Toast.LENGTH_SHORT).show()
                }
            }

            // Reset input fields
            textField.text.clear()
            selectedImageView.setImageResource(R.drawable.ic_placeholder_image)
            selectedImageView.visibility = View.GONE
            selectedfeedImage = null
        }


    }

    fun loadFeedback() {
        val myFeedbacks = databaseHelper.getFeedbacks(username, locationName)
        val otherFeedbacks = databaseHelper.getFeedbacksForLocationWithAverage(locationName)

        val filteredOtherFeedbacks = otherFeedbacks.filter { it.username != username }

        // Fetch average ratings and update adapters
        val averageRating = databaseHelper.getAverageRating(locationName)
        myFeedbacks.forEach { it.averageRating = averageRating }
        filteredOtherFeedbacks.forEach { it.averageRating = averageRating }

        myFeedbackAdapter.setFeedbackList(myFeedbacks)
        feedbackAdapter.setFeedbackList(filteredOtherFeedbacks)

        // Update UI for average rating
        val averageRatingTextView: TextView? = findViewById(R.id.average_rating_text_view)
        averageRatingTextView?.text = String.format("Average Rating: %.1f", averageRating)
    }

    fun populateFeedbackForEdit(feedback: Feedback) {
        textField.setText(feedback.text ?: "")

        if (feedback.image != null) {
            val bitmap = BitmapFactory.decodeByteArray(feedback.image, 0, feedback.image.size)
            selectedImageView.setImageBitmap(bitmap)
            selectedImageView.visibility = View.VISIBLE
            selectedfeedImage = feedback.image
        } else {
            selectedImageView.setImageResource(R.drawable.ic_placeholder_image)
            selectedImageView.visibility = View.GONE
            selectedfeedImage = null
        }
    }



}