package services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.sqube.tipshub.FullPostActivity
import com.sqube.tipshub.MainActivity
import com.sqube.tipshub.MemberProfileActivity
import utils.FirebaseUtil
import java.util.*

class NotificationBroadcastReceiver : BroadcastReceiver() {
    val COMMENT = "comment"
    val POST = "post"
    val FOLLOWING = "following"
    val SUBSCRIPTION = "subscription"
    var action: String? = null
    override fun onReceive(context: Context, intent: Intent) {
        action = intent.action
        assert(action != null)
        if (action == "NOTIFICATION_ACTION" || action == "WORKER_ACTION") {
            openNotification(context, intent)
        }
    }

    private fun openNotification(context: Context, intent: Intent) {
        val receivedIntent = intent.getStringExtra("RECEIVED_INTENT")
        val ref = FirebaseUtil.firebaseFirestore?.collection("notifications")
        Intent(context, MainActivity::class.java)
        val activityIntent: Intent
        when (receivedIntent) {
            COMMENT, POST -> {
                activityIntent = Intent(context, FullPostActivity::class.java)
                activityIntent.putExtra("postId", intent.getStringExtra("POST_ID"))
            }
            FOLLOWING, SUBSCRIPTION -> {
                activityIntent = Intent(context, MemberProfileActivity::class.java)
                activityIntent.putExtra("userId", intent.getStringExtra("USER_ID"))
            }
            else -> activityIntent = Intent(context, MainActivity::class.java)
        }
        context.startActivity(activityIntent)
        Log.i("NotificationBroadcast", "openNotification: clicked")
        if (action == "NOTIFICATION_ACTION") {
            val notificationTime = intent.getLongExtra("NOTIFICATION_TIME", 0)
            ref?.whereEqualTo("time", notificationTime)!!
                    .limit(1).get().addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        if (task.result == null || task.result!!.isEmpty) return@addOnCompleteListener
                        val notificationId = task.result!!.documents[0].id
                        val url: MutableMap<String, Any> = HashMap()
                        url["seen"] = true
                        ref.document(notificationId).set(url,  SetOptions.merge())
                    }
        } else if (action == "WORKER_ACTION") {
            val notificationId = intent.getStringExtra("NOTIFICATION_ID")
            val url: MutableMap<String, Any> = HashMap()
            url["seen"] = true
            ref?.document(notificationId!!)?.set(url, SetOptions.merge())
        }
    }
}