package com.hullor.app.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.hullor.app.ui.auth.LoginActivity
import com.hullor.app.ui.home_button.SavedListActivity
import com.hullor.app.ui.ticket.TicketHomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hullor.app.R
import com.hullor.app.databinding.FragmentHomeBinding
import com.hullor.app.ui.news.NewsModel
import com.hullor.app.ui.news.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerAdapter: ImageSliderAdapter
    private val bannerList = mutableListOf<Slider>()

    private lateinit var trendingAdapter: ImageSliderAdapter
    private val trendingList = mutableListOf<Slider>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupBannerSlider()
        setupButtons()
        setupMiniBanner()
        setupSavedButton()
        loadBannerData()
        setupNewsAll()
        setupTicketButton()
        setupSliderSection()
        preloadGridNews()
        setupCollaborationButton()
        setupBackPressHandler()


        return binding.root
    }

    // -------------------- 🖼️ Banner Slider --------------------
    private fun setupBannerSlider() {
        bannerAdapter = ImageSliderAdapter(bannerList)
        binding.bannerViewPager.apply {
            adapter = bannerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 5
            clipToPadding = false
            clipChildren = false
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val recyclerView = binding.bannerViewPager.getChildAt(0) as RecyclerView
        recyclerView.clipToPadding = false
        recyclerView.setPadding(30, 0, 60, 0)   // space between pages
        recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.bannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (!isAdded || _binding == null) return
                updateSliderTitle(position)
            }
        })
    }

    private fun updateSliderTitle(position: Int) {
        if (bannerList.isNotEmpty() && position in bannerList.indices) {
            val fullTitle = bannerList[position].title
           // binding.sliderTitle.text =
                //if (fullTitle.length > 40) fullTitle.take(40) + "..." else fullTitle
        }
    }

    private fun loadBannerData() {
        db.collection("events")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }
                if (!isAdded || _binding == null || snapshot == null) return@addSnapshotListener

                bannerList.clear()
                val today = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time

                for (doc in snapshot) {
                    val title = doc.getString("title") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    // Parse eventDate
                    val eventDateValue = doc.get("eventDate")
                    val eventDate = when (eventDateValue) {
                        is com.google.firebase.Timestamp -> eventDateValue
                        is String -> try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            com.google.firebase.Timestamp(sdf.parse(eventDateValue)!!)
                        } catch (ex: Exception) { null }
                        else -> null
                    }

                    // Skip past events
                    val eventDateOnly = eventDate?.toDate()
                    if (eventDateOnly != null && eventDateOnly.before(today)) {
                        continue
                    }

                    // Add to banner list
                    bannerList.add(Slider(imageUrl, title, eventDate))
                }

                // Sort by eventDate ascending → earliest first
                bannerList.sortWith(compareBy { (it.eventDate as? com.google.firebase.Timestamp)?.toDate() })

                // Limit to 5 items
                if (bannerList.size > 5) {
                    bannerList.subList(5, bannerList.size).clear()
                }

                bannerAdapter.notifyDataSetChanged()
                if (bannerList.isNotEmpty()) updateSliderTitle(0)
            }
    }


    private val banglaFeeds = mapOf(
        "https://www.banglatribune.com/feed/" to "Bangla Tribune"
    )

    private val englishFeeds = mapOf(
        "https://www.tbsnews.net/top-news/rss.xml" to "The Business Standard"
    )

    private var banglaNews: List<NewsModel> = emptyList()
    private var englishNews: List<NewsModel> = emptyList()


    private fun preloadGridNews() {
        lifecycleScope.launch(Dispatchers.IO) {
            val banglaDeferred = banglaFeeds.map { (url, source) -> async { RssParser.parse(url, source) } }
            val englishDeferred = englishFeeds.map { (url, source) -> async { RssParser.parse(url, source) } }

            banglaNews = banglaDeferred.awaitAll().flatten()
            englishNews = englishDeferred.awaitAll().flatten()

            withContext(Dispatchers.Main) {
                populateGridNews()
            }
        }
    }

    private fun populateGridNews() {
        // Top row → first 2 English news
        if (englishNews.size >= 2) {
            binding.gridTopLeftText.text = englishNews[0].title
            binding.gridTopRightText.text = englishNews[1].title
        }

        // Bottom row → first 2 Bangla news
        if (banglaNews.size >= 2) {
            binding.gridBottomLeftText.text = banglaNews[0].title
            binding.gridBottomRightText.text = banglaNews[1].title
        }

        // Click listeners to open news
        binding.gridTopLeft.setOnClickListener { openNewsUrl(englishNews.getOrNull(0)?.link) }
        binding.gridTopRight.setOnClickListener { openNewsUrl(englishNews.getOrNull(1)?.link) }
        binding.gridBottomLeft.setOnClickListener { openNewsUrl(banglaNews.getOrNull(0)?.link) }
        binding.gridBottomRight.setOnClickListener { openNewsUrl(banglaNews.getOrNull(1)?.link) }
    }

    private fun openNewsUrl(url: String?) {
        url ?: return
        val intent = Intent(requireContext(), com.hullor.app.ui.news.NewsWebActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }








    // ------------------ 🟠 Buttons ------------------
    private fun setupButtons() {
        binding.exploreButton.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_dashboard
        }
    }


    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val dialog = AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        requireActivity().finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()

                // Set white background
                dialog.window?.setBackgroundDrawableResource(android.R.color.white)

                dialog.show()

                // Optional: Change message and button text color to black
                dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }



    private fun setupSavedButton() {
        binding.savedEventsButton.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.putExtra("destination", "saved")
                startActivity(intent)            } else {
                startActivity(Intent(requireContext(), SavedListActivity::class.java))
            }
        }
    }







    private fun setupTicketButton() {
        binding.getTicketButton.setOnClickListener {
            val intent = Intent(requireContext(), TicketHomeActivity::class.java)
            startActivity(intent)

        }
    }


    private fun setupCollaborationButton() {
        binding.bookEventButton.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_news

        }
    }

    private fun setupSliderSection() {
        binding.seeAllText.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_dashboard
        }
    }


    private fun setupNewsAll() {
        binding.seeAllHot.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_news
        }
    }



    private lateinit var sliderHandler: android.os.Handler
    private lateinit var sliderRunnable: Runnable

    private fun setupMiniBanner() {
        val miniBannerList = listOf(R.drawable.slider1, R.drawable.slider2)

        val miniBannerAdapter = AutoSlider(miniBannerList) { position ->
            if (!isAdded || _binding == null) return@AutoSlider
            when (position % miniBannerList.size) {
                0 -> { // slider1 clicked → TicketActivity
                    val intent = Intent(requireContext(), TicketHomeActivity::class.java)
                    startActivity(intent)
                }
                1 -> { // slider2 clicked → FragmentNews
                    requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                        .selectedItemId = R.id.navigation_news
                }
            }
        }

        binding.autoSliderHome.adapter = miniBannerAdapter

        // Start in the middle for infinite scroll
        val startPos = Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % miniBannerList.size
        binding.autoSliderHome.setCurrentItem(startPos, false)

        sliderHandler = android.os.Handler(android.os.Looper.getMainLooper())
        sliderRunnable = object : Runnable {
            override fun run() {
                if (!isAdded || _binding == null) return
                val next = binding.autoSliderHome.currentItem + 1
                binding.autoSliderHome.setCurrentItem(next, true)
                sliderHandler.postDelayed(this, 4000)
            }
        }

        binding.autoSliderHome.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 4000)
            }
        })

        sliderHandler.postDelayed(sliderRunnable, 4000)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable) // stop auto-slide
        _binding = null
    }
}

