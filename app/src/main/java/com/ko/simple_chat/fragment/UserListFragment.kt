package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.adapter.UserListAdapter
import com.ko.simple_chat.databinding.FragmentUserListBinding
import com.ko.simple_chat.model.User
import timber.log.Timber

class UserListFragment : Fragment(), UserListAdapter.Listner {
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserListAdapter

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

        super.onViewCreated(view, savedInstanceState)

        val list = mutableListOf<User>()
        list.add(User("1", "user1", "고태권"))
        list.add(User("2", "user2", "김태권"))

        adapter.submitList(list)

        Timber.d("onViewCreated -")

    }

    override fun onDestroyView() {
        Timber.d("onDestroyView +")

        super.onDestroyView()
        _binding = null

        Timber.d("onDestroyView -")

    }

    override fun onItemClicked(user: User) {
        Timber.d("clicked user: $user")
    }
}