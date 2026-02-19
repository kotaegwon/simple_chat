package com.ko.simple_chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User

class LogInViewModel : ViewModel() {

    private val _myInfo = MutableLiveData<User?>()
    val myInfo: MutableLiveData<User?> get() = _myInfo

    init {
        loadMyUserInfo()
    }

    fun loadMyUserInfo() {
        FirebaseManager.loadMyUserInfo { user ->
            _myInfo.postValue(user)
        }
    }

    fun clearMyInfo() {
        _myInfo.value = null
    }
}