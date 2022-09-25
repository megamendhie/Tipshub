package com.sqube.tipshub.adapters

import com.sqube.tipshub.adapters.CommentAdapter.CommentHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.ObjectKey
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.activities.LoginActivity
import com.sqube.tipshub.activities.MemberProfileActivity
import com.sqube.tipshub.activities.MyProfileActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.CommentViewBinding
import com.sqube.tipshub.models.Comment
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.SnapId
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.services.GlideApp
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.GUEST
import com.sqube.tipshub.utils.Reusable
import com.sqube.tipshub.utils.Reusable.Companion.applyLinkfy
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.Reusable.Companion.getTime
import com.sqube.tipshub.utils.Reusable.Companion.signature
import java.util.*
import kotlin.math.min

class CommentAdapter(private val mainPostId: String, query: Query?, userID: String, val activity: Activity, val context: Context) : FirestoreRecyclerAdapter<Comment, CommentHolder>(FirestoreRecyclerOptions.Builder<Comment>()
        .setQuery((query)!!, Comment::class.java)
        .build()) {
    private val tag = "CommentAdapter"
    private var userId: String = userID
    private val calculations = Calculations(context)
    private val storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    val repliedList = ArrayList<SnapId>()

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun resetRepliesList() {
        repliedList.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: CommentHolder, position: Int, model: Comment) {
        val binding = holder.binding
        val postId = snapshots.getSnapshot(position).id
        if (model.isFlag)
            binding.containerPost.setBackgroundColor(context.resources.getColor(R.color.comment_flagged))
        else
            binding.containerPost.setBackgroundColor(context.resources.getColor(R.color.comment_bg))
        binding.imgReply.visibility = if ((model.userId == userId)) View.GONE else View.VISIBLE

        with(model){
            binding.txtUsername.text = username
            binding.txtPost.text = content
            applyLinkfy(context, content, binding.txtPost)
            binding.txtTime.text = getTime(time)
            binding.imgLike.setColorFilter(if (likes.contains(userId)) context.resources.getColor(R.color.likeGold) else context.resources.getColor(R.color.likeGrey))
            binding.imgDislike.setColorFilter(if (dislikes.contains(userId)) context.resources.getColor(R.color.likeGold) else context.resources.getColor(R.color.likeGrey))
            binding.txtLike.text = if (likesCount == 0L) "" else likesCount.toString()
            binding.txtDislike.text = if (dislikesCount == 0L) "" else dislikesCount.toString()
            GlideApp.with(context)
                    .load(storageReference.child(userId))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(userId[0]))
                    .signature(ObjectKey(userId + "_" + signature))
                    .into(binding.imgDp)
        }

        //listen to dp click and open user profile
        binding.imgDp.setOnClickListener {
            if ((model.userId == userId)) {
                binding.imgDp.context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                binding.imgDp.context.startActivity(intent)
            }
        }

        //listen to username click and open user profile
        binding.txtPost.setOnClickListener {
            if ((model.userId == userId))
                binding.txtPost.context.startActivity(Intent(context, MyProfileActivity::class.java))
            else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                binding.txtUsername.context.startActivity(intent)
            }
        }
        binding.imgLike.setOnClickListener {
            if ((userId == GUEST)) { loginPrompt(binding.imgLike)
                return@setOnClickListener
            }
            if (model.dislikes.contains(userId)) {
                binding.imgLike.setColorFilter(context.resources.getColor(R.color.likeGold))
                binding.imgDislike.setColorFilter(context.resources.getColor(R.color.likeGrey))
                binding.txtLike.text = (model.likesCount + 1).toString()
                binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
            }
            else {
                if (model.likes.contains(userId)) {
                    binding.imgLike.setColorFilter(context.resources.getColor(R.color.likeGrey))
                    binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
                } else {
                    binding.imgLike.setColorFilter(context.resources.getColor(R.color.likeGold))
                    binding.txtLike.text = (model.likesCount + 1).toString()
                }
            }
            val substring: String = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onCommentLike(userId, model.userId, postId, mainPostId, substring)
        }
        binding.imgReply.setOnClickListener {
            if ((userId == GUEST)) { loginPrompt(binding.imgReply)
                return@setOnClickListener
            }
            val edtComment: EditText = activity.findViewById(R.id.edtComment)
            val comment: String = edtComment.text.toString()
            if (comment.isEmpty()) edtComment.setText(String.format("@%s ", model.username)) else edtComment.setText(String.format("%s @%s ", comment.trim { it <= ' ' }, model.username))
            edtComment.setSelection(edtComment.text.length)
            repliedList.add(SnapId(model.userId, model.username))
        }
        binding.imgDislike.setOnClickListener {
            if ((userId == GUEST)) { loginPrompt(it)
                return@setOnClickListener
            }
            if (model.likes.contains(userId)) {
                binding.imgLike.setColorFilter(context.resources.getColor(R.color.likeGrey))
                binding.imgDislike.setColorFilter(context.resources.getColor(R.color.likeGold))
                binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
                binding.txtDislike.text = (model.dislikesCount + 1).toString()
            }
            else {
                if (model.dislikes.contains(userId)) {
                    binding.imgDislike.setColorFilter(context.resources.getColor(R.color.likeGrey))
                    binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
                } else {
                    binding.imgDislike.setColorFilter(context.resources.getColor(R.color.likeGold))
                    binding.txtDislike.text = (model.dislikesCount + 1).toString()
                }
            }
            val substring: String = model.content.substring(0, min(model.content.length, 90))
            calculations.onCommentDislike(userId, model.userId, postId, mainPostId, substring)
        }
        binding.imgOverflow.setOnClickListener { displayOverflow(model, model.userId, postId, it) }
    }

    private fun loginPrompt(view: View) {
        val builder = AlertDialog.Builder(view.rootView.context,
                R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { _: DialogInterface?, i: Int -> view.context.startActivity(Intent(view.context, LoginActivity::class.java)) }
                .show()
    }

    private fun displayOverflow(model: Comment, commentUserId: String, postId: String, imgOverflow: View) {
        val builder = android.app.AlertDialog.Builder(imgOverflow.rootView.context)
        val inflater = LayoutInflater.from(imgOverflow.rootView.context)
        val dialogView: View = if ((commentUserId == userId)) inflater.inflate(R.layout.dialog_mine_comment, null)
                else inflater.inflate(R.layout.dialog_member_comment, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        val reusable = Reusable()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val btnSubmit: Button = dialog.findViewById(R.id.btnSubmit)
        val btnDelete: Button = dialog.findViewById(R.id.btnDelete)
        val btnFollow: Button = dialog.findViewById(R.id.btnFollow)
        val btnShare: Button = dialog.findViewById(R.id.btnShare)
        btnSubmit.visibility = View.GONE
        if (commentUserId != userId) btnDelete.visibility = View.GONE
        if (UserNetwork.following == null)
            btnFollow.visibility = View.GONE
        else
            btnFollow.text = if (UserNetwork.following.contains(commentUserId)) "UNFOLLOW" else "FOLLOW"
        btnShare.setOnClickListener { reusable.shareComment(btnShare.context, model.username, model.content)
            dialog.cancel()
        }
        btnFollow.setOnClickListener {
            if (!getNetworkAvailability(context)) {
                Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                dialog.cancel()
                return@setOnClickListener
            }
            if ((userId == GUEST)) {
                loginPrompt(btnFollow)
                return@setOnClickListener
            }
            if ((btnFollow.text == "FOLLOW")) {
                calculations.followMember(imgOverflow, userId, commentUserId)
            } else unfollowPrompt(imgOverflow, commentUserId, model.username)
            dialog.cancel()
        }
        btnDelete.setOnClickListener { deleteComment(postId, imgOverflow)
            dialog.cancel()
        }
    }

    private fun unfollowPrompt(imgOverflow: View, userID: String, username: String) {
        val builder = AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { _: DialogInterface?, _: Int -> }
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> calculations.unfollowMember(imgOverflow, userId, userID) }
                .show()
    }

    private fun deleteComment(postId: String, imgOverflow: View) {
        val postPath = firebaseFirestore!!.collection("posts").document(mainPostId)
        firebaseFirestore!!.runTransaction { transaction: Transaction ->
            val snapshot: DocumentSnapshot = transaction.get(postPath)
            //Check if post exist first
            if (snapshot.exists()) {
                val updates: MutableMap<String, Any> = HashMap()
                val commentCount: Long = snapshot.toObject(Post::class.java)!!.commentsCount - 1 //Retrieve commentCount stat
                updates.put("commentsCount", Math.max(0, commentCount))
                transaction.update(postPath, updates)
                Log.i(tag, "apply: snapshot is empty")
            }
            null
        }.addOnSuccessListener {
            firebaseFirestore!!.collection("comments").document(postId).delete()
                    .addOnSuccessListener { Snackbar.make(imgOverflow, "Comment deleted", Snackbar.LENGTH_SHORT).show() }
                    .addOnFailureListener { Snackbar.make(imgOverflow, "Something went wrong", Snackbar.LENGTH_SHORT).show() }
        }.addOnFailureListener { Snackbar.make(imgOverflow, "Something went wrong", Snackbar.LENGTH_SHORT).show() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val binding = CommentViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentHolder(binding)
    }

    inner class CommentHolder(val binding: CommentViewBinding) : RecyclerView.ViewHolder(binding.root)
}