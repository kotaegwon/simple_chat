package com.ko.simple_chat.firebase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.ko.simple_chat.model.User

object FirebaseManager {
    lateinit var auth: FirebaseAuth
        private set

    lateinit var db: FirebaseFirestore
        private set

    lateinit var storage: FirebaseStorage
        private set

    fun init() {
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage
    }

    fun isLogin(): Boolean {
        return auth.currentUser != null
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    /**
     * 회원 가입
     */
    fun register(email: String, pwd: String, name: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 이메일 인증 발송
                    auth.currentUser?.sendEmailVerification()

                    // Firestore 저장
                    saveUserToFireStore(auth.currentUser, email, name)

                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)

                }
            }
    }

    /**
     * 사용자 정보 저장
     */
    private fun saveUserToFireStore(firebaseUser: FirebaseUser?, email: String, name: String) {
        firebaseUser ?: return

        val user = User(
            uid = firebaseUser.uid,
            email = email,
            name = name
        )

        db.collection("users")
            .document(firebaseUser.uid)
            .set(user)
    }

    /**
     * 로그인 성공 후 DB에서 정보 가져오기
     */
    fun loadMyUserInfo(onResult: (User?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                onResult(user)
            }
    }

    /**
     * 구글 로그인
     */
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 이메일 인증 확인
                    val user = auth.currentUser

                    saveGoogleUser(user)

                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * 구글 사용자 저장
     */
    fun saveGoogleUser(user: FirebaseUser?) {
        user ?: return

        val data = User(
            uid = user.uid,
            email = user.email ?: ""
        )

        db.collection("users")
            .document(user.uid)
            .set(data)
    }

    /**
     * 전체 유저 목록 가져오기
     */
    fun loadUserList(onResult: (List<User>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<User>()

                for (document in result) {
                    val user = document.toObject(User::class.java)
                    list.add(user)
                }

                onResult(list)
            }
    }
}
