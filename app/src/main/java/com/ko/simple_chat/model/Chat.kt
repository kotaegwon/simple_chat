package com.ko.simple_chat.model

data class Chat(
    val id: Int = 0,
    val name: String = "",
    val message: String = "",
    val timeStamp: Long = System.currentTimeMillis()
)