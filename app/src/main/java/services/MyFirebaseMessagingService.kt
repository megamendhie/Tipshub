package services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sqube.tipshub.MainActivity
import com.sqube.tipshub.R
import models.UserNetwork
import utils.FirebaseUtil
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val CHANNEL_ID = "admin_channel"
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "onTokenRefresh completed with token: $token")
        val FCM = FirebaseMessaging.getInstance()

        // Once the token is generated, subscribe to topic with the userId
        FCM.subscribeToTopic(SUBSCRIBE_TO)
        val user = FirebaseUtil.getFirebaseAuthentication().currentUser ?: return
        FCM.subscribeToTopic(user.uid)
        val subscriptionList = UserNetwork.getSubscribed()
        if (subscriptionList != null && !subscriptionList.isEmpty()) {
            for (subed in subscriptionList) {
                FCM.subscribeToTopic("sub_$subed")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val COMMENT = "comment"
        val POST = "post"
        val FOLLOWING = "following"
        val SUBSCRIPTION = "subscription"
        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNoificationChannel(notificationManager)

        /*
        Log.i(TAG, "onMessageReceived: "+ remoteMessage.getData());
        String received_intent = remoteMessage.getData().get("type");
        if(received_intent==null){
            received_intent="main";
        }
        switch (received_intent){
            case COMMENT:
            case POST:
                intent = new Intent(this, FullPostActivity.class);
                intent.putExtra("postId", remoteMessage.getData().get("intentUrl"));
                break;
            case FOLLOWING:
            case SUBSCRIPTION:
                intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra("userId", remoteMessage.getData().get("sentFrom"));
                break;
        }

         */

        //register broadcast receiver
        val nxnBroadcastReceiver = NotificationBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction("NOTIFICATION_ACTION")
        try {
            registerReceiver(nxnBroadcastReceiver, intentFilter)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        // While making notification
        val received_intent = remoteMessage.data["type"]
        val broadcastIntent = Intent("NOTIFICATION_ACTION")
        broadcastIntent.putExtra("RECEIVED_INTENT", received_intent)
        broadcastIntent.putExtra("NOTIFICATION_TIME", remoteMessage.sentTime)
        if (received_intent == null) return
        when (received_intent) {
            COMMENT, POST -> broadcastIntent.putExtra("POST_ID", remoteMessage.data["intentUrl"])
            FOLLOWING, SUBSCRIPTION -> broadcastIntent.putExtra("USER_ID", remoteMessage.data["sentFrom"])
        }
        val pendingIntentBroadcast = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)
        val largeIcon = BitmapFactory.decodeResource(resources,
                R.drawable.icn_medium)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_svg)
                .setLargeIcon(largeIcon)
                .setContentTitle(remoteMessage.data["title"])
                .setContentText(remoteMessage.data["message"])
                .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.data["message"]))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntentBroadcast)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorPrimaryDark)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNoificationChannel(notificationManager: NotificationManager) {
        val CHANNEL_NAME: CharSequence = "Tipshub notification"
        val CHANNEL_DESCRIPTION = "Notifications from Tipshub"
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = CHANNEL_DESCRIPTION
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val SUBSCRIBE_TO = "tipshub_admin"
        private const val TAG = "FbMessagingService"
    }
}