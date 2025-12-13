package com.example.eventappp.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.ui.auth.LoginActivity
import com.example.eventappp.ui.home_button.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get reference to the Log Out button
        val btnLogout: View = view.findViewById(R.id.btnLogout)

        // Hide Log Out button if user is not logged in
        if (auth.currentUser == null) {
            btnLogout.visibility = View.GONE
        }

        // Map of clickable layouts to URLs or actions
        val buttonMap: Map<Int, () -> Unit> = mapOf(
            R.id.btnProfile to { handleProfileButton() },
            R.id.btnAboutUs to { openWebView("https://www.tryhullor.com/about") },
            R.id.btnPrivacyPolicy to { openWebView("https://www.tryhullor.com/privacy") },
            R.id.btnTerms to { openWebView("https://www.tryhullor.com/terms") },
            R.id.btnContactUs to { openWebView("https://www.tryhullor.com/contact") },
            R.id.btnLogout to { handleLogout() }
        )

        // Set click listeners for all buttons
        for ((id, action) in buttonMap) {
            view.findViewById<View>(id).setOnClickListener { action() }
        }

        return view
    }

    // Open WebView activity with the given URL
    private fun openWebView(url: String) {
        val intent = Intent(requireContext(), MoreWebActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    // Handle Profile button click
    private fun handleProfileButton() {
        val user = auth.currentUser
        if (user == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.putExtra("destination", "profile")
            startActivity(intent)
        } else {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
    }

    // Handle Log Out button click
    private fun handleLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
