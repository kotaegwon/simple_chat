package com.ko.simple_chat.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val createAt: Long = System.currentTimeMillis()
)
