package com.example.eventappp.ui.news

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object NewsDateUtils {

    fun getTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()

        // Prevent future times (avoid "in 5 minutes")
        val safeTime = if (time > now) now else time

        val diff = now - safeTime

        // If less than 1 minute → show "just now"
        if (diff < 60_000) {
            return "Just now"
        }

        return DateUtils.getRelativeTimeSpanString(
            safeTime,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun parseDate(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            sdf.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

