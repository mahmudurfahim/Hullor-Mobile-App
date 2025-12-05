package com.example.eventappp.ui.news

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.databinding.ActivityNewsWebBinding

class NewsWebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsWebBinding.inflate(layoutInflater)
        setContentView(binding.root)



        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Dark icons? (if background is light)
            isAppearanceLightStatusBars = true
        }

        window.statusBarColor = Color.TRANSPARENT



        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }


        // WebView settings
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            webViewClient = WebViewClient()
        }

        // Load URL from intent or default
        val url = intent.getStringExtra("url") ?: "https://google.com"
        binding.webView.loadUrl(url)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if (webView.canGoBack()) {
            // Navigate back inside the WebView
            webView.goBack()
        } else {
            // No more pages to go back to, finish activity
            finish()
        }
    }
}
