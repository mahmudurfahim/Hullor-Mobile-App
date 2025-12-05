package com.example.eventappp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventappp.R

class AutoSlider(
    private val images: List<Int>,
    private val onClick: (position: Int) -> Unit
) : RecyclerView.Adapter<AutoSlider.BannerViewHolder>() {

    inner class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageBanner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_autoslider, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        // Use modulo to loop infinitely
        val realPosition = position % images.size
        holder.image.setImageResource(images[realPosition])
        holder.image.setOnClickListener { onClick(realPosition) }
    }

    override fun getItemCount(): Int = Int.MAX_VALUE
}
