package com.ko.simple_chat.adapter

import com.ko.simple_chat.model.Chat

/**
 * 리사이클러뷰 아이템 타입
 * 송신 메시지와 수신 메시지를 구분하기 위해 사용
 */
sealed class ChatTypeItem {
    data class Send(val chat: Chat) : ChatTypeItem()
    data class Receive(val chat: Chat) : ChatTypeItem()
}