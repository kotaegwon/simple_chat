package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User

class UserListViewModel : ViewModel() {

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> = _userList

    private val _myInfo = MutableLiveData<User?>()
    val myInfo: MutableLiveData<User?> = _myInfo

    init {
        loadUserList()
        loadMyUserInfo()
    }

    fun loadUserList() {
        FirebaseManager.loadUserList { list ->
            _userList.postValue(list)
        }
    }

    fun loadMyUserInfo() {
        FirebaseManager.loadMyUserInfo { user ->
            _myInfo.postValue(user)
        }
    }
}