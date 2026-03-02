package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.adapter.ChatListAdapter
import com.ko.simple_chat.adapter.RecyclerViewDecoration
import com.ko.simple_chat.databinding.FragmentChatListBinding
import com.ko.simple_chat.model.Chat
import com.ko.simple_chat.viewmodel.ChatListViewModel

class ChatListFragment : BaseFragment<FragmentChatListBinding, Chat>() {
    private lateinit var chatListAdapter: ChatListAdapter

    val viewModel: ChatListViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatListBinding {

        return FragmentChatListBinding.inflate(inflater, container, false)
    }

    override fun match(
        item: Chat,
        keyword: String
    ): Boolean {

        return item.name
            .lowercase()
            .contains(keyword)
    }

    override fun submitList(list: List<Chat>) {
        chatListAdapter.submitList(list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatListAdapter = ChatListAdapter()

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

            setUpMenu()
        }

        val list = listOf(
            Chat("1", "2", "고태권", "안녕하세요", System.currentTimeMillis()),
            Chat("1", "2", "김", "안녕하세요", System.currentTimeMillis())
        )

        chatListAdapter.submitList(list)
    }
}