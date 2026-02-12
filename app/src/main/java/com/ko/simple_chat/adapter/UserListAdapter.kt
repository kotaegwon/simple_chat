package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.databinding.UserListItemBinding
import com.ko.simple_chat.model.User
import timber.log.Timber

class UserListAdapter(val listner: Listner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemList = mutableListOf<User>()

    interface Listner {
        fun onItemClicked(user: User)
    }

    /**
     * 새로운 데이터를 Adapter에 전달하는 함수
     */
    fun submitList(items: List<User>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        Timber.d("onCreateViewHolder +")

        val inflater = LayoutInflater.from(parent.context)
        val binding = UserListItemBinding.inflate(inflater, parent, false)

        Timber.d("onCreateViewHolder -")

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        Timber.d("onBindViewHolder +")

        val user = itemList[position]
        (holder as UserViewHolder).bind(user)

        holder.itemView.setOnClickListener {
            listner.onItemClicked(user)
        }

        Timber.d("onBindViewHolder -")

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class UserViewHolder(val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUser.text = user.name
        }
    }
}