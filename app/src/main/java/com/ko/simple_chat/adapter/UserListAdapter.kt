package com.ko.simple_chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.databinding.UserListItemBinding
import com.ko.simple_chat.model.User
import timber.log.Timber

/**
 * 사용자 리스트 어댑터
 * Firebase에서 가져온 사용자 목록을 표시한다
 */
class UserListAdapter(val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemList = mutableListOf<User>()

    /**
     * 사용자를 선택했을 때 호출되는 인터페이스
     */
    interface Listener {
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

    /**
     * 뷰홀더를 생성하는 함수
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
        Timber.d("onCreateViewHolder +")

        val inflater = LayoutInflater.from(parent.context)
        val binding = UserListItemBinding.inflate(inflater, parent, false)

        Timber.d("onCreateViewHolder -")

        return UserViewHolder(binding)
    }

    /**
     * 뷰홀더에 데이터를 바인딩하는 함수
     *
     * @param holder 리사이클러뷰 뷰홀더
     * @param position 리사이클러뷰 아이템의 위치
     */
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        Timber.d("onBindViewHolder +")

        val user = itemList[position]
        Timber.d("onBindViewHodler: user: $user")

        (holder as UserViewHolder).bind(user)

        holder.itemView.setOnClickListener {
            listener.onItemClicked(user)
        }

        Timber.d("onBindViewHolder -")

    }

    /**
     * 리사이클러뷰 아이템의 개수를 반환하는 함수
     *
     * @return 리사이클러뷰 아이템의 개수
     */
    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * 뷰홀더 클래스
     *
     * @param binding 뷰바인딩 객체
     */
    class UserViewHolder(val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUser.text = user.name
        }
    }
}