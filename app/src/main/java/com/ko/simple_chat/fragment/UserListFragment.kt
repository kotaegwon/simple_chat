package com.ko.simple_chat.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ko.simple_chat.BuildConfig
import com.ko.simple_chat.adapter.UserListAdapter
import com.ko.simple_chat.databinding.FragmentUserListBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.User
import timber.log.Timber
import com.ko.simple_chat.R
import com.ko.simple_chat.Utils.Def
import com.ko.simple_chat.adapter.RecyclerViewDecoration
import com.ko.simple_chat.databinding.BottomSheetFriendsBinding
import com.ko.simple_chat.databinding.BottomSheetMyselfBinding
import com.ko.simple_chat.databinding.BottomSheetProfileSettingBinding
import com.ko.simple_chat.databinding.DialogFriendReqBinding
import com.ko.simple_chat.databinding.DialogProfileImageBinding
import com.ko.simple_chat.viewmodel.UserListViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * 채팅 앱 가입자 리스트 프래그먼트
 * Firebase Authentication에 등록된 사용자 목록을 표시한다
 * 사용자를 선택하면 채팅방으로 이동한다
 */
class UserListFragment : BaseFragment<FragmentUserListBinding, User>(), UserListAdapter.Listener {

    // RecyclerView Adapter
    private lateinit var userListAdapter: UserListAdapter
    private var mySelf: User? = null

    private val userListViewModel: UserListViewModel by viewModels()

    // 사진 저장용
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    // 갤러리 선택 ActivityResultLauncher
    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Timber.d("선택된 이미지 URI: $it")
                // 여기서 이미지 처리 or 저장
                uploadSelectedProfileImage(it)
                showLoading(true)
            }
        }

    // 카메라 선택 ActivityResultLauncher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Timber.d("촬영 성공: $photoUri")
                // photoUri 사용
                uploadSelectedProfileImage(photoUri)
                showLoading(true)
            }
        }

    override fun useSearchMenu(): Boolean {
        return true
    }

    override fun useAddMenu(): Boolean {
        return true
    }

    override fun onAddMenuClicked() {
        showAddFriendDialog()
    }

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
                    2,
                    1
                )
            )
        }

        // Firebase에서 사용자 목록을 가져와서 Adapter에 전달
        userListViewModel.friendsList.observe(viewLifecycleOwner) { list ->
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
        userListViewModel.myInfo.observe(viewLifecycleOwner) { user ->
            setToolbar(true, getString(R.string.app_name), true)
            mySelf = user
            binding.tvMyself.text = user?.name

            Glide.with(requireContext())
                .load(user?.profileImageUrl)
                .placeholder(R.drawable.account_circle)
                .error(R.drawable.account_circle)
                .circleCrop()
                .into(binding.imgProfile)
        }

        binding.tvMyself.setOnClickListener {
            mySelf?.let {
                showMyselfBottomSheet(it)
            }
        }

        binding.imgProfile.setOnClickListener {
            mySelf?.let {
                showMyselfBottomSheet(it)
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
        showUserBottomSheet(user)
    }

    /**
     * 갤러리에서 사진 선택
     */
    fun showPhotoSelectionActivity() {
        photoPickerLauncher.launch("image/*")
    }

    /**
     * 임시 파일 생성(카메라 촬영용)
     */
    fun createFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().cacheDir   // cacheDir 권장
        return File.createTempFile(
            "IMG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * 카메라 촬영 Activity 호출
     */
    fun showPhotoCaptureActivity() {
        photoFile = createFile()

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
    }

    private fun uploadSelectedProfileImage(imageUri: Uri) {
        val uid = mySelf?.uid ?: return

        FirebaseManager.uploadProfileImage(uid, imageUri) { success, result ->
            requireActivity().runOnUiThread {
                if (success) {
                    Timber.d("프로필 이미지 업로드 성공: $result")
                    // 필요하면 프로필 화면 갱신
                    showLoading(false)
                } else {
                    Timber.e("프로필 이미지 업로드 실패: $result")
                }
            }
        }
    }

    private fun showMyselfBottomSheet(user: User) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetMyselfBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(binding.root)

        val imageProfile = binding.imgProfile
        val tvProfile = binding.tvProfile
        val tvChat = binding.tvChat
        val btnClose = binding.btnClose

        Glide.with(imageProfile.context)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.account_circle)
            .error(R.drawable.account_circle)
            .circleCrop()
            .into(imageProfile)

        imageProfile.setOnClickListener {
            showProfileImageDialog(user.profileImageUrl)
        }

        tvProfile.setOnClickListener {
            showProfileImageBottomSheet()

            bottomSheetDialog.dismiss()
        }

        tvChat.setOnClickListener {
            bottomSheetDialog.dismiss()

            findNavController().navigate(
                R.id.action_to_ChatRoom,
                Bundle().apply {
                    putParcelable(Def.Intent.USER_INFO, user)
                }
            )
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showUserBottomSheet(user: User) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetFriendsBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(binding.root)

        val menu1 = binding.layoutMenu1
        val menu2 = binding.layoutMenu2
        val imageProfile = binding.imgProfile
        val tvChat = binding.tvChat
        val tvDelete = binding.tvDelete
        val btnClose = binding.btnClose
        val btnDeleteYes = binding.btnDeleteYes
        val btnDeleteNo = binding.btnDeleteNo

        Glide.with(imageProfile.context)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.account_circle)
            .error(R.drawable.account_circle)
            .circleCrop()
            .into(imageProfile)

        imageProfile.setOnClickListener {
            showProfileImageDialog(user.profileImageUrl)
        }

        tvChat.setOnClickListener {
            bottomSheetDialog.dismiss()

            findNavController().navigate(
                R.id.action_to_ChatRoom,
                Bundle().apply {
                    putParcelable(Def.Intent.USER_INFO, user)
                }
            )
        }

        tvDelete.setOnClickListener {
            menu1.visibility = View.GONE
            menu2.visibility = View.VISIBLE
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnDeleteYes.setOnClickListener {
            FirebaseManager.deleteFriend(user) { success, message ->
                Timber.d("showUserBottomSheet: $success, $message")
            }
            bottomSheetDialog.dismiss()
        }

        btnDeleteNo.setOnClickListener {
            menu1.visibility = View.VISIBLE
            menu2.visibility = View.GONE
        }

        bottomSheetDialog.show()
    }

    private fun showProfileImageBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetProfileSettingBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(binding.root)

        val tvCamera = binding.tvCamera
        val tvAlbum = binding.tvAlbum
        val btnClose = binding.btnClose

        tvCamera.setOnClickListener {
            showPhotoCaptureActivity()
            bottomSheetDialog.dismiss()
        }

        tvAlbum.setOnClickListener {
            showPhotoSelectionActivity()
            bottomSheetDialog.dismiss()
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showProfileImageDialog(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) return

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val binding = DialogProfileImageBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        val imageView = binding.imgProfileFull

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.account_circle)
            .error(R.drawable.account_circle)
            .into(imageView)

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddFriendDialog() {
        val binding = DialogFriendReqBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        val editName = binding.editReqName
        val editEmail = binding.editReqEmail
        val btnCancel = binding.btnDialogCancel
        val btnOkay = binding.btnDialogOkay

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOkay.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()

            FirebaseManager.sendFriendRequestByNameAndEmail(name, email) { success, message ->
                Timber.d("showAddFriendDialog: $success, $message")
            }

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}