package com.example.eventappp.ui.ticket

import com.google.firebase.Timestamp

data class Ticket(
    val title: String = "",
    val imageUrl: String = "",
    val websiteURL: String = "",
    val createdAt: Timestamp? = null,
    val eventDate: Timestamp? = null // use Timestamp instead of String
)
