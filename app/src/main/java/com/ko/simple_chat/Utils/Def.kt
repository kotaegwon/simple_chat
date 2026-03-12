package com.ko.simple_chat.Utils

object Def {
    /**
     * INTENT ACTION
     */
    object Intent {
        const val NOTIFICATION: String = "noti_info"
        const val USER_INFO: String = "user_info"
        const val SENDER_NAME = "senderName"
        const val SENDER_UID = "senderUid"
    }

    /**
     * FIREBASE COLLECTION
     */
    object Collection {
        const val CHAT_ROOMS: String = "chatRooms"
        const val CHAT_ROOMS_MESSAGES = "messages"
        const val USERS: String = "users"
        const val FRIENDS: String = "friends"
        const val FRIEND_REQUESTS: String = "friendRequests"
    }

    object UsersFields {
        const val UID = "uid"
        const val EMAIL = "email"
        const val NAME = "name"
        const val CREATE_AT = "createAt"
        const val FCM_TOKEN = "fcmToken"
        const val PROFILE_IMAGES: String = "profileImageUrl"

    }

    object ChatRoomsFields {
        const val USERS = "users"
        const val LAST_MESSAGE = "lastMessage"
        const val UPDATE_AT = "updateAt"
    }

    object MessageFields {
        const val MY_UID = "myUid"
        const val OTHER_UID = "otherUid"
        const val NAME = "name"
        const val MESSAGE = "message"
        const val TIME = "time"
        const val READ = "read"
    }

    object FriendReqFields {
        const val FROM_UID = "fromUid"
        const val FROM_NAME = "fromName"
        const val FROM_EMAIL = "fromEmail"
        const val FROM_PROFILE_IMAGES = "fromProfileImages"
        const val FROM_CREATE_AT = "fromCreateAt"
    }

    object MessageType{
        const val TEXT = "text"
        const val IMAGE = "image"
    }
}
