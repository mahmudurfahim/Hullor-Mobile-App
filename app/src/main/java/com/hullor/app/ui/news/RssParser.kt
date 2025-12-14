package com.hullor.app.ui.news

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object RssParser {

    private val client = OkHttpClient()

    fun parse(url: String, source: String): List<NewsModel> {
        val list = mutableListOf<NewsModel>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()

            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(body))

            var eventType = parser.eventType
            var title = ""
            var link = ""
            var pubDate = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "item" -> { title = ""; link = ""; pubDate = "" }
                            "title" -> title = parser.nextText()
                            "link" -> link = parser.nextText()
                            "pubDate" -> pubDate = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag == "item" && title.isNotEmpty() && link.isNotEmpty()) {
                            val timeMillis = NewsDateUtils.parseDate(pubDate)
                            list.add(NewsModel(title, link, timeMillis, source))
                        }
                    }
                }
                eventType = parser.next()
            }

        } catch (e: Exception) {
            Log.e("RSS", "Error parsing: ${e.message}")
        }
        return list
    }
}
