package com.ko.simple_chat.model

data class ChatList(
    val users: List<String> = emptyList(),
    val lastMessage: String = "",
    val updateAt: Long = 0L
)
