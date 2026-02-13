package com.ko.simple_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolbarViewModel : ViewModel() {

    private val _showToolbar = MutableLiveData<Boolean>()
    private val _title = MutableLiveData<String>()

    val showToolbar: LiveData<Boolean> = _showToolbar
    val title: LiveData<String> = _title


    // 상태 변경 함수
    fun show() {
        _showToolbar.value = true
    }

    fun hide() {
        _showToolbar.value = false
    }

    fun setTitle(text: String) {
        _title.value = text
    }

    fun setToolbar(
        visible: Boolean,
        title: String
    ) {
        _showToolbar.value = visible
        _title.value = title
    }
}