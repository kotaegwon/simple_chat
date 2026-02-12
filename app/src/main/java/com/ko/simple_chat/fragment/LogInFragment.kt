package com.ko.simple_chat.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ko.simple_chat.R
import com.ko.simple_chat.databinding.FragmentLoginBinding
import com.ko.simple_chat.firebase.FirebaseManager
import timber.log.Timber

class LogInFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient

    fun Fragment.toast(@StringRes resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGoogleLogin()

        binding.btnLogin.setOnClickListener(this)
        binding.btnRegister.setOnClickListener(this)
//        binding.btnGoogle.setOnClickListener(this)
        binding.imageVisibility.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun login(email: String, pwd: String) {
        FirebaseManager.auth
            .signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // 이메일 인증 확인
                    if (FirebaseManager.isEmailVerified()) {
                        FirebaseManager.loadMyUserInfo { user ->
                            if (user != null) {
                                toast("환영합니다 ${user.email}")

                                findNavController().navigate(R.id.action_to_UserList)
                            } else {
                                toast(R.string.none_user)
                            }
                        }
                    } else {
                        toast(R.string.email_not_verified)
                    }
                } else {
                    toast(R.string.login_fail)
                }
            }
    }

    private fun register(email: String, pwd: String, name: String) {
        FirebaseManager.register(email, pwd, name) { success, error ->

            if (success) {
                toast(R.string.register_success)
            } else {
                toast(R.string.register_fail)
            }
        }
    }

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

    fun showRegisterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel)
        val btnOkay = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_okay)
        val editEmail = dialogView.findViewById<EditText>(R.id.edit_register_email)
        val editPwd = dialogView.findViewById<EditText>(R.id.edit_register_pwd)
        val editName = dialogView.findViewById<EditText>(R.id.edit_register_name)
        val imageVisibility = dialogView.findViewById<ImageView>(R.id.image_visibility)

        var isVisible = false
        imageVisibility.setOnClickListener {
            isVisible = !isVisible
            if (isVisible) {
                editPwd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imageVisibility.setImageResource(R.drawable.visibility)
            } else {
                editPwd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imageVisibility.setImageResource(R.drawable.visibility_off)
            }
            // 커서 끝으로 이동
            editPwd.setSelection(editPwd.text.length)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOkay.setOnClickListener {
            val email = editEmail.text.toString().trim()
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
                var isVisible = false
                binding.imageVisibility.setOnClickListener {
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
            }


            else -> null
        }
    }
}