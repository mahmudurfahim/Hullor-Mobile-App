package com.hullor.app.ui.news

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hullor.app.MainActivity
import com.hullor.app.R
import com.hullor.app.databinding.ActivityNewsWebBinding

class NewsWebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // ================= SAFE WEBVIEW SETTINGS =================
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true

            allowFileAccess = false
            allowContentAccess = false

            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)

            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
        }

        // ================= SAFE URL FILTER =================
        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return true

                // Block non-https & dangerous schemes
                if (uri.scheme != "https") return true

                val host = uri.host ?: return true
                val allowedDomains = listOf(
                    "tryhullor.com",
                    "www.tryhullor.com"
                )

                return if (allowedDomains.any { host.endsWith(it) }) {
                    false // load inside WebView
                } else {
                    // Open external links safely
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                }
            }
        }

        binding.webView.webChromeClient = WebChromeClient()

        // ================= LOAD URL SAFELY =================
        val rawUrl = intent.getStringExtra("url") ?: "https://tryhullor.com"
        val uri = Uri.parse(rawUrl)
        if (uri.scheme == "https") {
            binding.webView.loadUrl(uri.toString())
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            finish()
        }
    }
}
