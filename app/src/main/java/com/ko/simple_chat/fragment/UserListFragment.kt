package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ko.simple_chat.adapter.UserListAdapter
import com.ko.simple_chat.databinding.FragmentUserListBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User
import timber.log.Timber
import com.ko.simple_chat.R
import com.ko.simple_chat.adapter.RecyclerViewDecoration
import com.ko.simple_chat.viewmodel.UserListViewModel


/**
 * 채팅 앱 가입자 리스트 프래그먼트
 * Firebase Authentication에 등록된 사용자 목록을 표시한다
 * 사용자를 선택하면 채팅방으로 이동한다
 */
class UserListFragment : BaseFragment<FragmentUserListBinding, User>(), UserListAdapter.Listener {

    // RecyclerView Adapter
    private lateinit var userListAdapter: UserListAdapter
    private var mySelf: User? = null

    private val userListViewModel : UserListViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserListBinding {

        return FragmentUserListBinding.inflate(inflater, container, false)
    }

    override fun match(
        item: User,
        keyword: String
    ): Boolean {

        return item.name
            .lowercase()
            .contains(keyword)
    }

    override fun submitList(list: List<User>) {
        userListAdapter.submitList(list)
    }

    /**
     * 뷰가 생성된 직후 호출
     *
     * Firebase에서 사용자 목록과 내 정보 로드
     * Adapter에 데이터 전달
     * Toolbar 업데이트
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated +")

        super.onViewCreated(view, savedInstanceState)


        // Adapter 연결
        userListAdapter = UserListAdapter(this)

        binding.userRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userListAdapter
            addItemDecoration(
                RecyclerViewDecoration(
                    requireContext(),
                    0,
                    1
                )
            )
        }

        // Firebase에서 사용자 목록을 가져와서 Adapter에 전달
        userListViewModel.userList.observe(viewLifecycleOwner){ list ->
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

        //Firebase에서 사용자 정보를 가져와서 Toolbar에 표시
        userListViewModel.myInfo.observe(viewLifecycleOwner){ user ->
            setToolbar(true, getString(R.string.app_name), true)
            mySelf = user
            binding.tvMyself.text = user?.name
        }

        binding.tvMyself.setOnClickListener {
            mySelf?.let {
                findNavController().navigate(
                    R.id.action_to_ChatRoom,
                    Bundle().apply {
                        putParcelable("user_info", it)
                    })
            }
        }

        setUpMenu()

        Timber.d("onViewCreated -")

    }

    /**
     * UserListAdapter에 등록되어 있는 Listner 구현
     *
     * 사용자를 선택하면 채팅방으로 이동한다
     */
    override fun onItemClicked(user: User) {
        findNavController().navigate(
            R.id.action_to_ChatRoom,
            Bundle().apply {
                putParcelable("user_info", user)
            })
        Timber.d("onItemClick: $user")
    }
}