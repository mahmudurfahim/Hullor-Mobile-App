package com.hullor.app.ui.home_button

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hullor.app.MainActivity
import com.hullor.app.ui.dashboard.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hullor.app.R

class SavedListActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_DETAIL = 1001
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedListAdapter
    private lateinit var emptyView: LinearLayout
    private val savedEvents = mutableListOf<Event>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_list)


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

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.savedRecycler)
        emptyView = findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavedListAdapter(savedEvents) { event ->
            val intent = Intent(this, SavedDetailActivity::class.java)
            intent.putExtra("eventId", event.id)
            startActivityForResult(intent, REQUEST_CODE_DETAIL)
        }
        recyclerView.adapter = adapter

        fetchSavedEvents()


    }



    private fun fetchSavedEvents() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please log in to view saved events", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("saved")
            .whereEqualTo("userId", user.uid)
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    savedEvents.clear()
                    adapter.notifyDataSetChanged()
                    updateEmptyView()
                    return@addOnSuccessListener
                }

                val eventIds = snapshot.documents.mapNotNull { it.getString("eventId") }

                // Firestore allows up to 10 items in whereIn, so batch if needed
                val chunks = eventIds.chunked(10)
                val tempEvents = mutableListOf<Event>()

                val today = java.util.Calendar.getInstance().apply {
                    // Set time to start of today (00:00:00) for comparison
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time

                var completedChunks = 0
                for (chunk in chunks) {
                    db.collection("events")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { eventsSnapshot ->
                            for (doc in eventsSnapshot.documents) {
                                val eventDateValue = doc.get("eventDate")
                                val eventDate = when (eventDateValue) {
                                    is com.google.firebase.Timestamp -> eventDateValue
                                    is String -> try {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        com.google.firebase.Timestamp(sdf.parse(eventDateValue)!!)
                                    } catch (ex: Exception) { null }
                                    else -> null
                                }

                                val eventDateOnly = eventDate?.toDate()
                                if (eventDateOnly != null && eventDateOnly.before(today)) {
                                    // Delete outdated saved event document
                                    db.collection("saved")
                                        .whereEqualTo("userId", user.uid)
                                        .whereEqualTo("eventId", doc.id)
                                        .get()
                                        .addOnSuccessListener { savedSnapshot ->
                                            for (savedDoc in savedSnapshot.documents) {
                                                savedDoc.reference.delete()
                                            }
                                        }
                                    continue // skip adding to tempEvents
                                }

                                val event = Event(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "Untitled",
                                    description = doc.getString("description") ?: "No description",
                                    location = doc.getString("location") ?: "Unknown",
                                    imageUrl = doc.getString("imageUrl") ?: "",
                                    createdAt = doc.get("createdAt"),
                                    eventDate = eventDate
                                )
                                tempEvents.add(event)
                            }

                            completedChunks++
                            if (completedChunks == chunks.size) {
                                val orderedEvents = eventIds.mapNotNull { id ->
                                    tempEvents.find { it.id == id }
                                }

                                savedEvents.clear()
                                savedEvents.addAll(orderedEvents)
                                adapter.notifyDataSetChanged()
                                updateEmptyView()
                            }
                        }
                        .addOnFailureListener {
                            completedChunks++
                            if (completedChunks == chunks.size) {
                                val orderedEvents = eventIds.mapNotNull { id ->
                                    tempEvents.find { it.id == id }
                                }

                                savedEvents.clear()
                                savedEvents.addAll(orderedEvents)
                                adapter.notifyDataSetChanged()
                                updateEmptyView()
                            }
                        }
                }
            }
            .addOnFailureListener {
                savedEvents.clear()
                adapter.notifyDataSetChanged()
                updateEmptyView()
            }
    }


    private fun updateEmptyView() {
        if (savedEvents.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }



    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            val removedEventId = data?.getStringExtra("removedEventId")
            if (!removedEventId.isNullOrEmpty()) {
                val pos = savedEvents.indexOfFirst { it.id == removedEventId }
                if (pos != -1) {
                    savedEvents.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                    updateEmptyView()
                }
            }
        }
    }


}
