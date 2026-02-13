package com.ko.simple_chat.adapter

import com.ko.simple_chat.model.Chat

sealed class ChatTypeItem {
    data class Send(val chat: Chat) : ChatTypeItem()
    data class Receive(val chat: Chat) : ChatTypeItem()
}