package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.Utils.TimeUtil
import com.ko.simple_chat.databinding.ReceiveItemBinding
import com.ko.simple_chat.databinding.SendItemBinding
import com.ko.simple_chat.model.Chat
import timber.log.Timber

class ChatRoomAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val itemList = mutableListOf<ChatTypeItem>()

    companion object {
        const val SEND = 0
        const val RECEIVE = 1
    }

    fun submitList(items: List<ChatTypeItem>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ChatTypeItem.Send -> SEND
            is ChatTypeItem.Receive -> RECEIVE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            SEND -> {
                val binding = SendItemBinding.inflate(inflater, parent, false)
                SendViewHolder(binding)
            }

            RECEIVE -> {
                val binding = ReceiveItemBinding.inflate(inflater, parent, false)
                ReceiveViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val uiType = itemList[position]

        when (uiType) {
            is ChatTypeItem.Send -> (holder as SendViewHolder).bind(uiType.chat)
            is ChatTypeItem.Receive -> (holder as ReceiveViewHolder).bind(uiType.chat)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class SendViewHolder(val binding: SendItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            Timber.d("SendViewHolder bind : $chat")
            binding.tvTime.text = TimeUtil.formatTIme(chat.time)
            binding.tvMessage.text = chat.message
        }
    }

    class ReceiveViewHolder(val binding: ReceiveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            Timber.d("ReceiveViewHolder bind : $chat")
            binding.tvTime.text = TimeUtil.formatTIme(System.currentTimeMillis())
            binding.tvMessage.text = chat.message
        }
    }
}