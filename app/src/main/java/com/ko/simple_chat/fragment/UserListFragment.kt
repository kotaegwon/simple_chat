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


/**
 * 채팅 앱 가입자 리스트 프래그먼트
 * Firebase Authentication에 등록된 사용자 목록을 표시한다
 * 사용자를 선택하면 채팅방으로 이동한다
 */
class UserListFragment : Fragment(), UserListAdapter.Listner {

    // ViewBinding
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter
    private lateinit var adapter: UserListAdapter

    // Activity 범위 ViewModel - 툴바 설정용
    val viewModel: ToolbarViewModel by activityViewModels()

    /**
     * 프래그먼트의 레이아웃을 inflate하고 초기 UI 설정
     *
     * RecyclerView 레이아웃매니저 설정
     * UserListAdapter 설정
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView +")
        _binding = FragmentUserListBinding.inflate(inflater, container, false)

        // RecyclerView 레이아웃 매니저 설정
        val layoutManager = LinearLayoutManager(requireContext())
        binding.userRecyclerview.layoutManager = layoutManager

        // Adapter 연결
        adapter = UserListAdapter(this)
        binding.userRecyclerview.adapter = adapter

        Timber.d("onCreateView -")

        return binding.root
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

        // Firebase에서 사용자 목록을 가져와서 Adapter에 전달
        FirebaseManager.loadUserList { list ->
            adapter.submitList(list)
        }

        //Firebase에서 사용자 정보를 가져와서 Toolbar에 표시
        FirebaseManager.loadMyUserInfo { user ->
            viewModel.setToolbar(true, user?.name ?: "")
        }

        Timber.d("onViewCreated -")

    }

    /**
     * 프래그먼트의 뷰가 소멸될 때 호출
     * 
     * 메모리 누수를 방지하기 위해 binding 해제
     */
    override fun onDestroyView() {
        Timber.d("onDestroyView +")

        super.onDestroyView()
        _binding = null

        Timber.d("onDestroyView -")
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