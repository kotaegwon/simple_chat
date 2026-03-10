package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ko.simple_chat.R
import com.ko.simple_chat.Utils.Def
import com.ko.simple_chat.Utils.Utils
import com.ko.simple_chat.adapter.ChatListAdapter
import com.ko.simple_chat.adapter.RecyclerViewDecoration
import com.ko.simple_chat.databinding.FragmentChatListBinding
import com.ko.simple_chat.model.ChatListItem
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ChatListViewModel
import timber.log.Timber

class ChatListFragment : BaseFragment<FragmentChatListBinding, ChatListItem>(),
    ChatListAdapter.Listener {
    private lateinit var chatListAdapter: ChatListAdapter

    val viewModel: ChatListViewModel by viewModels()

    var myInfo: User? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatListBinding {

        return FragmentChatListBinding.inflate(inflater, container, false)
    }

    override fun match(
        item: ChatListItem,
        keyword: String
    ): Boolean {
        return item.otherName.lowercase().contains(keyword) ||
                item.lastMessage.lowercase().contains(keyword)
    }

    override fun submitList(list: List<ChatListItem>) {
        chatListAdapter.submitList(list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatListAdapter = ChatListAdapter(this)

        binding.chatListRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatListAdapter
            addItemDecoration(
                RecyclerViewDecoration(
                    requireContext(),
                    0,
                    1
                )
            )
        }

        viewModel.chatList.observe(viewLifecycleOwner) { list ->
            originList.apply {
                clear()
                addAll(list)
            }

            filterList.apply {
                clear()
                addAll(list)
            }

            submitList(list)

            binding.chatListRecyclerview.scrollToPosition(list.size - 1)
        }

        viewModel.myChat.observe(viewLifecycleOwner) { item ->
            binding.tvMyChat.tvUser.text = item.otherName
            binding.tvMyChat.tvLastMessage.text = item.lastMessage
            binding.tvMyChat.tvLastTime.text = Utils.formatTImeY(item.updateAt)

            myInfo = User(
                name = item.otherName,
                uid = item.otherUid,
                email = "",
                createAt = item.updateAt
            )
        }

        viewModel.myInfo.observe(viewLifecycleOwner) { user ->
            Glide.with(requireContext())
                .load(user?.profileImageUrl)
                .placeholder(R.drawable.account_circle)
                .error(R.drawable.account_circle)
                .circleCrop()
                .into(binding.tvMyChat.imgProfile)
        }

        binding.tvMyChat.root.setOnClickListener {
            myInfo?.let {
                findNavController().navigate(
                    R.id.action_to_ChatRoom,
                    Bundle().apply {
                        putParcelable(Def.INTENT_USER_INFO, it)
                    })
            }
        }

        setUpMenu()
        setToolbar(true, getString(R.string.app_name), true)
    }

    override fun onItemClicked(chat: ChatListItem) {
        val user = User(
            name = chat.otherName,
            uid = chat.otherUid,
            email = "",
            createAt = chat.updateAt
        )

        findNavController().navigate(
            R.id.action_to_ChatRoom,
            Bundle().apply {
                putParcelable(Def.INTENT_USER_INFO, user)
            })
        Timber.d("onItemClick: $user")
    }
}