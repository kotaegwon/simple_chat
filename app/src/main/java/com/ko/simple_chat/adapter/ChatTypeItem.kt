package com.ko.simple_chat.adapter

sealed class ChatTypeItem {
    data class Send(val message: String) : ChatTypeItem()
    data class Receive(val message: String) : ChatTypeItem()
}