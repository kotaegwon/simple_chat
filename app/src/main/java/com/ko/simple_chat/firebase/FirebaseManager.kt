package com.ko.simple_chat.firebase

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.ko.simple_chat.Utils.Def
import com.ko.simple_chat.model.ChatList
import com.ko.simple_chat.model.ChatListItem
import com.ko.simple_chat.model.ChatRoom
import com.ko.simple_chat.model.User
import timber.log.Timber

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
     * 로그인
     *
     * @param email 이메일
     * @param pwd 비밀번호
     * @param onResult 완료 콜백(로그인 결과)
     */
    fun login(email: String, pwd: String, onResult: (LoginResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                // 로그인 요청 실패 처리
                if (!task.isSuccessful) {
                    onResult(LoginResult.Fail)
                    return@addOnCompleteListener
                }

                // 이메일 인증 여부 확인
                if (!isEmailVerified()) {
                    onResult(LoginResult.NotVerified)
                    return@addOnCompleteListener
                }

                // 로그인 성공 후 사용자 정보 조회
                loadMyUserInfo { user ->
                    if (user != null) {
                        onResult(LoginResult.Success(user))
                    } else {
                        onResult(LoginResult.NoneUser)
                    }
                }
            }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        auth.signOut()
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
    fun register(
        email: String,
        pwd: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
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

        db.collection(Def.Collection.USERS)
            .document(firebaseUser.uid)
            .set(user)
    }

    /**
     * 내 정보 가져오기
     *
     * @param onResult 완료 콜백(사용자 정보)
     */
    fun loadMyUserInfo(onResult: (User?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                onResult(user)
            }
    }


    /**
     * 구글 로그인
     */
    fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onResult: (Boolean, String?) -> Unit
    ) {
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

        db.collection(Def.Collection.USERS)
            .document(user.uid)
            .set(data)
    }

    /**
     * 전체 유저 목록 가져오기
     *
     * @param onResult 완료 콜백(사용자 목록)
     */
    fun loadUserList(onResult: (List<User>) -> Unit) {
        db.collection(Def.Collection.USERS)
            .addSnapshotListener { snapshot, _ ->

                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(User::class.java) }
                    ?.filter { it.uid != auth.currentUser?.uid }
                    ?: emptyList()

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

        loadMyUserInfo { user ->
            val name = user?.name ?: ""

            val chat = ChatRoom(
                myUid = myUid,
                otherUid = otherUid,
                name = name,
                message = message,
                imageUrl = "",
                type = Def.MessageType.TEXT,
                time = System.currentTimeMillis()
            )

            // Firestore에서 'chatRooms' 컬렉션 내 방 ID(roomUid)에 해당하는 문서 참조 생성
            val roomRef = db.collection(Def.Collection.CHAT_ROOMS)
                .document(roomUid)

            /**
             * 방 정보
             *
             * users: 두 사용자의 uid
             * lastMessage: 마지막 메시지
             * updateAt: 마지막 메시지 전송 시간(정렬, 최신 순 표시용)
             */
            val roomData = hashMapOf(
                Def.Collection.USERS to listOf(myUid, otherUid),
                Def.ChatRoomsFields.LAST_MESSAGE to message,
                Def.ChatRoomsFields.UPDATE_AT to chat.time
            )

            /**
             * 방이 없으면 생성
             * 방이 있으면 업데이트
             */
            roomRef.set(roomData)

            // messages 컬렉션 안에 메시지 저장
            roomRef.collection(Def.Collection.CHAT_ROOMS_MESSAGES)
                .add(chat)
                .addOnSuccessListener {
                    onResult(true)
                }.addOnFailureListener {
                    onResult(false)
                }
        }
    }

    fun sendImageMessage(otherUid: String, imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return
        val roomUid = makeRoom(myUid, otherUid)

        loadMyUserInfo { user ->
            val name = user?.name ?: ""
            val fileName = "${System.currentTimeMillis()}_${myUid}.jpg"
            val imageRef = storage.reference.child("chatImages/$roomUid/$fileName")

            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            val chat = ChatRoom(
                                myUid = myUid,
                                otherUid = otherUid,
                                name = name,
                                message = "",
                                imageUrl = downloadUri.toString(),
                                type = Def.MessageType.IMAGE,
                                time = System.currentTimeMillis()
                            )

                            val roomRef = db.collection(Def.Collection.CHAT_ROOMS)
                                .document(roomUid)

                            val roomData = hashMapOf(
                                Def.Collection.USERS to listOf(myUid, otherUid),
                                Def.ChatRoomsFields.LAST_MESSAGE to "[이미지]",
                                Def.ChatRoomsFields.UPDATE_AT to chat.time
                            )

                            roomRef.set(roomData, SetOptions.merge())

                            roomRef.collection(Def.Collection.CHAT_ROOMS_MESSAGES)
                                .add(chat)
                                .addOnSuccessListener {
                                    onResult(true, null)
                                }
                                .addOnFailureListener { e ->
                                    onResult(false, e.message)
                                }
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message)
                        }
                }
                .addOnFailureListener { e ->
                    onResult(false, e.message)
                }
        }
    }

    /**
     * 채팅 메시지 실시간 동기화
     *
     * @param otherUid 상대방 uid
     * @param onResult 완료 콜백(메시지 목록)
     */

    fun listenMessage(otherUid: String, onResult: (List<ChatRoom>) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        val roomId = makeRoom(myUid, otherUid)

        db.collection(Def.Collection.CHAT_ROOMS)
            .document(roomId)
            .collection(Def.Collection.CHAT_ROOMS_MESSAGES)
            .orderBy(Def.MessageFields.TIME)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val list = mutableListOf<ChatRoom>()

                for (doc in snapshot.documents) {
                    val chat = doc.toObject(ChatRoom::class.java)
                    chat?.let { list.add(it) }
                }
                onResult(list)
            }
    }

    fun loadChatList(onResult: (List<ChatListItem>) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIENDS)
            .get()
            .addOnSuccessListener { friendSnapshot ->

                val friendUidSet = friendSnapshot.documents
                    .mapNotNull { it.getString(Def.UsersFields.UID) }
                    .toSet()

                db.collection(Def.Collection.CHAT_ROOMS)
                    .whereArrayContains(Def.Collection.USERS, myUid)
                    .orderBy(Def.ChatRoomsFields.UPDATE_AT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->

                        if (e != null) {
                            Timber.e(e, "loadChatList listen error")
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            Timber.d("loadChatList snapshot is null")
                            return@addSnapshotListener
                        }

                        val basic = snapshot.documents.mapNotNull { doc ->
                            val room = doc.toObject(ChatList::class.java) ?: return@mapNotNull null
                            val otherUid = room.users.firstOrNull { it != myUid } ?: myUid

                            val isMyChat = otherUid == myUid
                            val isFriendChat = otherUid in friendUidSet

                            if (!isMyChat && !isFriendChat) {
                                return@mapNotNull null
                            }

                            Triple(doc.id, otherUid, room)
                        }

                        if (basic.isEmpty()) {
                            onResult(emptyList())
                            return@addSnapshotListener
                        }

                        val result = mutableListOf<ChatListItem>()
                        var remain = basic.size

                        basic.forEach { (roomId, otherUid, room) ->
                            db.collection(Def.Collection.USERS)
                                .document(otherUid)
                                .get()
                                .addOnSuccessListener { userSnap ->
                                    val otherName =
                                        userSnap.getString(Def.UsersFields.NAME) ?: ""
                                    val profileImageUrl =
                                        userSnap.getString(Def.UsersFields.PROFILE_IMAGES) ?: ""

                                    result.add(
                                        ChatListItem(
                                            roomId = roomId,
                                            otherUid = otherUid,
                                            otherName = otherName,
                                            lastMessage = room.lastMessage,
                                            updateAt = room.updateAt,
                                            profileImageUrl = profileImageUrl
                                        )
                                    )

                                    remain--
                                    if (remain == 0) {
                                        onResult(result.sortedByDescending { it.updateAt })
                                    }
                                }
                                .addOnFailureListener { error ->
                                    Timber.e(error, "loadChatList user load fail: $otherUid")

                                    result.add(
                                        ChatListItem(
                                            roomId = roomId,
                                            otherUid = otherUid,
                                            otherName = if (otherUid == myUid) "나" else "(불러오기 실패)",
                                            lastMessage = room.lastMessage,
                                            updateAt = room.updateAt,
                                            profileImageUrl = ""
                                        )
                                    )

                                    remain--
                                    if (remain == 0) {
                                        onResult(result.sortedByDescending { it.updateAt })
                                    }
                                }
                        }
                    }
            }
            .addOnFailureListener { error ->
                Timber.e(error, "friend list load fail")

                // 친구 목록을 못 읽어도 self chat만 보여주기
                db.collection(Def.Collection.CHAT_ROOMS)
                    .whereArrayContains(Def.Collection.USERS, myUid)
                    .orderBy(Def.ChatRoomsFields.UPDATE_AT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->

                        if (e != null) {
                            Timber.e(e, "loadChatList fallback listen error")
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            Timber.d("loadChatList fallback snapshot is null")
                            return@addSnapshotListener
                        }

                        val basic = snapshot.documents.mapNotNull { doc ->
                            val room = doc.toObject(ChatList::class.java) ?: return@mapNotNull null
                            val otherUid = room.users.firstOrNull { it != myUid } ?: myUid

                            if (otherUid != myUid) {
                                return@mapNotNull null
                            }

                            Triple(doc.id, otherUid, room)
                        }

                        if (basic.isEmpty()) {
                            onResult(emptyList())
                            return@addSnapshotListener
                        }

                        val result = basic.map { (roomId, otherUid, room) ->
                            ChatListItem(
                                roomId = roomId,
                                otherUid = otherUid,
                                otherName = "나",
                                lastMessage = room.lastMessage,
                                updateAt = room.updateAt,
                                profileImageUrl = ""
                            )
                        }

                        onResult(result.sortedByDescending { it.updateAt })
                    }
            }
    }

    fun updateMyFcmToken(token: String) {
        Timber.d("updateMyFcmToken: $token")

        val uid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(uid)
            .update(Def.UsersFields.FCM_TOKEN, token)
    }

    fun updateMyFcmTokenLoginSuccess() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener

            FirebaseFirestore.getInstance().collection(Def.Collection.USERS)
                .document(uid)
                .set(mapOf(Def.UsersFields.FCM_TOKEN to token), SetOptions.merge())
        }
    }

    fun uploadProfileImage(uid: String, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        val imageRef = storage.reference.child("profileImageUrl/$uid")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val newProfileUrl = downloadUri.toString()

                        db.collection(Def.Collection.USERS)
                            .document(uid)
                            .update(Def.UsersFields.PROFILE_IMAGES, newProfileUrl)
                            .addOnSuccessListener {
                                updateFriendProfileImage(uid, newProfileUrl) { success, error ->
                                    if (success) {
                                        onResult(true, newProfileUrl)
                                    } else {
                                        onResult(false, error)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    private fun updateFriendProfileImage(
        myUid: String,
        newProfileUrl: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIENDS)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                snapshot.documents.forEach { doc ->
                    val friendUid = doc.getString(Def.UsersFields.UID) ?: return@forEach

                    val otherFriendRef = db.collection(Def.Collection.USERS)
                        .document(friendUid)
                        .collection(Def.Collection.FRIENDS)
                        .document(myUid)

                    batch.update(otherFriendRef, Def.UsersFields.PROFILE_IMAGES, newProfileUrl)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * 이름 + 이메일로 사용자 찾기
     *
     * 친구 추가 시 사용, 정확히 일치하는 사용자를 찾기 위함
     */
    fun findUserByNameAndEmail(name: String, email: String, onResult: (User?) -> Unit) {
        db.collection(Def.Collection.USERS)
            .whereEqualTo(Def.UsersFields.NAME, name)
            .whereEqualTo(Def.UsersFields.EMAIL, email)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.documents.firstOrNull()?.toObject(User::class.java)
                onResult(user)
            }
            .addOnFailureListener { snapshot ->
                onResult(null)
            }
    }

    /**
     * 이름 + 이메일로 친구 요청 보내기
     */
    fun sendFriendRequestByNameAndEmail(
        name: String,
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        findUserByNameAndEmail(name, email) { user ->
            if (user == null) {
                onResult(false, "사용자를 찾을 수 없습니다.")
                return@findUserByNameAndEmail
            }
            sendFriendRequest(user, onResult)
        }
    }

    /**
     * 친구 요청 보내기
     *
     * 상대방의 users/{targetUid}/friendRequests/{myUid}에 요청 정보 저장
     */
    fun sendFriendRequest(targetUser: User, onResult: (Boolean, String?) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        if (myUid == targetUser.uid) {
            onResult(false, "자기 자신에게 친구 요청을 보낼 수 없습니다.")
            return
        }

        // 먼저 내 정보 조회
        db.collection(Def.Collection.USERS)
            .document(myUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val myUser = snapshot.toObject(User::class.java)

                if (myUser == null) {
                    onResult(false, "내 정보를 가져올 수 없습니다.")
                    return@addOnSuccessListener
                }

                // 이미 친구인지 확인
                db.collection(Def.Collection.USERS)
                    .document(myUid)
                    .collection(Def.Collection.FRIENDS)
                    .document(targetUser.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            onResult(false, "이미 친구입니다.")
                            return@addOnSuccessListener
                        }

                        // 이미 요청 보냈는지 확인
                        db.collection(Def.Collection.USERS)
                            .document(targetUser.uid)
                            .collection(Def.Collection.FRIEND_REQUESTS)
                            .document(myUid)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    onResult(false, "이미 요청을 보냈습니다.")
                                    return@addOnSuccessListener
                                }

                                val request = hashMapOf(
                                    Def.FriendReqFields.FROM_UID to myUid,
                                    Def.FriendReqFields.FROM_NAME to myUser.name,
                                    Def.FriendReqFields.FROM_EMAIL to myUser.email,
                                    Def.FriendReqFields.FROM_PROFILE_IMAGES to myUser.profileImageUrl,
                                    Def.FriendReqFields.FROM_CREATE_AT to System.currentTimeMillis()
                                        .toString(),
                                )

                                db.collection(Def.Collection.USERS)
                                    .document(targetUser.uid)
                                    .collection(Def.Collection.FRIEND_REQUESTS)
                                    .document(myUid)
                                    .set(request)
                                    .addOnSuccessListener {
                                        onResult(true, null)
                                    }
                                    .addOnFailureListener { e ->
                                        onResult(false, e.message)
                                    }
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * 친구 요청 목록 가져오기
     *
     * 친구 요청을 보낸 사람 목록을 가져옴
     */
    fun loadFriendRequest(onResult: (List<User>) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIEND_REQUESTS)
            .orderBy(Def.FriendReqFields.FROM_CREATE_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot == null || e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map {
                    User(
                        uid = it.getString(Def.FriendReqFields.FROM_UID) ?: "",
                        name = it.getString(Def.FriendReqFields.FROM_NAME) ?: "",
                        email = it.getString(Def.FriendReqFields.FROM_EMAIL) ?: "",
                        profileImageUrl = it.getString(Def.FriendReqFields.FROM_PROFILE_IMAGES)
                            ?: ""
                    )
                }
                onResult(list)
            }
    }

    /**
     * 친구 요청 수락
     *
     * 수락하면 서로의 friends에 저장하고, 요청은 삭제
     */
    fun acceptFriendRequest(user: User, onResult: (Boolean, String?) -> Unit) {
        Timber.d("acceptFriendRequest +")

        val myUid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(myUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val myUser = snapshot.toObject(User::class.java)

                if (myUser == null) {
                    onResult(false, "내 사용자 정보를 찾을 수 없습니다.")
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                val now = System.currentTimeMillis()

                val myFriendRef = db.collection(Def.Collection.USERS)
                    .document(myUid)
                    .collection(Def.Collection.FRIENDS)
                    .document(user.uid)

                val otherFriendRef = db.collection(Def.Collection.USERS)
                    .document(user.uid)
                    .collection(Def.Collection.FRIENDS)
                    .document(myUid)

                val requestRef = db.collection(Def.Collection.USERS)
                    .document(myUid)
                    .collection(Def.Collection.FRIEND_REQUESTS)
                    .document(user.uid)

                val myFriendData = hashMapOf(
                    Def.UsersFields.UID to user.uid,
                    Def.UsersFields.NAME to user.name,
                    Def.UsersFields.EMAIL to user.email,
                    Def.UsersFields.PROFILE_IMAGES to user.profileImageUrl,
                    Def.UsersFields.CREATE_AT to now
                )

                val otherFriendData = hashMapOf(
                    Def.UsersFields.UID to myUid,
                    Def.UsersFields.NAME to myUser.name,
                    Def.UsersFields.EMAIL to myUser.email,
                    Def.UsersFields.PROFILE_IMAGES to myUser.profileImageUrl,
                    Def.UsersFields.CREATE_AT to now
                )

                batch.set(myFriendRef, myFriendData)
                batch.set(otherFriendRef, otherFriendData)
                batch.delete(requestRef)

                batch.commit()
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
        Timber.d("acceptFriendRequest -")
    }

    /**
     * 친구 요청 거부
     */
    fun declineFriendRequest(user: User, onResult: (Boolean, String?) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIEND_REQUESTS)
            .document(user.uid)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * 친구 목록 가져오기
     */
    fun loadFriendList(onResult: (List<User>) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIENDS)
            .orderBy(Def.UsersFields.CREATE_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot == null || e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { documentSnapshot ->
                    Timber.d("loadFriendList: ${documentSnapshot.data}")

                    User(
                        uid = documentSnapshot.getString(Def.UsersFields.UID) ?: "",
                        name = documentSnapshot.getString(Def.UsersFields.NAME) ?: "",
                        email = documentSnapshot.getString(Def.UsersFields.EMAIL) ?: "",
                        profileImageUrl = documentSnapshot.getString(Def.UsersFields.PROFILE_IMAGES)
                            ?: ""
                    )
                }
                onResult(list)
            }
    }

    /**
     * 친구 삭제
     */
    fun deleteFriend(user: User, onResult: (Boolean, String?) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return

        val myFriendRef = db.collection(Def.Collection.USERS)
            .document(myUid)
            .collection(Def.Collection.FRIENDS)
            .document(user.uid)

        val otherFriendRef = db.collection(Def.Collection.USERS)
            .document(user.uid)
            .collection(Def.Collection.FRIENDS)
            .document(myUid)

        val batch = db.batch()
        batch.delete(myFriendRef)
        batch.delete(otherFriendRef)

        batch.commit()
            .addOnSuccessListener {
                Timber.d("delete success")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Timber.e(e, "delete fail")
                onResult(false, e.message)
            }
    }
}
