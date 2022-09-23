package com.sqube.tipshub.activities

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
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityFlagBinding
import com.sqube.tipshub.models.Comment
import com.sqube.tipshub.models.Report
import com.sqube.tipshub.utils.FirebaseUtil.firebaseAuthentication
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import java.util.*

class FlagActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding: ActivityFlagBinding? = null
    private val binding get() = _binding!!
    private var comment: String? = null
    private var postId: String? = null
    private var reportedUsername: String? = null
    private var reportedUserId: String? = null
    private val tag = "FlagActivity"
    private var prefs: SharedPreferences? = null
    private var postReference: DocumentReference? = null
    private var userId: String? = null
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFlagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = ""
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        binding.btnPost.setOnClickListener(this)
        binding.btnClose.setOnClickListener(this)
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
        when (v) {
            binding.btnPost -> increaseCommentCount()
            binding.btnClose -> popUp()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun reportPost() {
        comment = binding.edtPost.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(comment) || comment!!.length < 3) {
            binding.edtPost.error = "Type your reason"
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
                    binding.prgLogin.visibility = View.GONE
                    comment = ""
                    binding.edtPost.setText("")
                    Snackbar.make(binding.edtPost, "Comment added", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
    }

    private fun increaseCommentCount() {
        comment = binding.edtPost.text.toString()
        if (TextUtils.isEmpty(comment)) {
            binding.edtPost.error = "Type your comment"
            return
        }
        binding.prgLogin.visibility = View.VISIBLE
        firebaseFirestore!!.runTransaction { transaction: Transaction ->
            Log.i(tag, "apply: likes entered")
            val snapshot = transaction[postReference!!]

            //check if post still exists
            if (snapshot.exists()) {
                val commentsCount = snapshot.getLong("commentsCount")!! + 1
                val reportCount = snapshot.getLong("reportCount")!! + 1
                val upd: MutableMap<String, Any> = HashMap()
                upd["commentsCount"] = commentsCount
                upd["reportCount"] = reportCount
                transaction.update(postReference!!, upd)
            }
            null
        }
                .addOnSuccessListener {
                    Log.d(tag, "Transaction success!")
                    reportPost()
                }
                .addOnFailureListener { e: Exception? ->
                    binding.prgLogin.visibility = View.GONE
                    Log.w(tag, "Transaction failure.", e)
                    Toast.makeText(this@FlagActivity, "Connection failed", Toast.LENGTH_SHORT).show()
                }
    }

    private fun popUp() {
        val builder = AlertDialog.Builder(this@FlagActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("Save this comment?")
                .setPositiveButton("Save") { _: DialogInterface?, i: Int -> finish() }
                .setNegativeButton("Delete") { _: DialogInterface?, i: Int -> finish() }
                .show()
    }
}