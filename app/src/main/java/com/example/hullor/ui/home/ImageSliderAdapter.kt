package com.example.eventappp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventappp.R

class ImageSliderAdapter(private val banners: List<Slider>) :
    RecyclerView.Adapter<ImageSliderAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.sliderImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider_image, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]

        Glide.with(holder.itemView.context)
            .load(banner.imageUrl)
            //.placeholder(R.drawable.placeholder_image) // optional
            .centerCrop()
            .into(holder.image)
    }

    override fun getItemCount(): Int = banners.size
}
