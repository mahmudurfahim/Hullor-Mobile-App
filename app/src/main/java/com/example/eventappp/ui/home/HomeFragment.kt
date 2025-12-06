package com.example.eventappp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.eventappp.R
import com.example.eventappp.databinding.FragmentHomeBinding
import com.example.eventappp.ui.auth.LoginActivity
import com.example.eventappp.ui.home_button.CollaborationActivity
import com.example.eventappp.ui.home_button.SavedListActivity
import com.example.eventappp.ui.home_button.TrendingActivity
import com.example.eventappp.ui.news.NewsFragment
import com.example.eventappp.ui.ticket.TicketHomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
        setupTrendingSlider()
        setupButtons()
        setupMiniBanner()
        setupSavedButton()
        setupBookingTV()
        loadBannerData()
        loadTrendingData()
        setupTicketButton()
        setupSliderSection()
        setupTrendingSection()
        setupCollaborationButton()

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

    // ✅ Real-time banner sync (image + title) sorted by eventDate
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
                    // Set time to start of today (00:00:00) for comparison
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

                bannerAdapter.notifyDataSetChanged()
                if (bannerList.isNotEmpty()) updateSliderTitle(0)
            }
    }



    // -------------------- 🔥 Trending Slider --------------------
    private fun setupTrendingSlider() {
        trendingAdapter = ImageSliderAdapter(trendingList)
        binding.trendingViewPager.apply {
            adapter = trendingAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            clipToPadding = false
            clipChildren = false
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        binding.trendingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (!isAdded || _binding == null) return
                updateTrendingTitle(position)
            }
        })
    }

    private fun updateTrendingTitle(position: Int) {
        if (trendingList.isNotEmpty() && position in trendingList.indices) {
            val fullTitle = trendingList[position].title
            //binding.trendingTitle.text =
               // if (fullTitle.length > 40) fullTitle.take(40) + "..." else fullTitle
        }
    }

    // -------------------- 🔥 Trending Slider --------------------
    private fun loadTrendingData() {
        db.collection("trending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }
                if (!isAdded || _binding == null || snapshot == null) return@addSnapshotListener

                trendingList.clear()
                val today = java.util.Calendar.getInstance().apply {
                    // Set time to start of today (00:00:00) for comparison
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

                    trendingList.add(Slider(imageUrl, title, eventDate))
                }

                // Sort by eventDate ascending → earliest first
                trendingList.sortWith(compareBy { (it.eventDate as? com.google.firebase.Timestamp)?.toDate() })

                trendingAdapter.notifyDataSetChanged()
                if (trendingList.isNotEmpty()) updateTrendingTitle(0)
            }
    }




    // ------------------ 🟠 Buttons ------------------
    private fun setupButtons() {
        binding.exploreButton.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_dashboard
        }
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



    private fun setupBookingTV() {
        binding.BookingTV.setOnClickListener {
            val intent = Intent(requireContext(), CollaborationActivity::class.java)
            startActivity(intent)
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
            val intent = Intent(requireContext(), CollaborationActivity::class.java)
            startActivity(intent)

        }
    }

    private fun setupSliderSection() {
        binding.seeAllText.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_dashboard
        }
    }



    private fun setupTrendingSection() {
        binding.trendingSeeAll.setOnClickListener {
            val intent = Intent(requireContext(), TrendingActivity::class.java)
            startActivity(intent)

        }
    }

    private lateinit var sliderHandler: android.os.Handler
    private lateinit var sliderRunnable: Runnable

    private fun setupMiniBanner() {
        val miniBannerList = listOf(R.drawable.slider1, R.drawable.slider2)

        val miniBannerAdapter = AutoSlider(miniBannerList) { position ->
            if (!isAdded || _binding == null) return@AutoSlider
            when (position % miniBannerList.size) {
                0 -> { // slider1 clicked → TrendingActivity
                    val intent = Intent(requireContext(), TrendingActivity::class.java)
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

