package com.example.eventappp.ui.news

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventappp.R

class NewsAdapter(private val list: List<NewsModel>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val source: TextView = view.findViewById(R.id.newsSource)
        val title: TextView = view.findViewById(R.id.newsTitle)
        val timeAgo: TextView = view.findViewById(R.id.newsTimeAgo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = list[position]

        holder.source.text = item.source
        holder.title.text = item.title
        holder.timeAgo.text = NewsDateUtils.getTimeAgo(item.pubDate)

        holder.itemView.setOnClickListener {
            val ctx = holder.itemView.context
            val i = Intent(ctx, NewsWebActivity::class.java)
            i.putExtra("url", item.link)
            ctx.startActivity(i)
        }
    }

    override fun getItemCount() = list.size
}
