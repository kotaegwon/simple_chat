package com.ko.simple_chat.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ko.simple_chat.databinding.FragmentChatRoomBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ToolbarViewModel

class ChatRoomFragment : Fragment() {
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    val viewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgument()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun getArgument() {
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(
                "user_info",
                User::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("diary_item")
        }

        viewModel.setToolbar(true, "${user?.name}")

    }
}