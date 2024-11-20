package com.example.localtourismguide

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var generatedOtp: String? = null // To store generated OTP
    private var emailForOtp: String? = null // Email for OTP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        dbHelper = DatabaseHelper(this)

        val forgotBox = findViewById<LinearLayout>(R.id.forbox)
        forgotBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmNewPassword = findViewById<EditText>(R.id.etConfirmNewPassword)

        val btnSendOtp = findViewById<Button>(R.id.btnSendOtp)
        val btnVerifyOtp = findViewById<Button>(R.id.btnVerifyOtp)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)

        btnSendOtp.setOnClickListener {
            val emailOrUsername = etUsername.text.toString()

            if (emailOrUsername.isBlank()) {
                Toast.makeText(this, "Please enter your username or email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!dbHelper.isUserExists(emailOrUsername)) {
                Toast.makeText(this, "User not found in the database", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            emailForOtp = dbHelper.getUserEmail(emailOrUsername)
            generatedOtp = (100000..999999).random().toString() // Generate OTP

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    EmailSender.sendEmail(
                        toEmail = emailForOtp!!,
                        subject = "Password Reset OTP",
                        body = "Your OTP is: $generatedOtp"
                    )
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                        etOtp.visibility = android.view.View.VISIBLE
                        btnVerifyOtp.visibility = android.view.View.VISIBLE
                        btnSendOtp.visibility = android.view.View.GONE
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnVerifyOtp.setOnClickListener {
            val enteredOtp = etOtp.text.toString()
            if (enteredOtp == generatedOtp) {
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show()
                etNewPassword.visibility = android.view.View.VISIBLE
                etConfirmNewPassword.visibility = android.view.View.VISIBLE
                btnResetPassword.visibility = android.view.View.VISIBLE
                etOtp.visibility = android.view.View.GONE
                btnVerifyOtp.visibility = android.view.View.GONE
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()

            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.updatePassword(emailForOtp!!, newPassword)) {
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
