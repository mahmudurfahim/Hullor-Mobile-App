package com.example.eventappp.ui.home_button

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.ui.dashboard.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class SavedDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var eventImage: ImageView
    private lateinit var eventTitle: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventLocation: TextView
    private lateinit var eventDateText: TextView
    private lateinit var btnUnsave: ImageButton

    private var eventId: String? = null
    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_detail)



        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Dark icons? (if background is light)
            isAppearanceLightStatusBars = true
        }

        window.statusBarColor = Color.TRANSPARENT

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        eventImage = findViewById(R.id.eventImage)
        eventTitle = findViewById(R.id.eventTitle)
        eventDescription = findViewById(R.id.eventDescription)
        eventLocation = findViewById(R.id.eventLocation)
        eventDateText = findViewById(R.id.eventDateText)
        btnUnsave = findViewById(R.id.btnUnsave)

        eventId = intent.getStringExtra("eventId")
        if (eventId == null) {
            finish()
            return
        }

        loadEventDetails(eventId!!)

        btnUnsave.setOnClickListener {
            unsaveEvent()
        }
    }

    private fun loadEventDetails(eventId: String) {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "❌ Event not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val event = Event(
                    id = eventId,
                    title = doc.getString("title") ?: "Untitled",
                    description = doc.getString("description") ?: "No description",
                    location = doc.getString("location") ?: "Unknown",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    createdAt = doc.get("createdAt"),
                    eventDate = doc.get("eventDate")
                )
                currentEvent = event

                eventTitle.text = event.title
                eventDescription.text = event.description
                eventLocation.text = "Location: ${event.location}"
                eventDateText.text = "Date: ${formatDate(event.eventDate)}"

                Glide.with(this)
                    .load(event.imageUrl)
                    .placeholder(android.R.color.darker_gray)
                    .centerCrop()
                    .into(eventImage)
            }
    }

    private fun unsaveEvent() {
        val user = auth.currentUser ?: return
        val event = currentEvent ?: return

        db.collection("saved")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("eventId", event.id)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val batch = db.batch()
                    snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "❌ Unsaved", Toast.LENGTH_SHORT).show()

                            // Return the removed event ID to parent activity
                            val intent = intent
                            intent.putExtra("removedEventId", event.id)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                } else {
                    Toast.makeText(this, "⚠️ Event not found in saved list", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun formatDate(eventDate: Any?): String {
        if (eventDate == null) return "Unknown"
        val date: Date? = when (eventDate) {
            is Timestamp -> eventDate.toDate()
            is Date -> eventDate
            is String -> try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(eventDate)
            } catch (_: Exception) { null }
            else -> null
        }
        return if (date != null) SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(date) else "Unknown"
    }
}
