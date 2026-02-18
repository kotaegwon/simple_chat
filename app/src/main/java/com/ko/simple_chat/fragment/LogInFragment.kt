package com.ko.simple_chat.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.ko.simple_chat.R
import com.ko.simple_chat.databinding.DialogRegisterBinding
import com.ko.simple_chat.databinding.FragmentLoginBinding
import com.ko.simple_chat.firebase.FirebaseManager
import com.ko.simple_chat.firebase.LoginResult
import kotlin.getValue

/**
 * 로그인 프래그먼트
 * Firebase Authentication을 사용하여 로그인을 처리한다
 * 로그인 성공 시 UserListFragment로 이동한다
 */
class LogInFragment : Fragment(), View.OnClickListener {

    // ViewBinding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient

    private var isVisible = false

    private lateinit var authListener: FirebaseAuth.AuthStateListener


    // string.xml resource toast
    fun Fragment.toast(@StringRes resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    // string.xml string toast
    fun Fragment.toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                e.printStackTrace()
                toast(R.string.google_login_fail)
            }
        }

    /**
     * 프래그먼트의 레이아웃을 inflate하고 초기 UI 설정
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    /**
     * 뷰가 생성된 직후 호출
     *
     * 클릭 리스너 설정
     * Toolbar 숨김
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        initGoogleLogin()

        binding.btnLogin.setOnClickListener(this)
        binding.btnRegister.setOnClickListener(this)
        binding.imageVisibility.setOnClickListener(this)
        binding.btnLogout.setOnClickListener(this)
        binding.btnStartTalk.setOnClickListener(this)
//        binding.btnGoogle.setOnClickListener(this)

        authListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null) {
                updateUi(false)
            } else {
                updateUi(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseManager.auth.addAuthStateListener(authListener)
    }

    override fun onPause() {
        super.onPause()
        FirebaseManager.auth.removeAuthStateListener(authListener)
    }

    /**
     * 프래그먼트의 뷰가 소멸될 때 호출
     * 메모리 누수를 방지하기 위해 binding 해제
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 로그인
     *
     * Firebase Authentication을 사용하여 로그인을 처리한다
     * 로그인 성공 시 UserListFragment로 이동한다
     *
     * @param email 사용자 이메일
     * @param pwd 사용자 비밀번호
     *
     * @return 로그인 성공 시 true, 실패 시 false
     */
    private fun login(email: String, pwd: String) {
        FirebaseManager.login(email, pwd) { result ->
            when (result) {
                is LoginResult.Success -> {
                    toast(getString(R.string.login_completed, result.user.name))

                    findNavController().navigate(R.id.action_to_UserList)
                }

                is LoginResult.NotVerified -> {
                    toast(R.string.email_not_verified)
                }

                is LoginResult.NoneUser -> {
                    toast(R.string.none_user)
                }

                is LoginResult.Fail -> {
                    toast(R.string.login_fail)
                }
            }
        }
    }

    /**
     * 회원가입
     *
     * Firebase Authentication을 사용하여 회원가입을 처리한다
     * 회원가입 성공 시 UserListFragment로 이동한다
     *
     * @param email 사용자 이메일
     * @param pwd 사용자 비밀번호
     * @param name 사용자 이름
     */
    private fun register(email: String, pwd: String, name: String) {
        FirebaseManager.register(email, pwd, name) { success, error ->

            if (success) {
                toast(R.string.register_success)
            } else {
                toast(R.string.register_fail)
            }
        }
    }

    /**
     * 구글 로그인
     *
     * GoogleSignInOptions을 사용하여 구글 로그인을 처리한다
     * 로그인 성공 시 UserListFragment로 이동한다
     */
    private fun initGoogleLogin() {
        val gos = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(
            requireContext(),
            gos
        )
    }

    /**
     * 구글 로그인 인증
     *
     * GoogleSignInAccount을 사용하여 구글 로그인 인증을 처리한다
     * 로그인 성공 시 UserListFragment로 이동한다
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        FirebaseManager.firebaseAuthWithGoogle(account) { success, error ->
            if (success) {
                toast(R.string.google_login_success)

                findNavController().navigate(R.id.action_to_UserList)
            } else {
                toast(R.string.google_login_fail)
            }
        }
    }

    /**
     * 회원가입 다이얼로그 표시
     *
     * AlertDialog를 사용하여 회원가입 다이얼로그를 표시한다
     * 다이얼로그에서 회원가입을 처리한다
     * 회원가입 성공 시 UserListFragment로 이동한다
     */
    fun showRegisterDialog() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        val editEmil = binding.editRegisterEmail
        val editPwd = binding.editRegisterPwd
        val editName = binding.editRegisterName
        val imageVisibility = binding.imageVisibility
        val btnCancel = binding.btnDialogCancel
        val btnOkay = binding.btnDialogOkay

        var isVisible = false
        imageVisibility.setOnClickListener {
            isVisible = !isVisible
            if (isVisible) {
                editPwd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.imageVisibility.setImageResource(R.drawable.visibility)
            } else {
                editPwd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.imageVisibility.setImageResource(R.drawable.visibility_off)
            }
            // 커서 끝으로 이동
            editPwd.setSelection(binding.editRegisterPwd.text.length)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOkay.setOnClickListener {
            val email = editEmil.text.toString().trim()
            val pwd = editPwd.text.toString().trim()
            val name = editName.text.toString().trim()

            when {
                email.isEmpty() || pwd.isEmpty() -> {
                    toast(R.string.edit_email_pwd)
                    return@setOnClickListener
                }

                pwd.length < 6 -> {
                    toast(R.string.pwd_length)
                    return@setOnClickListener
                }

                else -> register(email, pwd, name)
            }
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun toggleVisibility() {
        isVisible = !isVisible

        if (isVisible) {
            binding.editPwd.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.imageVisibility.setImageResource(R.drawable.visibility)
        } else {
            binding.editPwd.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.imageVisibility.setImageResource(R.drawable.visibility_off)
        }
        binding.editPwd.setSelection(binding.editPwd.text.length)
    }

    private fun updateUi(isLogin: Boolean) {

        binding.apply {

            btnLogout.visibility = if (isLogin) View.VISIBLE else View.GONE
            btnStartTalk.visibility = if (isLogin) View.VISIBLE else View.GONE
            tvLoginInfo.visibility = if (isLogin) View.VISIBLE else View.GONE

            btnLogin.visibility = if (isLogin) View.GONE else View.VISIBLE
            btnRegister.visibility = if (isLogin) View.GONE else View.VISIBLE
            editEmail.visibility = if (isLogin) View.GONE else View.VISIBLE
            editPwd.visibility = if (isLogin) View.GONE else View.VISIBLE
            imageVisibility.visibility = if (isLogin) View.GONE else View.VISIBLE
        }
    }

    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.app_name)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * 뷰 클릭 이벤트를 처리한다
     *
     * 클릭된 뷰의 ID에 따라 다른 동작을 수행합니다.
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                val email = binding.editEmail.text.toString()
                val pwd = binding.editPwd.text.toString()

                when {
                    email.isEmpty() && !pwd.isEmpty() -> toast(R.string.edit_email)
                    pwd.isEmpty() && !email.isEmpty() -> toast(R.string.edit_pwd)
                    email.isEmpty() && pwd.isEmpty() -> toast(R.string.edit_email_pwd)

                    else -> login(email, pwd)
                }
            }

            R.id.btn_register -> {
                showRegisterDialog();
            }

//            R.id.btn_google -> {
//                val signInIntent = googleSignInClient.signInIntent
//                googleLauncher.launch(signInIntent)
//            }

            R.id.image_visibility -> {
                toggleVisibility()
            }

            R.id.btn_logout -> {
                FirebaseManager.logout()
            }

            R.id.btn_start_talk -> {
                findNavController().navigate(R.id.action_to_UserList)
            }
        }
    }
}