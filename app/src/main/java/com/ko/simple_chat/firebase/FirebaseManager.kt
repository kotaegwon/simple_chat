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
import com.ko.simple_chat.model.Chat
import com.ko.simple_chat.model.User

/**
 * Firebase 인증, 데이터베이스, 스토리지 관리
 *
 * 로그인, 회원가입, 로그아웃, 사용자 정보 가져오기
 * 방 만들기, 메시지 송신, 메시지 수신
 */
object FirebaseManager {

    // Firebase 인증 객체
    lateinit var auth: FirebaseAuth
        private set

    // Firebase DB 객체
    lateinit var db: FirebaseFirestore
        private set

    // FirebaseStorage 객체
    lateinit var storage: FirebaseStorage
        private set


    /**
     * Firebase 초기화
     *
     * 반드시 앱 시작 시 1회 호출 필요
     */
    fun init() {
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage
    }

    /**
     * 로그인 상태 확인
     *
     * @return 로그인 상태 여부
     */
    fun isLogin(): Boolean {
        return auth.currentUser != null
    }

    /**
     * 로그아웃
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * 이메일 확인
     *
     * @return 이메일 확인 여부
     */
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    /**
     * 이메일 인증 확인
     *
     * @return 이메일 인증 여부
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    /**
     * 회원 가입
     *
     * @param email 이메일
     * @param pwd 비밀번호
     * @param name 이름
     *
     * @return onResult 완료 콜백(성공 여부, 에러 메시지)
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
     * Firestore에 사용자 정보 저장
     *
     * @param firebaseUser Firebase 사용자 정보
     * @param email 이메일
     * @param name 이름
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
     *
     * @param onResult 완료 콜백(사용자 정보)
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
     *
     * @param onResult 완료 콜백(사용자 목록)
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


    /**
     * 두 사람 uid로 방 만들기
     *
     * @param myUid 내 uid
     * @param otherUid 상대방 uid
     *
     * @return 방 uid
     */
    fun makeRoom(myUid: String, otherUid: String): String {
        return if (myUid < otherUid) {
            "${myUid}_${otherUid}"
        } else {
            "${otherUid}_${myUid}"
        }
    }

    /**
     * 메시지 송신
     *
     * @param otherUid 상대방 uid
     * @param message 메시지
     * @param onResult 완료 콜백(성공 여부)
     */
    fun sendMessage(otherUid: String, message: String, onResult: (Boolean) -> Unit) {

        val myUid = auth.currentUser?.uid ?: return
        val roomUid = makeRoom(myUid, otherUid)

        val chat = Chat(
            myUid = myUid,
            otherUid = otherUid,
            message = message,
            time = System.currentTimeMillis()
        )

        // Firestore에서 'chatRooms' 컬렉션 내 방 ID(roomUid)에 해당하는 문서 참조 생성
        val roomRef = db.collection("chatRooms").document(roomUid)

        /**
         * 방 정보
         *
         * users: 두 사용자의 uid
         * lastMessage: 마지막 메시지
         * updateAt: 마지막 메시지 전송 시간(정렬, 최신 순 표시용)
         */
        val roomData = hashMapOf(
            "users" to listOf(myUid, otherUid),
            "lastMessage" to message,
            "updateAt" to chat.time
        )

        /**
         * 방이 없으면 생성
         * 방이 있으면 업데이트
         */
        roomRef.set(roomData)

        // messages 컬렉션 안에 메시지 저장
        roomRef.collection("messages")
            .add(chat)
            .addOnSuccessListener {
                onResult(true)
            }.addOnFailureListener {
                onResult(false)
            }
    }

    /**
     * 메시지 수신
     *
     * @param otherUid 상대방 uid
     * @param onResult 완료 콜백(메시지 목록)
     */

    fun listenMessage(otherUid: String, onResult: (List<Chat>) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        val roomId = makeRoom(myUid, otherUid)

        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("time")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val list = mutableListOf<Chat>()

                for (doc in snapshot.documents) {
                    val chat = doc.toObject(Chat::class.java)
                    chat?.let { list.add(it) }
                }
                onResult(list)
            }
    }

}
