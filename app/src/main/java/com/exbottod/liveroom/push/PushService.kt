package com.exbottod.liveroom.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Placeholder FCM receiver. Hook notification routing here once the
 * backend starts sending room invites / gift events.
 */
class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // TODO: send token to our own backend: POST /device/fcm-token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // TODO: route by message.data["type"]
    }
}
