package services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MainActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.R;

import java.util.Random;

import models.Notification;
import utils.FirebaseUtil;

import static android.app.Notification.DEFAULT_ALL;
import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;

public class NotificationCheckWorker extends Worker {
    public NotificationCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user ==null)
            return Result.success();

        String userId = user.getUid();
        FirebaseUtil.getFirebaseFirestore().collection("notifications").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId).whereEqualTo("seen", false).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()){
                        for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                            Notification notification = snapshot.toObject(Notification.class);
                            showNotification(notification, snapshot.getId());
                        }
                    }
                });

        Data dataOutput = new Data.Builder().putString("Work Result", "Job finished").build();
        return Result.success(dataOutput);
    }

    private void showNotification(Notification notification, String notificationId) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
        String channelId = "admin_channel";

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String channelName = "Tipshub notification";
            String channelDescription = "Notifications from Tipshub";

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(NotificationCompat.DEFAULT_LIGHTS);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        new Intent(getApplicationContext(), MainActivity.class);
        Intent notificationIntent;
        final String COMMENT = "comment", POST = "post", FOLLOWING = "following", SUBSCRIPTION = "subscription";

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
        NotificationBroadcastReceiver nsnBroadcast = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction("WORKER_ACTION");
        getApplicationContext().registerReceiver(nsnBroadcast, intentFilter);

        // While making notification
        String received_intent = notification.getType();
        Intent broadcastIntent = new Intent("NOTIFICATION_ACTION");
        broadcastIntent.putExtra("RECEIVED_INTENT", received_intent);
        broadcastIntent.putExtra("NOTIFICATION_ID", notificationID);
        switch (received_intent){
            case COMMENT:
            case POST:
                broadcastIntent.putExtra("POST_ID", notification.getIntentUrl());
                break;
            case FOLLOWING:
            case SUBSCRIPTION:
                broadcastIntent.putExtra("USER_ID", notification.getSentFrom());
                break;
        }

        PendingIntent pendingIntentBroadcast = PendingIntent.getBroadcast(getApplicationContext(), 0, broadcastIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimaryDark))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setSmallIcon(R.drawable.icon_svg)
                .setPriority(PRIORITY_DEFAULT)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntentBroadcast)
                .setAutoCancel(true);

        manager.notify(notificationID, builder.build());
    }
}
