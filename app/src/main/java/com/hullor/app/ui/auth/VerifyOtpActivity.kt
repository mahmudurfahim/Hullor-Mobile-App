package com.hullor.app.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hullor.app.R
import com.hullor.app.databinding.ActivityVerifyOtpBinding
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder

class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyOtpBinding
    private lateinit var timer: CountDownTimer
    private val client = OkHttpClient()
    private val smsApiKey = "uL0D9AMJ7dZ0GA51rC7C"
    private val senderId = "8809617625525"

    private var correctOtp = ""
    private var phone = ""
    private var name = ""
    private var emailInput = ""
    private var password = ""
    private var mode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        mode = intent.getStringExtra("mode")!!

        name = intent.getStringExtra("name") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        emailInput = intent.getStringExtra("emailInput") ?: ""
        password = intent.getStringExtra("password") ?: ""
        correctOtp = intent.getStringExtra("otp")!!

        startTimer()

        binding.btnVerify.setOnClickListener { verifyOtp() }
        binding.btnResend.setOnClickListener { resendOtp() }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun startTimer() {
        binding.btnResend.visibility = View.INVISIBLE
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(ms: Long) {
                binding.tvTimer.text = "Resend in ${ms / 1000}s"
            }

            override fun onFinish() {
                binding.tvTimer.text = "You can resend now"
                binding.btnResend.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun resendOtp() {
        correctOtp = (100000..999999).random().toString()
        sendOtp(correctOtp)
        startTimer()
    }

    private fun sendOtp(otp: String) {
        val number = "88$phone"
        val message = URLEncoder.encode("Your Hullor OTP is: $otp", "UTF-8")
        val url = "https://bulksmsbd.net/api/smsapi?api_key=$smsApiKey&senderid=$senderId&type=text&number=$number&message=$message"
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { Toast.makeText(this@VerifyOtpActivity, "OTP resent", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun verifyOtp() {
        val enteredOtp = binding.etOtp.text.toString().trim()
        if (enteredOtp != correctOtp) {
            Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show()
            return
        }

        when (mode) {
            "register" -> registerUser()
        }
    }

    private fun registerUser() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val firebaseEmail = "$phone@gmail.com"
        binding.btnVerify.isEnabled = false

        auth.createUserWithEmailAndPassword(firebaseEmail, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid
                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "phone" to phone,
                    "email" to emailInput,
                    "role" to "user"
                )
                db.collection("users").document(uid).set(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registration complete!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                    .addOnFailureListener { e ->
                        val message = when (e) {
                            is FirebaseNetworkException -> "Network error. Check your internet."
                            else -> "Failed to save user info: ${e.localizedMessage ?: "Unknown error"}"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        binding.btnVerify.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                val message = when (e) {
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "This phone is already registered."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is FirebaseNetworkException -> "Network error. Check your internet."
                    else -> "Registration failed: ${e.localizedMessage ?: "Unknown error"}"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                binding.btnVerify.isEnabled = true
            }
    }



}
