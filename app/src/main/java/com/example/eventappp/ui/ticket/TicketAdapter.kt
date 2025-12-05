package com.example.eventappp.ui.ticket

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.eventappp.R
import com.example.eventappp.ui.ticket.Ticket

class TicketAdapter(private val context: Context, private val tickets: List<Ticket>) : BaseAdapter() {

    override fun getCount(): Int = tickets.size

    override fun getItem(position: Int): Any = tickets[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false)
        val ticket = tickets[position]

        val imageView = view.findViewById<ImageView>(R.id.ticketImage)
        val titleView = view.findViewById<TextView>(R.id.ticketTitle)

        titleView.text = ticket.title
        Glide.with(context).load(ticket.imageUrl).into(imageView)

        return view
    }
}
