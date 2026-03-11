package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ko.simple_chat.R
import com.ko.simple_chat.databinding.UserAddItemBinding
import com.ko.simple_chat.model.User

class FriendAdapter(val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemList = mutableListOf<User>()

    interface Listener {
        fun onAcceptClicked(user: User)
        fun onDeclineClicked(user: User)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserAddItemBinding.inflate(inflater, parent, false)

        return UserAddViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val user = itemList[position]
        (holder as UserAddViewHolder).bind(user)

        holder.binding.tvAccept.setOnClickListener {
            listener.onAcceptClicked(user)
        }

        holder.binding.tvDecline.setOnClickListener {
            listener.onDeclineClicked(user)

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * 새로운 데이터를 Adapter에 전달하는 함수
     */
    fun submitList(items: List<User>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    class UserAddViewHolder(val binding: UserAddItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email

            Glide.with(binding.imgProfile.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.account_circle)
                .error(R.drawable.account_circle)
                .circleCrop()
                .into(binding.imgProfile)
        }
    }
}