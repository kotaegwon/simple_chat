package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.Utils.Utils
import com.ko.simple_chat.databinding.ReceiveItemBinding
import com.ko.simple_chat.databinding.SendItemBinding
import com.ko.simple_chat.model.Chat
import timber.log.Timber

/**
 * 리사이클러뷰 어댑터
 * 송신 메시지와 수신 메시지를 표시한다
 */
class ChatRoomAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 리사이클러뷰 아이템 리스트
    val itemList = mutableListOf<ChatTypeItem>()

    // 송신, 수신 메시지 구분을 위한 타입
    companion object {
        const val SEND = 0
        const val RECEIVE = 1
    }

    /**
     * 리사이클러뷰 아이템 리스트를 갱신한다
     */
    fun submitList(items: List<ChatTypeItem>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * 리사이클러뷰 아이템의 타입을 반환한다
     */
    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ChatTypeItem.Send -> SEND
            is ChatTypeItem.Receive -> RECEIVE
        }
    }

    /**
     * 리사이클러뷰 뷰홀더를 생성한다
     *
     * @param parent 리사이클러뷰 뷰홀더의 부모 뷰
     * @param viewType 리사이클러뷰 뷰홀더의 타입
     *
     * @return 리사이클러뷰 뷰홀더
     */
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

    /**
     * 리사이클러뷰 뷰홀더에 데이터를 바인딩한다
     *
     * @param holder 리사이클러뷰 뷰홀더
     * @param position 리사이클러뷰 아이템의 위치
     */
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

    /**
     * 리사이클러뷰 아이템의 개수를 반환한다
     *
     * @return 리사이클러뷰 아이템의 개수
     */
    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * 송신 메시지 뷰홀더
     *
     * @param binding 송신 메시지 뷰바인딩
     */
    class SendViewHolder(val binding: SendItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            Timber.d("SendViewHolder bind : $chat")
            binding.tvTime.text = Utils.formatTIme(chat.time)
            binding.tvMessage.text = chat.message
        }
    }

    /**
     * 수신 메시지 뷰홀더
     *
     * @param binding 수신 메시지 뷰바인딩
     */
    class ReceiveViewHolder(val binding: ReceiveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            Timber.d("ReceiveViewHolder bind : $chat")
            binding.tvTime.text = Utils.formatTIme(chat.time)
            binding.tvMessage.text = chat.message
        }
    }
}