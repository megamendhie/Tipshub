package adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.signature.ObjectKey
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.sqube.tipshub.*
import models.Post
import models.UserNetwork
import services.GlideApp
import utils.Calculations
import utils.FirebaseUtil.firebaseFirestore
import utils.FirebaseUtil.firebaseStorage
import utils.Reusable.Companion.applyLinkfy
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.getPlaceholderImage
import utils.Reusable.Companion.getTime
import utils.Reusable.Companion.shareTips
import utils.Reusable.Companion.signature
import views.DislikeButton
import views.LikeButton
import java.util.*

class BankerAdapter(query: Query?, userID: String?, context: Context, private val anchorSnackbar: Boolean) : FirestoreRecyclerAdapter<Post, BankerPostHolder>(FirestoreRecyclerOptions.Builder<Post>()
        .setQuery(query!!, Post::class.java)
        .build()) {
    private val TAG = "BankerAdaper"
    private val context: Context
    private var userId: String? = null
    private val calculations: Calculations
    private val storageReference: StorageReference
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BankerPostHolder, position: Int, model: Post) {
        Log.i(TAG, "onBindViewHolder: executed")
        var makeVisible = false
        var makePublic = false
        val postId = snapshots.getSnapshot(position).id
        holder.mUsername.setText(model.username)
        holder.imgStatus.setVisibility(if (model.status == 1) View.GONE else View.VISIBLE)
        if (model.bookingCode != null && !model.bookingCode.isEmpty()) {
            holder.mCode.setText(model.bookingCode + " @" + code[model.recommendedBookie - 1])
            holder.mCode.setVisibility(View.VISIBLE)
        } else holder.mCode.setVisibility(View.GONE)
        if (model.type == 0) {
            holder.mType.setVisibility(View.GONE)
        } else {
            holder.mType.setVisibility(View.VISIBLE)
            holder.mType.setText(type[model.type - 1])
        }
        if (model.userId == userId) makeVisible = true else if (UserNetwork.getSubscribed() != null && UserNetwork.getSubscribed().contains(userId)) makeVisible = true
        if (model.status == 2 || Date().time - model.time > 18 * 60 * 60 * 1000) makePublic = true
        if (makeVisible || makePublic) {
            holder.lnrSub.setVisibility(View.GONE)
        } else {
            holder.mpost.setMaxLines(6)
            holder.mSub.setText("Subscribe to " + model.username)
        }
        GlideApp.with(context)
                .load(storageReference.child(model.userId))
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(model.userId[0]))
                .signature(ObjectKey(model.userId + "_" + signature))
                .into(holder.imgDp)

        //listen to dp click and open user profile
        holder.imgDp.setOnClickListener { v ->
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }

        //listen to username click and open user profile
        holder.mUsername.setOnClickListener { v ->
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        holder.mpost.setText(model.content)
        applyLinkfy(context, model.content, holder.mpost)
        holder.mTime.setText(getTime(model.time))
        holder.imgLikes.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
        holder.imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
        holder.mComment.setText(if (model.commentsCount == 0L) "" else model.commentsCount.toString())
        holder.mLikes.setText(if (model.likesCount == 0L) "" else model.likesCount.toString())
        holder.mDislikes.setText(if (model.dislikesCount == 0L) "" else model.dislikesCount.toString())
        val finalMakePublic = makePublic
        val finalMakeVisible = makeVisible
        holder.imgShare.setOnClickListener { v ->
            if (!finalMakePublic) {
                Snackbar.make(holder.mComment, context.resources.getString(R.string.str_cannot_share_post), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareTips(holder.imgShare.getContext(), model.username, model.content)
        }
        holder.mpost.setOnClickListener { v ->
            //display full post with comments if visibility or public is set true
            if (!finalMakePublic && !finalMakeVisible) {
                val intent = Intent(context, SubscriptionActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
                return@setOnClickListener
            }
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        holder.lnrContainer.setOnClickListener { v ->
            //display full post with comments if visibility or public is set true
            if (!finalMakePublic && !finalMakeVisible) {
                val intent = Intent(context, SubscriptionActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
                return@setOnClickListener
            }
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        holder.imgComment.setOnClickListener { v ->
            //display full post with comments if visibility or public is set true
            if (!finalMakePublic && !finalMakeVisible) {
                Snackbar.make(holder.mComment, "Access denied", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        holder.imgLikes.setOnClickListener { v ->
            if (userId == Calculations.GUEST) {
                loginPrompt(holder.imgLikes)
                return@setOnClickListener
            }
            if (holder.imgDislike.getState() === DislikeButton.DISLIKED) {
                holder.imgLikes.setState(LikeButton.LIKED)
                holder.imgDislike.setState(LikeButton.NOT_LIKED)
                holder.mLikes.setText((model.likesCount + 1).toString())
                holder.mDislikes.setText(if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else "")
            } else {
                if (holder.imgLikes.getState() === LikeButton.LIKED) {
                    holder.imgLikes.setState(LikeButton.NOT_LIKED)
                    holder.mLikes.setText(if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else "")
                } else {
                    holder.imgLikes.setState(LikeButton.LIKED)
                    holder.mLikes.setText((model.likesCount + 1).toString())
                }
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onLike(postId, userId, model.userId, substring)
        }
        holder.imgDislike.setOnClickListener { v ->
            if (userId == Calculations.GUEST) {
                loginPrompt(holder.imgDislike)
                return@setOnClickListener
            }
            if (holder.imgLikes.getState() === LikeButton.LIKED) {
                holder.imgLikes.setState(LikeButton.NOT_LIKED)
                holder.imgDislike.setState(DislikeButton.DISLIKED)
                holder.mLikes.setText(if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else "")
                holder.mDislikes.setText((model.dislikesCount + 1).toString())
            } else {
                if (holder.imgDislike.getState() === DislikeButton.DISLIKED) {
                    holder.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                    holder.mDislikes.setText(if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else "")
                } else {
                    holder.imgDislike.setState(DislikeButton.DISLIKED)
                    holder.mDislikes.setText((model.dislikesCount + 1).toString())
                }
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onDislike(postId, userId, model.userId, substring)
        }
        holder.imgOverflow.setOnClickListener { v -> displayOverflow(model, model.userId, postId, model.status, model.type, holder.imgOverflow, finalMakePublic) }
    }

    private fun displayOverflow(model: Post, userID: String, postId: String, status: Int, type: Int, imgOverflow: ImageView,
                                makePublic: Boolean) {
        val builder = AlertDialog.Builder(imgOverflow.rootView.context)
        val inflater = LayoutInflater.from(imgOverflow.rootView.context)
        val dialogView: View
        dialogView = if (userID == userId) inflater.inflate(R.layout.dialog_mine, null) else inflater.inflate(R.layout.dialog_member, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val btnSubmit: Button
        val btnDelete: Button
        val btnRepost: Button
        val btnFollow: Button
        btnSubmit = dialog.findViewById(R.id.btnSubmit)
        btnDelete = dialog.findViewById(R.id.btnDelete)
        btnRepost = dialog.findViewById(R.id.btnRepost)
        btnFollow = dialog.findViewById(R.id.btnFollow)
        val timeDifference = Date().time - model.time
        if (model.userId == userId && model.type > 0 && timeDifference > 9000000) btnDelete.isEnabled = false
        if (model.userId == userId && model.type == 0) btnSubmit.visibility = View.GONE else if (model.userId == userId && timeDifference > 144000000) btnSubmit.visibility = View.GONE else {
            if (model.userId == userId && model.status == 2 && timeDifference <= 9000000) btnSubmit.text = "CANCEL WON"
            if (model.userId == userId && model.status == 2 && timeDifference > 9000000) btnSubmit.visibility = View.GONE
        }
        if (!makePublic) {
            btnRepost.visibility = View.GONE
        }
        if (UserNetwork.getFollowing() == null) btnFollow.visibility = View.GONE else btnFollow.text = if (UserNetwork.getFollowing().contains(userID)) "UNFOLLOW" else "FOLLOW"
        btnSubmit.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (model.type > 0) popUp()
                dialog.cancel()
            }

            private fun popUp() {
                val message = """
                    <p><span style="color: #F80051; font-size: 16px;"><strong>Your tips have delivered?</strong></span></p>
                    <p>By clicking 'YES', you confirm that your prediction has delivered.</p>
                    <p>Your account may be suspended or terminated if that's not true.</p>
                    """.trimIndent()
                val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
                builder.setMessage(Html.fromHtml(message))
                        .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> calculations.onPostWon(imgOverflow, postId, userId, type, anchorSnackbar) }
                        .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
                        .show()
            }
        })
        btnDelete.setOnClickListener { v: View? ->
            if (btnDelete.text.toString().toLowerCase() == "flag") {
                val intent = Intent(context, FlagActivity::class.java)
                intent.putExtra("postId", postId)
                intent.putExtra("reportedUsername", model.username)
                intent.putExtra("reportedUserId", userID)
                context.startActivity(intent)
                dialog.cancel()
            } else {
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type, anchorSnackbar) else {
                    firebaseFirestore!!.collection("posts").document(postId).delete()
                    Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
            }
            dialog.cancel()
        }
        btnRepost.setOnClickListener { v: View? ->
            if (userId == Calculations.GUEST) {
                dialog.cancel()
                loginPrompt(btnRepost)
                return@setOnClickListener
            }
            val intent = Intent(context, RepostActivity::class.java)
            intent.putExtra("postId", postId)
            intent.putExtra("model", model)
            context.startActivity(intent)
            shareTips(btnRepost.context, model.username, model.content)
            dialog.cancel()
        }
        btnFollow.setOnClickListener { v: View? ->
            if (!getNetworkAvailability(context)) {
                if (anchorSnackbar) Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.bottom_navigation).show() else Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                dialog.cancel()
                return@setOnClickListener
            }
            if (userId == Calculations.GUEST) {
                loginPrompt(btnFollow)
                return@setOnClickListener
            }
            if (btnFollow.text == "FOLLOW") {
                calculations.followMember(imgOverflow, userId, userID, anchorSnackbar)
            } else unfollowPrompt(imgOverflow, userID, model.username)
            dialog.cancel()
        }
    }

    private fun loginPrompt(view: View) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(view.context, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { dialogInterface: DialogInterface?, i: Int -> view.context.startActivity(Intent(view.context, LoginActivity::class.java)) }
                .show()
    }

    private fun unfollowPrompt(imgOverflow: ImageView, userID: String, username: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> calculations.unfollowMember(imgOverflow, userId, userID, anchorSnackbar) }
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankerPostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_banker, parent, false)
        return BankerPostHolder(view)
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    init {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        Log.i(TAG, "BankerAdapter: created")
        this.context = context
        setUserId(userID)
        calculations = Calculations(context)
        storageReference = firebaseStorage!!.reference.child("profile_images")
    }
}