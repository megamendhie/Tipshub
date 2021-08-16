package adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sqube.tipshub.*
import models.Post
import models.SnapId
import models.UserNetwork
import services.GlideApp
import utils.Calculations
import utils.FirebaseUtil.firebaseFirestore
import utils.Reusable.Companion.applyLinkfy
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.getPlaceholderImage
import utils.Reusable.Companion.getTime
import utils.Reusable.Companion.shareTips
import utils.Reusable.Companion.signature
import views.DislikeButton
import views.LikeButton
import java.util.*

class FilteredBankerAdapter(userID: String, context: Context, postList: ArrayList<Post>, snapIds: ArrayList<SnapId>) : RecyclerView.Adapter<BankerPostHolder>() {
    private val TAG = "PostAdapter"
    private val context: Context
    private val userId: String
    private val storageReference: StorageReference
    private val calculations: Calculations
    private val postList: ArrayList<Post>
    private val snapIds: ArrayList<SnapId>
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    override fun getItemCount(): Int {
        return snapIds.size
    }

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
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
        btnDelete.setOnClickListener { v: View? ->
            if (btnDelete.text.toString().toLowerCase() == "flag") {
                val intent = Intent(context, FlagActivity::class.java)
                intent.putExtra("postId", postId)
                intent.putExtra("reportedUsername", model.username)
                intent.putExtra("reportedUserId", userID)
                context.startActivity(intent)
                dialog.cancel()
            } else {
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type, true) else {
                    firebaseFirestore!!.collection("posts").document(postId).delete()
                    Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
            }
            dialog.cancel()
        }
        btnRepost.setOnClickListener { v: View? ->
            val intent = Intent(context, RepostActivity::class.java)
            intent.putExtra("postId", postId)
            intent.putExtra("model", model)
            btnRepost.context.startActivity(intent)
            dialog.cancel()
        }
        btnFollow.setOnClickListener { v: View? ->
            if (!getNetworkAvailability(context)) {
                Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.bottom_navigation).show()
                dialog.cancel()
                return@setOnClickListener
            }
            if (btnFollow.text == "FOLLOW") {
                calculations.followMember(imgOverflow, userId, userID, true)
            } else unfollowPrompt(imgOverflow, userID, model.username)
            dialog.cancel()
        }
    }

    private fun unfollowPrompt(imgOverflow: ImageView, userID: String, username: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> calculations.unfollowMember(imgOverflow, userId, userID, true) }
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankerPostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return BankerPostHolder(view)
    }

    override fun onBindViewHolder(holder: BankerPostHolder, i: Int) {
        val position = holder.adapterPosition
        val model = postList[position]
        val postId = snapIds[position].id
        holder.mUsername.setText(model.username)
        holder.imgStatus.setVisibility(if (model.status == 1) View.GONE else View.VISIBLE)
        if (model.bookingCode != null && !model.bookingCode.isEmpty()) {
            holder.mCode.setText(String.format(Locale.ENGLISH, "%s @%s",
                    model.bookingCode, code[model.recommendedBookie - 1]))
            holder.mCode.setVisibility(View.VISIBLE)
        } else holder.mCode.setVisibility(View.GONE)
        if (model.type == 0) {
            holder.mType.setVisibility(View.GONE)
        } else {
            holder.mType.setVisibility(View.VISIBLE)
            holder.mType.setText(type[model.type - 1])
        }
        GlideApp.with(context)
                .load(storageReference.child(model.userId))
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(model.userId[0]))
                .signature(ObjectKey(model.userId + "_" + signature))
                .into(holder.imgDp)

        //listen to dp click and open user profile
        holder.imgDp.setOnClickListener(View.OnClickListener {
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        })

        //listen to username click and open user profile
        holder.mUsername.setOnClickListener(View.OnClickListener {
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        })
        holder.mpost.setText(model.content)
        applyLinkfy(context, model.content, holder.mpost)
        holder.mTime.setText(getTime(model.time))
        holder.imgLikes.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
        holder.imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
        holder.mComment.setText(if (model.commentsCount == 0L) "" else model.commentsCount.toString())
        holder.mLikes.setText(if (model.likesCount == 0L) "" else model.likesCount.toString())
        holder.mDislikes.setText(if (model.dislikesCount == 0L) "" else model.dislikesCount.toString())
        val finalMakePublic = model.status == 2 || Date().time - model.time > 18 * 60 * 60 * 1000
        holder.imgShare.setOnClickListener { v ->
            if (!finalMakePublic) {
                Snackbar.make(holder.mComment, holder.imgShare.getContext().getResources().getString(R.string.str_cannot_share_post), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareTips(holder.imgShare.getContext(), model.username, model.content)
        }
        holder.lnrContainer.setOnClickListener { v ->
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        holder.imgComment.setOnClickListener { v ->
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        holder.imgLikes.setOnClickListener { v ->
            if (!getNetworkAvailability(context)) return@setOnClickListener
            val l = model.likes
            if (holder.imgDislike.getState() === DislikeButton.DISLIKED) {
                //get list of userIds that disliked
                val dl = model.dislikes
                dl.remove(userId)
                l.add(userId)
                postList[position].dislikes = dl
                postList[position].likes = l
                postList[position].likesCount = model.likesCount + 1
                postList[position].dislikesCount = model.dislikesCount - 1
            } else {
                //get list of userIds that liked
                if (holder.imgLikes.getState() === LikeButton.LIKED) {
                    l.remove(userId)
                    postList[position].likesCount = model.likesCount - 1
                } else {
                    l.add(userId)
                    postList[position].likesCount = model.likesCount + 1
                }
                postList[position].likes = l
            }
            notifyDataSetChanged()
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onLike(postId, userId, model.userId, substring)
        }
        holder.imgDislike.setOnClickListener { v ->
            if (!getNetworkAvailability(context)) return@setOnClickListener
            val dl = model.dislikes
            if (holder.imgLikes.getState() === LikeButton.LIKED) {
                //get list of userIds that liked
                val l = model.likes
                l.remove(userId)
                dl.add(userId)
                postList[position].likes = l
                postList[position].dislikes = dl
                postList[position].likesCount = model.likesCount - 1
                postList[position].dislikesCount = model.dislikesCount + 1
            } else {
                //get list of userIds that disliked
                if (holder.imgDislike.getState() === DislikeButton.DISLIKED) {
                    dl.remove(userId)
                    postList[position].dislikesCount = model.dislikesCount - 1
                } else {
                    dl.add(userId)
                    postList[position].dislikesCount = model.dislikesCount + 1
                }
                postList[position].dislikes = dl
            }
            notifyDataSetChanged()
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onDislike(postId, userId, model.userId, substring)
        }
        holder.imgOverflow.setOnClickListener { v -> displayOverflow(model, model.userId, postId, model.status, model.type, holder.imgOverflow, finalMakePublic) }
    }

    init {
        Log.i(TAG, "PostAdapter: created")
        this.context = context
        userId = userID
        this.postList = postList
        this.snapIds = snapIds
        calculations = Calculations(context)
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    }
}