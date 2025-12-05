package com.example.eventappp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.eventappp.databinding.FragmentDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!





    private val db = FirebaseFirestore.getInstance()
    private val eventList = mutableListOf<Event>()
    private lateinit var adapter: EventPagerAdapter

    private var lastViewedPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
       // adapter = EventPagerAdapter(eventList, showSaveButton = true)

        adapter = EventPagerAdapter(eventList)
        setupViewPager()




        if (savedInstanceState != null) {
            lastViewedPosition = savedInstanceState.getInt("last_position", 0)
        }

        loadEvents()
        return binding.root
    }

    private fun setupViewPager() {
        binding.viewPagerEvents.apply {
            adapter = this@DashboardFragment.adapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    lastViewedPosition = position
                }
            })
        }
    }



    private fun loadEvents() {
        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                eventList.clear()
                val today = java.util.Calendar.getInstance().apply {
                    // Set time to start of today (00:00:00) for comparison
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time

                for (doc in result) {
                    // Parse eventDate properly
                    val eventDateValue = doc.get("eventDate")
                    val eventDate = when (eventDateValue) {
                        is com.google.firebase.Timestamp -> eventDateValue
                        is String -> try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            com.google.firebase.Timestamp(sdf.parse(eventDateValue)!!)
                        } catch (e: Exception) { null }
                        else -> null
                    }

                    // Skip events that are in the past
                    val eventDateOnly = eventDate?.toDate()
                    if (eventDateOnly != null && eventDateOnly.before(today)) {
                        continue
                    }

                    val event = Event(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        createdAt = doc.get("createdAt"),
                        eventDate = eventDate
                    )
                    eventList.add(event)
                }

                // Sort events by eventDate ascending (earliest first)
                eventList.sortWith(compareBy { (it.eventDate as? com.google.firebase.Timestamp)?.toDate() })

                adapter.notifyDataSetChanged()

                if (eventList.isNotEmpty()) {
                    val positionToShow = if (lastViewedPosition < eventList.size) lastViewedPosition else 0
                    binding.viewPagerEvents.setCurrentItem(positionToShow, false)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("last_position", lastViewedPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
