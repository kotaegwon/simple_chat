package com.ko.simple_chat.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.adapter.RecyclerViewDecoration
import com.ko.simple_chat.adapter.FriendAdapter
import com.ko.simple_chat.databinding.DialogFriendReqBinding
import com.ko.simple_chat.databinding.FragmentUserAddBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.FriendReqViewModel
import timber.log.Timber

class FriendReqFragment : BaseFragment<FragmentUserAddBinding, User>(), FriendAdapter.Listener {
    private lateinit var userAddAdapter: FriendAdapter

    private val friendReqViewModel: FriendReqViewModel by viewModels()

    override fun useSearchMenu(): Boolean {
        return true
    }

    override fun useAddMenu(): Boolean {
        return false
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserAddBinding {
        return FragmentUserAddBinding.inflate(inflater, container, false)
    }

    override fun match(item: User, keyword: String): Boolean {
        return item.name.lowercase().contains(keyword)
    }

    override fun submitList(list: List<User>) {
        userAddAdapter.submitList(list)
    }

    override fun onAcceptClicked(user: User) {
        Timber.d("onAcceptClicked: $user")

        FirebaseManager.acceptFriendRequest(user) { success, message ->
            Timber.d("onAcceptClicked: $success, $message")
        }
    }

    override fun onDeclineClicked(user: User) {
        Timber.d("onDeclineClicked: $user")

        FirebaseManager.declineFriendRequest(user) { success, message ->
            Timber.d("onDeclineClicked: $success, $message")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userAddAdapter = FriendAdapter(this)

        binding.addReqRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAddAdapter
            addItemDecoration(
                RecyclerViewDecoration(
                    requireContext(),
                    2,
                    1
                )
            )
        }

        friendReqViewModel.friendList.observe(viewLifecycleOwner) { list ->

            binding.tvNoReq.isVisible = list.isEmpty()

            originList.apply {
                clear()
                addAll(list)
            }

            filterList.apply {
                clear()
                addAll(list)
            }

            submitList(filterList)
        }

        setUpMenu()

        friendReqViewModel.loadFriendRequests()
    }
}