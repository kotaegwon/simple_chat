package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.ChatListItem
import com.ko.simple_chat.model.User

class ChatListViewModel : ViewModel() {
    private val _chatList = MutableLiveData<List<ChatListItem>>()
    val chatList: LiveData<List<ChatListItem>> get() = _chatList

    private val _myChat = MutableLiveData<ChatListItem>()
    val myChat: LiveData<ChatListItem> get() = _myChat

    private val _myInfo = MutableLiveData<User?>()
    val myInfo: MutableLiveData<User?> get() = _myInfo

    init {
        loadChatList()
        loadMyInfo()
    }

    fun loadMyInfo() {
        FirebaseManager.loadMyUserInfo { user ->
            _myInfo.postValue(user)
        }
    }

    fun loadChatList() {
        FirebaseManager.loadChatList { list ->
            val myUid = FirebaseManager.auth.currentUser?.uid

            // self_chat
            val selfChat = list.firstOrNull { it.otherUid == myUid }
            selfChat?.let {
                _myChat.postValue(it)
            }

            // self_chat 제외 나머지
            val filtered = list.filter { it.otherUid != myUid }
            _chatList.postValue(filtered)
        }
    }
}