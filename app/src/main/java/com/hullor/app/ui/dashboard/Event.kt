package com.hullor.app.ui.dashboard

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val createdAt: Any? = null,
    val eventDate: Any? = null,
    val link: String = ""
)
