package com.ko.simple_chat.model

data class ChatListItem(
    val roomId: String,
    val otherUid: String,
    val otherName: String,
    val lastMessage: String,
    val updateAt: Long
)
