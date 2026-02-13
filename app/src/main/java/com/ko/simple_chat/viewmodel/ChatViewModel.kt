package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.Chat

class ChatViewModel : ViewModel() {

    private val _chatList = MutableLiveData<List<Chat>>()
    val chatList: LiveData<List<Chat>> = _chatList


    fun listenChat(roomId: String) {
        FirebaseManager.listenMessage(roomId) { chatList ->
            _chatList.postValue(chatList)
        }
    }
}