package com.ko.simple_chat.model

/**
 * Firebase에서 사용하는 채팅 데이터 클래스
 *
 * @param myUid 내 uid
 * @param otherUid 상대방 uid
 * @param name 상대방 이름
 * @param message 메시지
 * @param time 메시지 전송 시간
 * @param read 메시지 읽음 여부
 *
 */
data class Chat(
    val myUid: String = "",
    val otherUid: String = "",
    val name: String = "",
    val message: String = "",
    val time: Long = 0L,
    val read: Boolean = false
)