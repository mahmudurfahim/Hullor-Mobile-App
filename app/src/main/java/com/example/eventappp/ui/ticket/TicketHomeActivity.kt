package com.example.eventappp.ui.ticket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.ui.home.HomeFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

        fetchTickets()

        ticketListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val ticket = tickets[position]

                // Open in Chrome or default browser
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ticket.websiteURL))
                startActivity(browserIntent)
            }

    }



    private fun fetchTickets() {
        db.collection("tickets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                tickets.clear()
                for (doc in snapshot.documents) {
                    val ticket = doc.toObject(Ticket::class.java)
                    ticket?.let { tickets.add(it) }
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
