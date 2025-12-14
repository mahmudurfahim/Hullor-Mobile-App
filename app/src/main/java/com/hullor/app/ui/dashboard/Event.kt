package com.hullor.app.ui.dashboard

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val createdAt: Any? = null,  // For ordering
    val eventDate: Any? = null   // Can be Timestamp, Date, or String
)
