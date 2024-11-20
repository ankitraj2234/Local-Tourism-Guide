package com.example.localtourismguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordFailedOnce = false // Track incorrect password attempts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val loginBox = findViewById<LinearLayout>(R.id.loginBox)
        val signUpText = findViewById<TextView>(R.id.tvSignUp)
        val slogo = findViewById<ImageView>(R.id.slogo)
        val bgimg = findViewById<FrameLayout>(R.id.bgimg)

        loginBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        signUpText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        slogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        bgimg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_bg))


        // TextWatcher for Username Validation
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString()
                if (username.isEmpty()) {
                    etUsername.error = "Username cannot be empty"
                }
            }
        })

        // TextWatcher for Password Validation
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 6) {
                    etPassword.error = "Password must be at least 6 characters long"
                }
            }
        })

        // Login button action
        btnLogin.setOnClickListener {
            val usernameOrEmail = etUsername.text.toString() // Username or Email
            val password = etPassword.text.toString()

            if (dbHelper.loginUser(usernameOrEmail, password)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Save login session
                with(sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    putString("loggedInUsername", usernameOrEmail) // Store the username or email
                    apply()
                }

                // Navigate to the main map view page
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                isPasswordFailedOnce = true
                tvForgotPassword.visibility = TextView.VISIBLE // Show Forgot Password option
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }


        // Sign Up action
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Forgot Password action
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
