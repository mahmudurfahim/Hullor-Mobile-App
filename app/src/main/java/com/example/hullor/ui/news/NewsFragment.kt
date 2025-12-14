package com.example.eventappp.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventappp.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val banglaFeeds = mapOf(
        "https://www.banglatribune.com/feed/" to "Bangla Tribune",
    )

    private val englishFeeds = mapOf(
        "https://www.tbsnews.net/top-news/rss.xml" to "The Business Standard"

    )

    private var banglaNews: List<NewsModel> = emptyList()
    private var englishNews: List<NewsModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.newsRecycler.layoutManager = LinearLayoutManager(requireContext())

        // 🔹 Tabs
        binding.newsTabs.addTab(binding.newsTabs.newTab().setText("Bangla"))
        binding.newsTabs.addTab(binding.newsTabs.newTab().setText("English"))

        binding.newsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val adapterNews = if (tab?.position == 0) banglaNews else englishNews
                binding.newsRecycler.adapter = NewsAdapter(adapterNews)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        preloadNews()
    }





    private fun preloadNews() {
        // Show progress bar
        binding.newsProgress.visibility = View.VISIBLE
        binding.newsRecycler.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            // Preload Bangla and English feeds in parallel
            val banglaDeferred = banglaFeeds.map { (url, source) ->
                async { RssParser.parse(url, source) }
            }

            val englishDeferred = englishFeeds.map { (url, source) ->
                async { RssParser.parse(url, source) }
            }

            banglaNews = interleaveFeeds(*banglaDeferred.awaitAll().toTypedArray())
            englishNews = interleaveFeeds(*englishDeferred.awaitAll().toTypedArray())

            withContext(Dispatchers.Main) {
                // Hide progress bar and show recycler
                binding.newsProgress.visibility = View.GONE
                binding.newsRecycler.visibility = View.VISIBLE

                // Show Bangla news by default
                binding.newsRecycler.adapter = NewsAdapter(banglaNews)
            }
        }
    }


    private fun interleaveFeeds(vararg lists: List<NewsModel>): List<NewsModel> {
        val result = mutableListOf<NewsModel>()
        val maxSize = lists.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxSize) {
            for (list in lists) {
                if (i < list.size) result.add(list[i])
            }
        }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
