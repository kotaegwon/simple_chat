package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User

class FriendReqViewModel : ViewModel() {
    private val _friendList = MutableLiveData<List<User>>()
    val friendList: LiveData<List<User>> = _friendList

    init {
        loadFriendRequests()
    }

    fun loadFriendRequests() {
        FirebaseManager.loadFriendRequest { list ->
            _friendList.postValue(list)
        }
    }
}
