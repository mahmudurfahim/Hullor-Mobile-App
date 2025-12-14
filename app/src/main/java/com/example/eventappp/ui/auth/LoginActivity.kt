package com.example.eventappp.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.databinding.ActivityLoginBinding
import com.example.eventappp.ui.home_button.ProfileActivity
import com.example.eventappp.ui.home_button.SavedListActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // FIX: Correct status bar padding (no repeated padding)
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLinearLayout) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                topInset,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        binding.btnLogin.setOnClickListener { loginUser() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { onBackPressed() }
    }

    private fun loginUser() {
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (phone.isEmpty() || phone.length != 11 || password.isEmpty()) {
            Toast.makeText(this, "Enter valid phone and password", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        val email = "$phone@gmail.com"

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                val destination = intent.getStringExtra("destination")
                val nextIntent = when (destination) {
                    "saved" -> Intent(this, SavedListActivity::class.java)
                    "profile" -> Intent(this, ProfileActivity::class.java)
                    else -> Intent(this, MainActivity::class.java)
                }

                nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(nextIntent)
            }
            .addOnFailureListener { exception ->
                binding.btnLogin.isEnabled = true

                val message = when (exception) {
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "User not found. Please register first."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid password. Try again."
                    is com.google.firebase.FirebaseNetworkException -> "Network error. Check your connection."
                    else -> exception.localizedMessage ?: "Login failed. Try again."
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
