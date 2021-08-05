package services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.SetOptions;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MainActivity;
import com.sqube.tipshub.MemberProfileActivity;

import java.util.HashMap;
import java.util.Map;

import utils.FirebaseUtil;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    final String COMMENT = "comment", POST = "post", FOLLOWING = "following", SUBSCRIPTION = "subscription";
    String action;
    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        assert action != null;
        if(action.equals("NOTIFICATION_ACTION")||action.equals("WORKER_ACTION")){
            openNotification(context, intent);
        }
    }

    private void openNotification(Context context, Intent intent) {
        String receivedIntent = intent.getStringExtra("RECEIVED_INTENT");
        new Intent(context, MainActivity.class);
        Intent activityIntent;
        switch (receivedIntent){
            case COMMENT:
            case POST:
                activityIntent = new Intent(context, FullPostActivity.class);
                activityIntent.putExtra("postId", intent.getStringExtra("POST_ID"));
                break;
            case FOLLOWING:
            case SUBSCRIPTION:
                activityIntent = new Intent(context, MemberProfileActivity.class);
                activityIntent.putExtra("userId", intent.getStringExtra("USER_ID"));
                break;
            default:
                activityIntent = new Intent(context, MainActivity.class);
                break;
        }
        context.startActivity(activityIntent);

        Log.i("NotificationBroadcast", "openNotification: clicked");
        if(action.equals("NOTIFICATION_ACTION")) {
            final long notificationTime = intent.getLongExtra("NOTIFICATION_TIME", 0);
            FirebaseUtil.getFirebaseFirestore().collection("notifications").whereEqualTo("time", notificationTime)
                    .limit(1).get().addOnCompleteListener(task -> {
                if (task.getResult() == null || task.getResult().isEmpty())
                    return;
                String notificationId = task.getResult().getDocuments().get(0).getId();
                Map<String, Object> url = new HashMap<>();
                url.put("seen", true);
                FirebaseUtil.getFirebaseFirestore().collection("notifications").document(notificationId)
                        .set(url, SetOptions.merge());
            });
        }
        else if(action.equals("WORKER_ACTION")){
            String notificationId = intent.getStringExtra("NOTIFICATION_ID");
            Map<String, Object> url = new HashMap<>();
            url.put("seen", true);
            FirebaseUtil.getFirebaseFirestore().collection("notifications").document(notificationId)
                    .set(url, SetOptions.merge());
        }
    }
}
