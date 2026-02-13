package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.adapter.UserListAdapter
import com.ko.simple_chat.databinding.FragmentUserListBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ToolbarViewModel
import timber.log.Timber
import com.ko.simple_chat.R


//TODO 상단바 유저 검색 기능, 채팅방 생성 기능 구현
class UserListFragment : Fragment(), UserListAdapter.Listner {
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserListAdapter

    val viewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView +")
        _binding = FragmentUserListBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.userRecyclerview.layoutManager = layoutManager

        adapter = UserListAdapter(this)
        binding.userRecyclerview.adapter = adapter

        Timber.d("onCreateView -")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated +")
        var myName: String = ""

        super.onViewCreated(view, savedInstanceState)


        FirebaseManager.loadUserList { list ->
            adapter.submitList(list)
        }

        FirebaseManager.loadMyUserInfo { user ->
            viewModel.setToolbar(true, user?.name ?: "")
        }

        Timber.d("onViewCreated -")

    }

    override fun onDestroyView() {
        Timber.d("onDestroyView +")

        super.onDestroyView()
        _binding = null

        Timber.d("onDestroyView -")

    }

    override fun onItemClicked(user: User) {
        findNavController().navigate(
            R.id.action_to_ChatRoom,
            Bundle().apply {
                putParcelable("user_info", user)
            })
        Timber.d("onItemClick: $user")
    }
}