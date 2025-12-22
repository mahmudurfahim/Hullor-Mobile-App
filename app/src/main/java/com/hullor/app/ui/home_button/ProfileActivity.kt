package com.hullor.app.ui.home_button

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hullor.app.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hullor.app.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var textName: TextView
    private lateinit var textPhone: TextView
    private lateinit var textEmail: TextView

    private lateinit var btnLogout: TextView          // FIXED
    private lateinit var btnDeleteProfile: TextView   // FIXED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        textName = findViewById(R.id.textName)
        textPhone = findViewById(R.id.textPhone)
        textEmail = findViewById(R.id.textEmail)

        btnLogout = findViewById(R.id.btnLogout)             // FIXED
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile) // FIXED

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        textName.text = doc.getString("name") ?: "Unknown"
                        textPhone.text = doc.getString("phone") ?: "Unknown"
                        textEmail.text = doc.getString("email") ?: "Unknown"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnDeleteProfile.setOnClickListener {
            val userNow = auth.currentUser
            if (userNow != null) {

                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile? This cannot be undone.")
                    .setPositiveButton("Yes") { _, _ ->
                        db.collection("users").document(userNow.uid).delete()
                            .addOnSuccessListener {
                                userNow.delete().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
                                        startActivity(
                                            Intent(this, MainActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        )
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("No", null)
                    .create()

                // Set white background
                dialog.window?.setBackgroundDrawableResource(android.R.color.white)

                // Optional: make message and buttons black for readability
                dialog.show()
                dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            }
        }

        btnLogout.setOnClickListener {
            val userNow = auth.currentUser
            if (userNow != null) {
                // Show confirmation dialog before logout (optional)
                val logoutDialog = AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        auth.signOut() // Sign out from Firebase
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                        // Redirect to login or main screen
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .create()

                // White background for dialog
                logoutDialog.window?.setBackgroundDrawableResource(android.R.color.white)

                logoutDialog.show()
                logoutDialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
                logoutDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                logoutDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            }
        }


    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }
}
