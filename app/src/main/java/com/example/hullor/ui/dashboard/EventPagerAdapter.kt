package com.example.eventappp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventappp.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EventPagerAdapter(
    private val events: List<Event>,
    private val showSaveButton: Boolean = true
) : RecyclerView.Adapter<EventPagerAdapter.EventViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Store saved document IDs for each event
    private val savedDocIds = mutableMapOf<String, String>() // eventId -> savedDocId

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.eventImage)
        val eventTitle: TextView = view.findViewById(R.id.eventTitle)
        val eventDescription: TextView = view.findViewById(R.id.eventDescription)
        val eventLocation: TextView = view.findViewById(R.id.eventLocation)
        val eventDateText: TextView = view.findViewById(R.id.eventTimeAgo)
        val btnSave: ImageButton = view.findViewById(R.id.btnSaveEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.eventTitle.text = event.title
        holder.eventDescription.text = event.description
        holder.eventLocation.text = "Location: ${event.location}"
        holder.eventDateText.text = "Date: ${formatDate(event.eventDate)}"

        Glide.with(holder.itemView.context)
            .load(event.imageUrl)
            .placeholder(android.R.color.darker_gray)
            .centerCrop()
            .into(holder.eventImage)

        // Always show save button
        holder.btnSave.visibility = View.VISIBLE

        val user = auth.currentUser
        val savedRef = db.collection("saved")

        if (user == null) {
            // User not logged in → show Toast when clicked
            holder.btnSave.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Please log in to save events",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Optional: show unfilled icon for not saved
            holder.btnSave.isSelected = false
            holder.btnSave.setImageResource(R.drawable.ic_save_outline)
            return
        }

        // User logged in → normal save/unsave logic
        holder.btnSave.isEnabled = false

        savedRef.whereEqualTo("eventId", event.id)
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val isSaved = snapshot.documents.isNotEmpty()
                val docId = if (isSaved) snapshot.documents[0].id else null

                if (docId != null) savedDocIds[event.id] = docId

                updateSaveButton(holder, isSaved)

                holder.btnSave.isEnabled = true

                holder.btnSave.setOnClickListener {
                    holder.btnSave.isEnabled = false

                    val currentlySaved = savedDocIds.containsKey(event.id)

                    if (currentlySaved) {
                        // UNSAVE
                        val docToDelete = savedDocIds[event.id]!!
                        savedRef.document(docToDelete).delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Unsaved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                savedDocIds.remove(event.id)
                                updateSaveButton(holder, false)
                                holder.btnSave.isEnabled = true
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                holder.btnSave.isEnabled = true
                            }
                        return@setOnClickListener
                    }

                    // SAVE
                    db.collection("events").document(event.id).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Event not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                                holder.btnSave.isEnabled = true
                                return@addOnSuccessListener
                            }

                            val data = doc.data!!.toMutableMap()
                            data["userId"] = user.uid
                            data["eventId"] = event.id
                            data["savedAt"] = Timestamp.now()

                            savedRef.add(data)
                                .addOnSuccessListener { savedDoc ->
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    savedDocIds[event.id] = savedDoc.id
                                    updateSaveButton(holder, true)
                                    holder.btnSave.isEnabled = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    holder.btnSave.isEnabled = true
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    holder.itemView.context,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                holder.btnSave.isEnabled = true
            }
    }


    private fun updateSaveButton(holder: EventViewHolder, isSaved: Boolean) {
        holder.btnSave.isSelected = isSaved
        holder.btnSave.setImageResource(
            if (isSaved) R.drawable.ic_save_filled else R.drawable.ic_save_outline
        )
    }

    override fun getItemCount(): Int = events.size

    private fun formatDate(eventDate: Any?): String {
        if (eventDate == null) return "Unknown"

        val date: Date? = when (eventDate) {
            is Timestamp -> eventDate.toDate()
            is Date -> eventDate
            is String -> try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(eventDate)
            } catch (e: Exception) { null }
            else -> null
        }

        if (date == null) return "Unknown"

        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}
