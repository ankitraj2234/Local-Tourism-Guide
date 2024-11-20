package com.example.localtourismguide

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class MemoriesActivity : AppCompatActivity() {

    private lateinit var uploadImageButton: Button
    private lateinit var memoryImageView: ImageView
    private lateinit var memoryTextField: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemoriesAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var selectedImage: ByteArray? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories)

        dbHelper = DatabaseHelper(this)

        // Get username and locationName from Intent
        val username = intent.getStringExtra("username") ?: "default_username"
        val locationName = intent.getStringExtra("locationName") ?: "Unknown Location"

        // Initialize UI elements
        uploadImageButton = findViewById(R.id.uploadImageButton)
        memoryImageView = findViewById(R.id.memoryImageView)
        memoryTextField = findViewById(R.id.memoryTextField)
        addButton = findViewById(R.id.addButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Initially hide memoryImageView, memoryTextField, and addButton
        memoryImageView.visibility = View.GONE
        memoryTextField.visibility = View.GONE
        addButton.visibility = View.GONE

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MemoriesAdapter(dbHelper.getAllMemories(username, locationName)) { memory ->
            confirmDeleteMemory(memory, username, locationName)
        }
        recyclerView.adapter = adapter

        // Upload Image button listener
        uploadImageButton.setOnClickListener {
            selectImage()
        }

        // Add Button listener
        addButton.setOnClickListener {
            val memoryText = memoryTextField.text.toString()
            if (memoryText.isNotEmpty() && selectedImage != null) {
                dbHelper.addMemory(username, locationName, selectedImage!!, memoryText)
                refreshMemories(username, locationName)
                resetFields()
                Toast.makeText(this, "Memory added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Both image and text are required!", Toast.LENGTH_SHORT).show()
            }
        }
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
        memoryImageView.setImageBitmap(bitmap)
        memoryImageView.visibility = View.VISIBLE
        memoryTextField.visibility = View.VISIBLE
        addButton.visibility = View.VISIBLE
    }

    // Compress the image
    private fun compressImage(bitmap: Bitmap): ByteArray {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)
        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        resizedBitmap.recycle()
        return stream.toByteArray()
    }

    // Refresh RecyclerView with updated data
    private fun refreshMemories(username: String, locationName: String) {
        adapter.updateData(dbHelper.getAllMemories(username, locationName))
    }

    // Reset fields after adding a memory
    private fun resetFields() {
        memoryImageView.visibility = View.GONE
        memoryTextField.visibility = View.GONE
        addButton.visibility = View.GONE
        memoryTextField.text.clear()
        memoryImageView.setImageResource(0)
        selectedImage = null
    }

    // Confirm delete memory
    private fun confirmDeleteMemory(memory: Memory, username: String, locationName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory?")
            .setPositiveButton("Yes") { _, _ ->
                dbHelper.deleteMemory(memory.id)
                refreshMemories(username, locationName)
                Toast.makeText(this, "Memory deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
