package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User

class UserListViewModel : ViewModel() {

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> get() = _userList

    private val _myInfo = MutableLiveData<User?>()
    val myInfo: MutableLiveData<User?> get() = _myInfo

    private val _friendsList = MutableLiveData<List<User>>()
    val friendsList: LiveData<List<User>> get() = _friendsList

    init {
        loadUserList()
        loadMyUserInfo()
        loadFriendsList()
    }

    /**
     * Firebase에서 사용자 목록을 가져와서 LiveData에 저장
     */
    fun loadUserList() {
        FirebaseManager.loadUserList { list ->
            _userList.postValue(list)
        }
    }

    /**
     * Firebase에서 사용자 정보를 가져와서 LiveData에 저장
     */
    fun loadMyUserInfo() {
        FirebaseManager.loadMyUserInfo { user ->
            _myInfo.postValue(user)
        }
    }

    /**
     * Firebase에서 친구 목록을 가져와서 LiveData에 저장
     */
    fun loadFriendsList() {
        FirebaseManager.loadFriendList { list ->
            _friendsList.postValue(list)
        }
    }
}