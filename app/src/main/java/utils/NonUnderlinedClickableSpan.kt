package utils

import android.content.Context
import android.content.Intent
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.sqube.tipshub.MemberProfileActivity
import com.sqube.tipshub.MyProfileActivity
import com.sqube.tipshub.R
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore

class NonUnderlinedClickableSpan internal constructor(private val context: Context, // Keyword or url
                                                      private val text: String, // 0-hashtag , 1- mention, 2- url link
                                                      private val type: Int) : ClickableSpan() {
    private val TAG = "NonUnderlinedSpan"
    override fun onClick(widget: View) {
        Log.d(TAG, "onClick: ok$text")
        when (type) {
            0 -> {
            }
            1 -> {
                val myUserId = firebaseAuthentication!!.currentUser!!.uid
                val username = text.substring(1)
                firebaseFirestore!!.collection("profiles").whereEqualTo("a2_username", username)
                        .limit(1).get().addOnCompleteListener { task: Task<QuerySnapshot?> ->
                            if (task.result == null || task.result!!.isEmpty) {
                                Toast.makeText(context, "unknown username", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                            val userId = task.result!!.documents[0].id
                            if (userId == myUserId) {
                                context.startActivity(Intent(context, MyProfileActivity::class.java))
                            } else {
                                val intent = Intent(context, MemberProfileActivity::class.java)
                                intent.putExtra("userId", userId)
                                context.startActivity(intent)
                            }
                        }
            }
            else -> {
            }
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
        ds.color = context.resources.getColor(
                R.color.colorPrimaryDark)
        // ds.setTypeface(Typeface.DEFAULT_BOLD);
    }
}