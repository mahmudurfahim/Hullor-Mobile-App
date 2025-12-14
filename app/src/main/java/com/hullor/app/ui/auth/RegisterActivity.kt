package com.hullor.app.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hullor.app.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.hullor.app.R
import com.hullor.app.databinding.ActivityRegisterBinding
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import kotlin.random.Random

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val bulkSmsApiKey = "uL0D9AMJ7dZ0GA51rC7C"
    private val senderId = "8809617625525"
    private val client = OkHttpClient()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // White text & icons
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { onBackPressed() }

        binding.btnRegister.setOnClickListener { validateAndSendOtp() }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    private fun validateAndSendOtp() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val emailInput = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || emailInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all data", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.length != 11) {
            Toast.makeText(this, "Valid 11 digit phone required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            Toast.makeText(this, "Valid email required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length !in 8..12) {
            Toast.makeText(this, "Password must be 8–12 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if phone exists
        db.collection("users").whereEqualTo("phone", phone).get()
            .addOnSuccessListener { phoneDocs ->
                if (!phoneDocs.isEmpty) {
                    Toast.makeText(this, "Phone number already registered!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val otp = Random.nextInt(100000, 999999).toString()
                sendOtp(phone, otp)
                val intent = Intent(this, VerifyOtpActivity::class.java).apply {
                    putExtra("name", name)
                    putExtra("phone", phone)
                    putExtra("emailInput", emailInput)
                    putExtra("password", password)
                    putExtra("otp", otp)
                    putExtra("mode", "register")
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendOtp(phone: String, otp: String) {
        val number = "88$phone"
        val message = URLEncoder.encode("Your Hullor OTP is: $otp", "UTF-8")
        val url = "https://bulksmsbd.net/api/smsapi?api_key=$bulkSmsApiKey&senderid=$senderId&type=text&number=$number&message=$message"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "OTP sending failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "OTP sent", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
}
