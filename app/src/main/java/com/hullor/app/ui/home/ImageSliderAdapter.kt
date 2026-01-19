package com.hullor.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hullor.app.R

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

        // Make width 75% of screen
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val layoutParams = holder.image.layoutParams
        layoutParams.width = (screenWidth * 0.75).toInt()
        layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT // auto height
        holder.image.layoutParams = layoutParams

        // Load image
        Glide.with(holder.itemView.context)
            .load(banner.imageUrl)
            .into(holder.image)

// Make it stretch to fit both width & height
        holder.image.scaleType = ImageView.ScaleType.FIT_XY

    }

    override fun getItemCount(): Int = banners.size
}
