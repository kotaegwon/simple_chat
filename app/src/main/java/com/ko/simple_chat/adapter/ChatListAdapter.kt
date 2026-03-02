package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.Utils.Utils
import com.ko.simple_chat.databinding.ChatListItemBinding
import com.ko.simple_chat.model.ChatListItem

class ChatListAdapter(val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemList = mutableListOf<ChatListItem>()

    interface Listener {
        fun onItemClicked(chat: ChatListItem)
    }

    /**
     * 새로운 데이터를 Adapter에 전달하는 함수
     */
    fun submitList(items: List<ChatListItem>) {
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

        holder.itemView.setOnClickListener {
            listener.onItemClicked(chat)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ChatViewHolder(val binding: ChatListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatListItem) {
            binding.tvUser.text = chat.otherName
            binding.tvLastMessage.text = chat.lastMessage
            binding.tvLastTime.text = Utils.formatTImeY(chat.updateAt)
        }
    }
}