package com.example.eventappp.ui.ticket

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class TicketHomeActivity : AppCompatActivity() {

    private lateinit var ticketListView: ListView
    private val tickets = mutableListOf<Ticket>()
    private lateinit var adapter: TicketAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_home)

        ticketListView = findViewById(R.id.ticketListView)
        adapter = TicketAdapter(this, tickets)
        ticketListView.adapter = adapter

        // Transparent status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
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
        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        fetchTickets()

        ticketListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val ticket = tickets[position]
                val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ticket.websiteURL))
                startActivity(browserIntent)
            }
    }

    private fun fetchTickets() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        db.collection("tickets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                tickets.clear()
                for (doc in snapshot.documents) {
                    val ticket = doc.toObject(Ticket::class.java)
                    ticket?.let {
                        val eventDate = it.eventDate?.toDate()
                        if (eventDate != null) {
                            // Subtract 1 day to get last visible date
                            val lastVisibleDate = Calendar.getInstance().apply {
                                time = eventDate
                                add(Calendar.DAY_OF_MONTH, -1)
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.time

                            // Only show ticket if today is before or equal lastVisibleDate
                            if (!today.after(lastVisibleDate)) {
                                tickets.add(it)
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load tickets: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
