package com.ko.simple_chat.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.R
import com.ko.simple_chat.Utils.Def
import com.ko.simple_chat.Utils.Utils
import com.ko.simple_chat.adapter.ChatRoomAdapter
import com.ko.simple_chat.adapter.ChatTypeItem
import com.ko.simple_chat.databinding.FragmentChatRoomBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.ChatRoom
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ChatViewModel
import timber.log.Timber

/**
 * 채팅방 프래그먼트
 * Firebase Authentication에 등록된 사용자 목록을 표시한다
 * 사용자를 선택하면 채팅방으로 이동한다
 * 송신 메시지와 수신 메시지를 표시한다
 * 송신 버튼을 누르면 메시지를 전송한다
 */
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding, ChatRoom>(), View.OnClickListener {

    // RecyclerView Adapter
    private lateinit var chatAdapter: ChatRoomAdapter
    var user: User? = null


    // ChatViewModel
    val chatViewModel: ChatViewModel by viewModels()

    // 나의 UID와 상대방의 UID
    var myUid: String = ""
    var otherUid: String = ""

    override fun useSearchMenu(): Boolean {
        return true
    }

    override fun useAddMenu(): Boolean {
        return false
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatRoomBinding {
        return FragmentChatRoomBinding.inflate(inflater, container, false)
    }

    override fun match(
        item: ChatRoom,
        keyword: String
    ): Boolean {
        return item.message
            .lowercase()
            .contains(keyword)
    }

    override fun submitList(list: List<ChatRoom>) {
        var lastDate: String? = null
        val uiList = mutableListOf<ChatTypeItem>()

        list.forEach { chat ->
            val currentDate = Utils.formatDateHeader(chat.time)

            if (lastDate != currentDate) {
                uiList.add(ChatTypeItem.Date(currentDate))
                lastDate = currentDate
            }

            if (chat.myUid == myUid) {
                uiList.add(ChatTypeItem.Send(chat))
            } else {
                uiList.add(ChatTypeItem.Receive(chat))
            }
        }
        chatAdapter.submitList(uiList)

        if (uiList.isNotEmpty()) {
            binding.mainRecyclerview.scrollToPosition(uiList.size - 1)
        }
    }


    /**
     * 뷰가 생성된 직후 호출
     * Firebase에서 사용자 목록과 내 정보 로드
     * 채팅방 메시지 관찰 및 Adapter에 적용
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        chatAdapter = ChatRoomAdapter()

        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        user = getArgument()

//        uiAdapterTest()
        otherUid = user!!.uid

        myUid = FirebaseManager.auth.currentUser!!.uid

        binding.imgSend.setOnClickListener(this)

        // 채팅방 ID 생성
        chatViewModel.listenChat(otherUid)

        // 특정 id의 채팅 내역을 listenChat으로 관찰
        chatViewModel.chatList.observe(viewLifecycleOwner) { list ->
            originList.apply {
                clear()
                addAll(list)
            }

            filterList.apply {
                clear()
                addAll(list)
            }
            submitList(list)

            binding.mainRecyclerview.scrollToPosition(list.size - 1)
        }

        setUpMenu()

    }

    /**
     * UserListFragment, MainActivity로 부터 User 클래스를 전달 받음
     */
    private fun getArgument(): User? {
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(
                Def.Intent.USER_INFO,
                User::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Def.Intent.USER_INFO)
        }

        Timber.d("getArgument: $user")

        setToolbar(true, user!!.name, true)

        return user
    }

    /**
     * 리사이클러뷰 송수신 레이아웃 더미 데이터
     */
    private fun uiAdapterTest() {
        val testList = mutableListOf<ChatTypeItem>()
        val sendData = ChatRoom(
            myUid = "0",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        val receive = ChatRoom(
            myUid = "1",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        testList.add(ChatTypeItem.Send(sendData))
        testList.add(ChatTypeItem.Receive(receive))

        chatAdapter.submitList(testList)
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
                            } else {

                            }
                        }
                    }
                }
            }
        }
    }
}