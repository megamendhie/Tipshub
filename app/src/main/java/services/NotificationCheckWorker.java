package services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.sqube.tipshub.MainActivity;
import com.sqube.tipshub.R;

import java.util.Random;

import models.Notification;
import utils.FirebaseUtil;

import static android.app.Notification.DEFAULT_ALL;
import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;

public class NotificationCheckWorker extends Worker {
    private String username;

    public NotificationCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user ==null)
            return Result.success();
        username = user.getDisplayName();

        String userId = user.getUid();
        FirebaseUtil.getFirebaseFirestore().collection("notifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId).whereEqualTo("seen", false).get().addOnSuccessListener(queryDocumentSnapshots -> {

                    if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()){
                        for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                            Notification notification = snapshot.toObject(Notification.class);
                            showNotification(notification);
                        }
                    }
                });

        Data dataOutput = new Data.Builder().putString("Work Result", "Job finished").build();
        return Result.success(dataOutput);
    }

    private void showNotification(Notification notification) {
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

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationID, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimaryDark))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setSmallIcon(R.drawable.icon_svg)
                .setPriority(PRIORITY_DEFAULT)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(notificationID, builder.build());
    }
}
