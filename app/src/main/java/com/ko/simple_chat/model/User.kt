package com.ko.simple_chat.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val createAt: Long = System.currentTimeMillis()
): Parcelable
