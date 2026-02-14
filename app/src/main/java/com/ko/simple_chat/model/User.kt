package com.ko.simple_chat.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Firebase에서 사용하는 사용자 데이터 클래스
 *
 * @param uid 사용자 uid
 * @param email 사용자 이메일
 * @param name 사용자 이름
 * @param createAt 사용자 생성 시간
 */
@Parcelize
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val createAt: Long = 0L
) : Parcelable
