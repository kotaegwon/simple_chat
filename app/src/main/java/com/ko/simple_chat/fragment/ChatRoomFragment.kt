package com.ko.simple_chat.fragment

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ko.simple_chat.BuildConfig
import com.ko.simple_chat.R
import com.ko.simple_chat.Utils.Def
import com.ko.simple_chat.Utils.Utils
import com.ko.simple_chat.adapter.ChatRoomAdapter
import com.ko.simple_chat.adapter.ChatTypeItem
import com.ko.simple_chat.databinding.BottomSheetProfileSettingBinding
import com.ko.simple_chat.databinding.DialogProfileImageBinding
import com.ko.simple_chat.databinding.FragmentChatRoomBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.model.ChatRoom
import com.ko.simple_chat.model.User
import com.ko.simple_chat.viewmodel.ChatViewModel
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 채팅방 프래그먼트
 * Firebase Authentication에 등록된 사용자 목록을 표시한다
 * 사용자를 선택하면 채팅방으로 이동한다
 * 송신 메시지와 수신 메시지를 표시한다
 * 송신 버튼을 누르면 메시지를 전송한다
 */
class ChatRoomFragment : BaseFragment<FragmentChatRoomBinding, ChatRoom>(), View.OnClickListener,
    ChatRoomAdapter.Listener {

    // RecyclerView Adapter
    private lateinit var chatAdapter: ChatRoomAdapter
    var user: User? = null


    // ChatViewModel
    val chatViewModel: ChatViewModel by viewModels()

    // 나의 UID와 상대방의 UID
    var myUid: String = ""
    var otherUid: String = ""

    // 사진 저장용
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private var selectedImageUri: Uri? = null

    // 갤러리 선택 ActivityResultLauncher
    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Timber.d("선택된 이미지 URI: $it")
                // 여기서 이미지 처리 or 저장
                selectedImageUri = it
                showPreviewImage(it)
            }
        }

    // 카메라 선택 ActivityResultLauncher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Timber.d("촬영 성공: $photoUri")
                // photoUri 사용
                selectedImageUri = photoUri
                showPreviewImage(photoUri)
            }
        }

    override fun useSearchMenu(): Boolean {
        return true
    }

    override fun useAddMenu(): Boolean {
        return false
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatRoomBinding {
        return FragmentChatRoomBinding.inflate(inflater, container, false)
    }

    override fun match(
        item: ChatRoom,
        keyword: String
    ): Boolean {
        return item.message
            .lowercase()
            .contains(keyword)
    }

    override fun submitList(list: List<ChatRoom>) {
        var lastDate: String? = null
        val uiList = mutableListOf<ChatTypeItem>()

        list.forEach { chat ->
            val currentDate = Utils.formatDateHeader(chat.time)

            if (lastDate != currentDate) {
                uiList.add(ChatTypeItem.Date(currentDate))
                lastDate = currentDate
            }

            if (chat.type == Def.MessageType.TEXT) {
                if (chat.myUid == myUid) {
                    uiList.add(ChatTypeItem.Send(chat))
                } else {
                    uiList.add(ChatTypeItem.Receive(chat))
                }
            } else {
                if (chat.myUid == myUid) {
                    uiList.add(ChatTypeItem.SendImage(chat))
                } else {
                    uiList.add(ChatTypeItem.ReceiveImage(chat))
                }
            }
        }

        chatAdapter.submitList(uiList)

        if (uiList.isNotEmpty()) {
            binding.mainRecyclerview.scrollToPosition(uiList.size - 1)
        }
    }

    /**
     * 뷰가 생성된 직후 호출
     * Firebase에서 사용자 목록과 내 정보 로드
     * 채팅방 메시지 관찰 및 Adapter에 적용
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        chatAdapter = ChatRoomAdapter(this)

        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter

        }

        user = getArgument()

//        uiAdapterTest()
        otherUid = user!!.uid

        myUid = FirebaseManager.auth.currentUser!!.uid

        binding.btnSend.setOnClickListener(this)
        binding.btnImage.setOnClickListener(this)
        binding.btnRemoveImage.setOnClickListener(this)
        binding.imgPreview.setOnClickListener(this)

        // 채팅방 ID 생성
        chatViewModel.listenChat(otherUid)

        // 특정 id의 채팅 내역을 listenChat으로 관찰
        chatViewModel.chatList.observe(viewLifecycleOwner) { list ->
            originList.apply {
                clear()
                addAll(list)
            }

            filterList.apply {
                clear()
                addAll(list)
            }
            submitList(list)
        }

        setUpMenu()

    }

    /**
     * UserListFragment, MainActivity로 부터 User 클래스를 전달 받음
     */
    private fun getArgument(): User {
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(
                Def.Intent.USER_INFO,
                User::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Def.Intent.USER_INFO)
        }

        Timber.d("getArgument: $user")

        setToolbar(true, user!!.name, true)

        return user
    }

    /**
     * 리사이클러뷰 송수신 레이아웃 더미 데이터
     */
    private fun uiAdapterTest() {
        val testList = mutableListOf<ChatTypeItem>()
        val sendData = ChatRoom(
            myUid = "0",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        val receive = ChatRoom(
            myUid = "1",
            name = "고태권",
            message = "안녕하세요",
            time = System.currentTimeMillis()
        )

        testList.add(ChatTypeItem.Send(sendData))
        testList.add(ChatTypeItem.Receive(receive))

        chatAdapter.submitList(testList)
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

    private fun showPreviewImage(uri: Uri) {
        binding.previewLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .into(binding.imgPreview)
    }

    private fun clearPreviewImage() {
        selectedImageUri = null

        if (view == null) return

        binding.imgPreview.setImageDrawable(null)
        binding.previewLayout.visibility = View.GONE
    }

    private fun showPreviewImageDialog(imageUrl: Uri?) {
        if (imageUrl == null) return

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val binding = DialogProfileImageBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        val imageView = binding.imgProfileFull

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_visibility_off)
            .error(R.drawable.visibility_off)
            .into(imageView)

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send -> {
                val msg = binding.editMessage.text.toString().trim()
                val imageUri = selectedImageUri

                user?.let { targetUser ->
                    when {
                        imageUri != null -> {
                            showLoading(true)

                            FirebaseManager.sendImageMessage(
                                otherUid = targetUser.uid,
                                imageUri = imageUri
                            ) { success, errorMessage ->
                                if(view == null || !isAdded) return@sendImageMessage

                                if (success) {
                                    clearPreviewImage()
                                    binding.editMessage.text.clear()
                                    showLoading(false)
                                } else {
                                    showLoading(false)
                                    toast(errorMessage ?: getString(R.string.send_fail))
                                }
                            }
                        }

                        msg.isNotEmpty() -> {
                            FirebaseManager.sendMessage(
                                otherUid = targetUser.uid,
                                message = msg
                            ) { success ->
                                if (success) {
                                    binding.editMessage.text.clear()
                                } else {
                                    toast(R.string.send_fail)
                                }
                            }
                        }
                    }
                }
            }

            R.id.btn_image -> {
                showProfileImageBottomSheet()
            }

            R.id.btn_remove_image -> {
                clearPreviewImage()
            }

            R.id.img_preview -> {
                showPreviewImageDialog(selectedImageUri)
            }
        }
    }

    override fun onImageClick(imageUri: String) {
        if (view == null) return
        showPreviewImageDialog(imageUri.toUri())
    }
}