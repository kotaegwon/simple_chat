package com.ko.simple_chat.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.R
import com.ko.simple_chat.adapter.ChatRoomAdapter
import com.ko.simple_chat.adapter.ChatTypeItem
import com.ko.simple_chat.databinding.FragmentChatRoomBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.Chat
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ChatViewModel
import com.ko.simple_chat.viewmodel.ToolbarViewModel

class ChatRoomFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatRoomAdapter
    var user: User? = null

    val viewModel: ToolbarViewModel by activityViewModels()
    val chatViewModel: ChatViewModel by viewModels()

    var myUid: String = ""
    var otherUid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)


        val layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatRoomAdapter()

        binding.mainRecyclerview.layoutManager = layoutManager
        binding.mainRecyclerview.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = getArgument()

//        uiAdapterTest()
        otherUid = user!!.uid
        myUid = FirebaseManager.auth.currentUser!!.uid

        binding.imgSend.setOnClickListener(this)

        chatViewModel.listenChat(otherUid)

        chatViewModel.chatList.observe(viewLifecycleOwner) { list ->
            val list = list.map {
                if (it.myUid == myUid) {
                    ChatTypeItem.Send(it)
                } else {
                    ChatTypeItem.Receive(it)
                }
            }
            adapter.submitList(list)

            binding.mainRecyclerview.scrollToPosition(list.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun getArgument(): User? {
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

        return user
    }

    private fun uiAdapterTest() {
        val testList = mutableListOf<ChatTypeItem>()
        val sendData = Chat(
            myUid = "0",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        val receive = Chat(
            myUid = "1",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        testList.add(ChatTypeItem.Send(sendData))
        testList.add(ChatTypeItem.Receive(receive))

        adapter.submitList(testList)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_send -> {
                val msg = binding.editMessage.text.toString()

                if (msg.isNotEmpty()) {
                    user?.let {
                        FirebaseManager.sendMessage(
                            otherUid = it.uid,
                            message = msg
                        ) { success ->
                            if (success) {
                                binding.editMessage.text.clear()
                            }
                        }
                    }
                }
            }
        }
    }
}