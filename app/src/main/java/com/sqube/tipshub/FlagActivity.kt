package com.sqube.tipshub

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Transaction
import models.Comment
import models.Report
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import java.util.*

class FlagActivity : AppCompatActivity(), View.OnClickListener {
    private var edtComment: MultiAutoCompleteTextView? = null
    private var comment: String? = null
    private var postId: String? = null
    private var reportedUsername: String? = null
    private var reportedUserId: String? = null
    private val TAG = "FlagActivity"
    private var progressBar: ProgressBar? = null
    private var prefs: SharedPreferences? = null
    var postReference: DocumentReference? = null
    var userId: String? = null
    var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flag)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = ""
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        edtComment = findViewById(R.id.edtPost)
        val btnPost = findViewById<Button>(R.id.btnPost)
        btnPost.setOnClickListener(this)
        val btnClose = findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener(this)
        progressBar = findViewById(R.id.prgLogin)
        postId = intent.getStringExtra("postId")
        val user = firebaseAuthentication!!.currentUser
        userId = user!!.uid
        username = user.displayName

        //get from intent reported post, username, and userId
        postId = intent.getStringExtra("postId")
        reportedUsername = intent.getStringExtra("reportedUsername")
        reportedUserId = intent.getStringExtra("reportedUserId")
        postReference = firebaseFirestore!!.collection("posts").document(postId!!)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnPost -> increaseCommentCount()
            R.id.btnClose -> popUp()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun reportPost() {
        comment = edtComment!!.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(comment) || comment!!.length < 3) {
            edtComment!!.error = "Type your reason"
            return
        }
        val commentReference = firebaseFirestore!!.collection("comments").document(postId!!)
                .collection("comments")
        val reportReference = firebaseFirestore!!.collection("report")

        //get user verification status from SharePreference
        val isVerified = prefs!!.getBoolean("isVerified", false)
        commentReference.add(Comment(username, userId, comment, postId, true, isVerified))
                .addOnSuccessListener {
                    reportReference.add(Report(username, userId, comment, postId, reportedUsername, reportedUserId))
                    progressBar!!.visibility = View.GONE
                    comment = ""
                    edtComment!!.setText("")
                    Snackbar.make(edtComment!!, "Comment added", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
    }

    fun increaseCommentCount() {
        comment = edtComment!!.text.toString()
        if (TextUtils.isEmpty(comment)) {
            edtComment!!.error = "Type your comment"
            return
        }
        progressBar!!.visibility = View.VISIBLE
        firebaseFirestore!!.runTransaction(label@ Transaction.Function<Void?> { transaction: Transaction ->
            Log.i(TAG, "apply: likes entered")
            val snapshot = transaction[postReference!!]

            //check if post still exists
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: like doesn't exist")
                return@label null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            val commentsCount = snapshot.getLong("commentsCount")!! + 1
            val reportCount = snapshot.getLong("reportCount")!! + 1
            val upd: MutableMap<String, Any> = HashMap()
            upd["commentsCount"] = commentsCount
            upd["reportCount"] = reportCount
            transaction.update(postReference!!, upd)
            null
        })
                .addOnSuccessListener { aVoid: Void? ->
                    Log.d(TAG, "Transaction success!")
                    reportPost()
                }
                .addOnFailureListener { e: Exception? ->
                    progressBar!!.visibility = View.GONE
                    Log.w(TAG, "Transaction failure.", e)
                    Toast.makeText(this@FlagActivity, "Connection failed", Toast.LENGTH_SHORT).show()
                }
    }

    private fun popUp() {
        val builder = AlertDialog.Builder(this@FlagActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("Save this comment?")
                .setPositiveButton("Save") { dialogInterface: DialogInterface?, i: Int -> finish() }
                .setNegativeButton("Delete") { dialogInterface: DialogInterface?, i: Int -> finish() }
                .show()
    }
}