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
}