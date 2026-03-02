package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.databinding.ChatListItemBinding
import com.ko.simple_chat.model.Chat

class ChatListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemList = mutableListOf<Chat>()

    /**
     * 새로운 데이터를 Adapter에 전달하는 함수
     */
    fun submitList(items: List<Chat>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatListItemBinding.inflate(inflater, parent, false)

        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val chat = itemList[position]
        (holder as ChatViewHolder).bind(chat)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ChatViewHolder(val binding: ChatListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.tvUser.text = chat.name
            binding.tvLastMessage.text = chat.message
            binding.tvLastTime.text = chat.time.toString()
        }
    }
}