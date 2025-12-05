package com.example.eventappp.ui.ticket

data class Ticket(
    val title: String = "",
    val imageUrl: String = "",
    val websiteURL: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val eventDate: Any? = null  // Timestamp, Date, or String
)
