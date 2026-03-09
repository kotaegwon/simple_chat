package com.ko.simple_chat.Utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    fun formatTIme(millis: Long): String {
        val data = Date(millis)

        val format = SimpleDateFormat("a h:mm", Locale.KOREA)

        return format.format(data)
    }

    fun formatTImeY(millis: Long): String {
        val data = Date(millis)

        val format = SimpleDateFormat("yyyy MM dd a h:mm", Locale.KOREA)

        return format.format(data)
    }

    fun formatDateHeader(time: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREA)
        return sdf.format(Date(time))
    }
}