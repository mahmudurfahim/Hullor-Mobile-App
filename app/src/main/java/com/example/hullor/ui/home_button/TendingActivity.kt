package com.example.eventappp.ui.home_button

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
import androidx.viewpager2.widget.ViewPager2
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.ui.dashboard.Event
import com.example.eventappp.ui.dashboard.EventPagerAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TrendingActivity : AppCompatActivity() {



    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: EventPagerAdapter
    private val db = FirebaseFirestore.getInstance()
    private val trendingEvents = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending)



        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Dark icons? (if background is light)
            isAppearanceLightStatusBars = true
        }

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
        window.navigationBarColor = Color.TRANSPARENT


        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressed() // or finish()
        }

        viewPager = findViewById(R.id.trendingViewPager)
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        adapter = EventPagerAdapter(trendingEvents, showSaveButton = false)
        viewPager.adapter = adapter

        fetchTrendingEvents()
    }

    private fun fetchTrendingEvents() {
        db.collection("trending")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "⚠️ No trending events found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                trendingEvents.clear()
                val today = java.util.Calendar.getInstance().apply {
                    // Set time to start of today (00:00:00) for comparison
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time

                for (doc in snapshot.documents) {
                    val eventDateValue = doc.get("eventDate")
                    val eventDate = when (eventDateValue) {
                        is com.google.firebase.Timestamp -> eventDateValue
                        is String -> try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            com.google.firebase.Timestamp(sdf.parse(eventDateValue)!!)
                        } catch (ex: Exception) { null }
                        else -> null
                    }

                    // Skip past events
                    val eventDateOnly = eventDate?.toDate()
                    if (eventDateOnly != null && eventDateOnly.before(today)) {
                        continue
                    }

                    val event = Event(
                        id = doc.getString("eventId") ?: doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        description = doc.getString("description") ?: "No description",
                        location = doc.getString("location") ?: "Unknown",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        createdAt = doc.get("createdAt"),
                        eventDate = eventDate
                    )
                    trendingEvents.add(event)
                }

                // Sort by eventDate ascending → earliest event first
                trendingEvents.sortWith(compareBy { (it.eventDate as? com.google.firebase.Timestamp)?.toDate() })

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error loading trending events: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
