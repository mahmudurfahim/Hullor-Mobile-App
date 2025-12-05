package com.example.eventappp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.databinding.ActivityVerifyOtpBinding
import com.example.eventappp.ui.ticket.TicketHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        name = intent.getStringExtra("name")!!
        phone = intent.getStringExtra("phone")!!
        emailInput = intent.getStringExtra("emailInput")!!
        password = intent.getStringExtra("password")!!
        correctOtp = intent.getStringExtra("otp")!!

        startTimer()

        binding.btnVerify.setOnClickListener {
            val entered = binding.etOtp.text.toString().trim()
            if (entered == correctOtp) {
                registerUser()
            } else {
                Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnResend.setOnClickListener {
            val newOtp = (100000..999999).random().toString()
            correctOtp = newOtp
            sendOtp(newOtp)
            startTimer()
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }


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
        }
        timer.start()
    }


    private fun sendOtp(otp: String) {
        val number = "88$phone"
        val message = URLEncoder.encode("Your Hullor OTP is: $otp", "UTF-8")
        val url =
            "https://bulksmsbd.net/api/smsapi?api_key=$smsApiKey&senderid=$senderId&type=text&number=$number&message=$message"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { Toast.makeText(this@VerifyOtpActivity, "OTP resent", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun registerUser() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val firebaseEmail = "${phone}@gmail.com"

        // Disable verify button to prevent multiple clicks
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
                        Toast.makeText(this, "Failed to save user info: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnVerify.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnVerify.isEnabled = true
            }


    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, RegisterActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

}


