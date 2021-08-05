package services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MainActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.Random;

import models.UserNetwork;
import utils.FirebaseUtil;

import static android.app.Notification.DEFAULT_ALL;
import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String CHANNEL_ID ="admin_channel";
    private static final String SUBSCRIBE_TO = "tipshub_admin";
    private static final String TAG = "FbMessagingService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG, "onTokenRefresh completed with token: " + token);
        FirebaseMessaging FCM = FirebaseMessaging.getInstance();

        // Once the token is generated, subscribe to topic with the userId
        FCM.subscribeToTopic(SUBSCRIBE_TO);

        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user == null)
            return;

        FCM.subscribeToTopic(user.getUid());
        ArrayList<String> subscriptionList = UserNetwork.getSubscribed();
        if(subscriptionList!=null && !subscriptionList.isEmpty()){
            for(String subed: subscriptionList){
                FCM.subscribeToTopic("sub_"+subed);
            }
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        final String COMMENT = "comment", POST = "post", FOLLOWING = "following", SUBSCRIPTION = "subscription";

        Intent intent = new Intent(this, MainActivity.class);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNoificationChannel(notificationManager);

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
        NotificationBroadcastReceiver nxnBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction("NOTIFICATION_ACTION");
        try {
            registerReceiver(nxnBroadcastReceiver, intentFilter);
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        // While making notification
        String received_intent = remoteMessage.getData().get("type");
        Intent broadcastIntent = new Intent("NOTIFICATION_ACTION");
        broadcastIntent.putExtra("RECEIVED_INTENT", received_intent);
        broadcastIntent.putExtra("NOTIFICATION_TIME", remoteMessage.getSentTime());
        if(received_intent==null)
            return;
        switch (received_intent){
            case COMMENT:
            case POST:
                broadcastIntent.putExtra("POST_ID", remoteMessage.getData().get("intentUrl"));
                break;
            case FOLLOWING:
            case SUBSCRIPTION:
                broadcastIntent.putExtra("USER_ID", remoteMessage.getData().get("sentFrom"));
                break;
        }
        PendingIntent pendingIntentBroadcast = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.icn_medium);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_svg)
                .setLargeIcon(largeIcon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("message")))
                .setAutoCancel(true)
                .setPriority(PRIORITY_DEFAULT)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntentBroadcast);

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNoificationChannel(NotificationManager notificationManager){
        final CharSequence CHANNEL_NAME = "Tipshub notification";
        final String CHANNEL_DESCRIPTION = "Notifications from Tipshub";

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
    }

}