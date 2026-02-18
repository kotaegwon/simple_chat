package com.ko.simple_chat.firebase

import com.ko.simple_chat.model.User

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object NotVerified : LoginResult()
    object NoneUser : LoginResult()
    object Fail : LoginResult()
}