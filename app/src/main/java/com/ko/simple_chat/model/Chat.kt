package com.ko.simple_chat.model

data class Chat(
    val myUid: String = "",
    val otherUid: String = "",
    val name: String = "",
    val message: String = "",
    val time: Long = 0L,
    val read: Boolean = false
)