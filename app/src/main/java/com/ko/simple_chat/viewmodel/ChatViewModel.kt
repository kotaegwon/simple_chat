package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.Chat

/**
 * 채팅 ViewModel
 * Firebase에서 채팅 메시지 가져오기
 * 송신 메시지와 수신 메시지 구분하기 위해 사용
 */
class ChatViewModel : ViewModel() {

    private val _chatList = MutableLiveData<List<Chat>>()
    val chatList: LiveData<List<Chat>> = _chatList


    /**
     * 채팅 메시지 가져오기
     *
     * @param roomId 방 ID
     * @param onResult 완료 콜백(메시지 목록))
     */
    fun listenChat(roomId: String) {
        FirebaseManager.listenMessage(roomId) { chatList ->
            _chatList.postValue(chatList)
        }
    }
}