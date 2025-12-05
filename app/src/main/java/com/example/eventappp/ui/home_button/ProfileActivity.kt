package com.example.eventappp.ui.home_button

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var textName: TextView
    private lateinit var textPhone: TextView
    private lateinit var textEmail: TextView
   // private lateinit var textGender: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)



        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Dark icons? (if background is light)
            isAppearanceLightStatusBars = true
        }

        window.statusBarColor = Color.TRANSPARENT

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        textName = findViewById(R.id.textName)
        textPhone = findViewById(R.id.textPhone)
        textEmail = findViewById(R.id.textEmail)
       // textGender = findViewById(R.id.textGender)
        btnLogout = findViewById(R.id.btnLogout)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        val user = auth.currentUser
        if (user != null) {
            // Fetch user data from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        textName.text = doc.getString("name") ?: "Unknown"
                        textPhone.text = doc.getString("phone") ?: "Unknown"
                        textEmail.text = doc.getString("email") ?: "Unknown"
                       // textGender.text = doc.getString("gender") ?: "Unknown"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }
    }

    // Back button goes to MainActivity
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
