package com.ko.simple_chat.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ko.simple_chat.MainActivity
import com.ko.simple_chat.R
import timber.log.Timber

/**
 * Firebase Cloud Messaging(FCM) 메시지를 수신하는 서비스
 *
 * FCM 토큰 생성시 Firestore에 저장
 * 서버에서 전송된 메시지 수신
 * 채팅 알림(Notification) 생성
 */
class MyFirebaseManagerService : FirebaseMessagingService() {

    /**
     * FCM 토큰이 새로 발급되었을 때 호출되는 함수
     *
     * 토큰은 앱 재설치, 데이터 초기화, 보안 정책 변경 등으로
     * 언제든지 변경될 수 있기 때문에 서버나 DB에 다시 저장해야 함.
     */
    override fun onNewToken(token: String) {
        Timber.d("onNewToken: $token")

        FirebaseManager.updateMyFcmToken(token)
    }

    /**
     * FCM 메시지를 수신했을 때 호출되는 함수
     *
     * 서버에서 전달된 메시지를 파싱하고
     * 알림을 생성하여 사용자에게 표시
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("onMessageReceived: ${message.data}")
        Timber.d("onMessageReceived: ${message.notification?.title}, ${message.notification?.body}")

        // 서버에서 전달된 data payload 값 추출
        val title = message.notification?.title ?: "새 메시지"
        val body = message.notification?.body ?: "메시지가 도착했습니다."
        val otherUid = message.data["otherUid"] ?: ""

        showNotification(title, body, otherUid)
    }

    /**
     * 채팅 알림을 생성하고 표시하는 함수
     */
    private fun showNotification(title: String, body: String, otherUid: String) {
        val channelId = "chat_message_ch"
        val channelName = "Chat Message"

        createNotificationChannel(channelId, channelName)

        val intent = Intent(this, MainActivity::class.java).apply {
            if (otherUid.isNotEmpty()) {
                putExtra("otherUid", otherUid)
            }

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            otherUid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()


        val hasPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val notificationId = System.currentTimeMillis().toInt()

            NotificationManagerCompat.from(this)
                .notify(notificationId, notification)
        }
    }

    /**
     * Notification 채널 생성
     *
     * Android 8 (API 26) 이상에서는
     * 알림을 표시하기 위해 채널을 반드시 생성해야 한다.
     */
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "채팅 메시지 알림 채널"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}