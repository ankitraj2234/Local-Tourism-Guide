package com.example.localtourismguide

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class SignupActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var ivProfileImage: ImageView
    private var imageSelected = false
    private var imageBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etName = findViewById<EditText>(R.id.etName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val signupBox = findViewById<LinearLayout>(R.id.signupbx)

        signupBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        ivProfileImage = findViewById(R.id.ivProfileImage)



        // Image selection
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Add TextWatcher for password validation
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (!validatePassword(password)) {
                    etPassword.error = "Password must contain at least 6 characters, including letters, numbers, and special characters"
                }
            }
        })

        // Add TextWatcher for confirm password validation
        etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                if (confirmPassword != etPassword.text.toString()) {
                    etConfirmPassword.error = "Passwords do not match"
                }
            }
        })

        // Registration button action
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val name = etName.text.toString()
            val age = etAge.text.toString().toIntOrNull() ?: 0
            val mobile = etMobile.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validateInputs(username, name, age, mobile, email, password, confirmPassword)) {
                // Check if the fields are unique
                when {
                    !dbHelper.isFieldUnique(DatabaseHelper.COLUMN_USERNAME, username) -> {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                    !dbHelper.isFieldUnique(DatabaseHelper.COLUMN_EMAIL, email) -> {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                    }
                    !dbHelper.isFieldUnique(DatabaseHelper.COLUMN_MOBILE, mobile) -> {
                        Toast.makeText(this, "Mobile number already exists", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Proceed with registration
                        val success = dbHelper.registerUser(username, name, age, mobile, email, password, imageBytes!!)
                        if (success) {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

                // Compress the image by reducing the resolution to half
                val compressedBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    bitmap.width / 2,
                    bitmap.height / 2,
                    true
                )

                // Further compress the image quality to reduce size
                val outputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                imageBytes = outputStream.toByteArray()

                // Display the compressed image in the ImageView
                ivProfileImage.setImageBitmap(compressedBitmap)
                ivProfileImage.visibility = View.VISIBLE
                imageSelected = true
            }
        }
    }


    // Validate inputs and image selection
    private fun validateInputs(username: String, name: String, age: Int, mobile: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || name.isEmpty() || age <= 0 || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()  || !imageSelected) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!validatePassword(password)) {
            Toast.makeText(this, "Password does not meet security requirements", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Password validation logic
    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,}$"
        return password.matches(regex.toRegex())
    }
}
