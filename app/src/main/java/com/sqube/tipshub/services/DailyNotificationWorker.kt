package com.sqube.tipshub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.sqube.tipshub.activities.MainActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.models.Notification
import java.util.*

class DailyNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var username: String? = null
    override fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        username = user.displayName
        showNotification()
        val dataOutput = Data.Builder().putString("Work Result", "Job finished").build()
        return Result.success(dataOutput)
    }

    private fun showNotification() {
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
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, notificationID, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT)
        val notification = getNotification(Random().nextInt(5))
        val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setColor(applicationContext.resources.getColor(R.color.colorPrimaryDark))
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setSmallIcon(R.drawable.icon_svg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        manager.notify(notificationID, builder.build())
    }

    private fun getNotification(i: Int): Notification {
        val notification = Notification()
        when (i) {
            1 -> {
                notification.title = "Recommended for you"
                notification.message = "See all the new tips from your favourite tipsters"
            }
            2 -> {
                notification.title = "You missed some banker tips"
                notification.message = "We have some new banker tips on the app"
            }
            3 -> {
                notification.title = "Wow.. Today is awesome"
                notification.message = "Get the latest posts for today"
            }
            4 -> {
                notification.title = "Hello $username"
                notification.message = "We just wanted to check up you"
            }
            else -> {
                notification.title = "Latest sports news for this week"
                notification.message = "See the top 15 sports news for this week"
            }
        }
        return notification
    }
}