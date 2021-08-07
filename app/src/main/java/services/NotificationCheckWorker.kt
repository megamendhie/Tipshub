package services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.sqube.tipshub.MainActivity
import com.sqube.tipshub.R
import models.Notification
import utils.FirebaseUtil
import java.util.*

class NotificationCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val userId = user.uid
        FirebaseUtil.getFirebaseFirestore().collection("notifications").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId).whereEqualTo("seen", false).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot? ->
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        for (snapshot in queryDocumentSnapshots.documents) {
                            val notification = snapshot.toObject(Notification::class.java)
                            showNotification(notification, snapshot.id)
                        }
                    }
                }
        val dataOutput = Data.Builder().putString("Work Result", "Job finished").build()
        return Result.success(dataOutput)
    }

    private fun showNotification(notification: Notification?, notificationId: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)
        val channelId = "admin_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Tipshub notification"
            val channelDescription = "Notifications from Tipshub"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = channelDescription
            channel.enableLights(true)
            channel.lightColor = NotificationCompat.DEFAULT_LIGHTS
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }
        Intent(applicationContext, MainActivity::class.java)
        var notificationIntent: Intent
        val COMMENT = "comment"
        val POST = "post"
        val FOLLOWING = "following"
        val SUBSCRIPTION = "subscription"

        /*
        String received_intent = notification.getType();
        switch (received_intent){
            case COMMENT:
            case POST:
                notificationIntent = new Intent(getApplicationContext(), FullPostActivity.class);
                notificationIntent.putExtra("postId", notification.getIntentUrl());
                break;
            case FOLLOWING:
            case SUBSCRIPTION:
                notificationIntent = new Intent(getApplicationContext(), MemberProfileActivity.class);
                notificationIntent.putExtra("userId", notification.getSentFrom());
                break;
            default:
                notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                break;

        }
         */

        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationID, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        //register broadcast receiver
        val nsnBroadcast = NotificationBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction("WORKER_ACTION")
        applicationContext.registerReceiver(nsnBroadcast, intentFilter)

        // While making notification
        val received_intent = notification!!.type
        val broadcastIntent = Intent("NOTIFICATION_ACTION")
        broadcastIntent.putExtra("RECEIVED_INTENT", received_intent)
        broadcastIntent.putExtra("NOTIFICATION_ID", notificationID)
        when (received_intent) {
            COMMENT, POST -> broadcastIntent.putExtra("POST_ID", notification.intentUrl)
            FOLLOWING, SUBSCRIPTION -> broadcastIntent.putExtra("USER_ID", notification.sentFrom)
        }
        val pendingIntentBroadcast = PendingIntent.getBroadcast(applicationContext, 0, broadcastIntent, 0)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setColor(applicationContext.resources.getColor(R.color.colorPrimaryDark))
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setSmallIcon(R.drawable.icon_svg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntentBroadcast)
                .setAutoCancel(true)
        manager.notify(notificationID, builder.build())
    }
}