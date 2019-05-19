package com.trainerapp.web.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trainerapp.R
import com.trainerapp.ui.NavigationActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    private val NOTIFICATION_EVENT_KEY = "notification_event"

    private val EVENT_ID_KEY = "event_id"

    private val NOTIFICATION_EVENT_COMMENT_VALUE = "comment"

    private val NOTIFICATION_EVENT_REFRESH_VALUE = "refresh"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom())
        var notificationEvent = remoteMessage.data[NOTIFICATION_EVENT_KEY]
        when (notificationEvent){
            NOTIFICATION_EVENT_COMMENT_VALUE -> {
                val eventId = remoteMessage.data[EVENT_ID_KEY]
                if (eventId != null) {
                    sendToActivity(eventKey = NOTIFICATION_EVENT_COMMENT_VALUE, eventId = eventId)
                }
            }
            NOTIFICATION_EVENT_REFRESH_VALUE -> {
                val eventId = remoteMessage.data[EVENT_ID_KEY]
                if (eventId != null) {
                    sendToActivity(eventKey = NOTIFICATION_EVENT_REFRESH_VALUE, eventId = eventId)
                } else {
                    sendToActivity(eventKey = NOTIFICATION_EVENT_REFRESH_VALUE, eventId = "")
                }
            }
        }

//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.getBody())
//            sendNotification(remoteMessage.notification)
//        }

    }

    private fun sendToActivity(eventKey: String?, eventId: String?) {
        val intent = Intent(NavigationActivity.BROADCAST_REFRESH)
        intent.putExtra(NavigationActivity.EVENT_ID_INTENT, eventId)
        intent.putExtra(NavigationActivity.NOTIFICATION_EVENT_KEY, eventKey)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: " + token!!)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(remoteMessage: RemoteMessage.Notification?) {
        val intent = Intent(this, NavigationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage?.title)
                .setContentText(remoteMessage?.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        } else {
            TODO("VERSION.SDK_INT < M")
        }

         //Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }


        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}
