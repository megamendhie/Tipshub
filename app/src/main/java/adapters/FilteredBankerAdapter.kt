package adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.*
import com.sqube.tipshub.activities.*
import com.sqube.tipshub.databinding.ItemPostBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.SnapId
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.shareTips
import com.sqube.tipshub.views.DislikeButton
import com.sqube.tipshub.views.LikeButton
import java.util.*

class FilteredBankerAdapter(userID: String, val context: Context, private val postList: ArrayList<Post>, private val snapIds: ArrayList<SnapId>) : RecyclerView.Adapter<FilteredBankerAdapter.FilteredPostHolder>() {
    private val TAG = "PostAdapter"
    private val userId = userID
    private val storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    private val calculations = Calculations(context)
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    override fun getItemCount(): Int {
        return snapIds.size
    }

    private fun displayOverflow(model: Post, userID: String, postId: String, status: Int, type: Int, imgOverflow: ImageView,
                                makePublic: Boolean) {
        val builder = AlertDialog.Builder(imgOverflow.rootView.context)
        val inflater = LayoutInflater.from(imgOverflow.rootView.context)
        val dialogView = if (userID == userId) inflater.inflate(R.layout.dialog_mine, null) else inflater.inflate(R.layout.dialog_member, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val btnSubmit: Button = dialog.findViewById(R.id.btnSubmit)
        val btnDelete: Button = dialog.findViewById(R.id.btnDelete)
        val btnRepost: Button = dialog.findViewById(R.id.btnRepost)
        val btnFollow: Button = dialog.findViewById(R.id.btnFollow)
        val timeDifference = Date().time - model.time
        if (model.userId == userId && model.type > 0 && timeDifference > 9000000) btnDelete.isEnabled = false
        if (model.userId == userId && model.type == 0) btnSubmit.visibility = View.GONE else if (model.userId == userId && timeDifference > 144000000) btnSubmit.visibility = View.GONE else {
            if (model.userId == userId && model.status == 2 && timeDifference <= 9000000) btnSubmit.text = "CANCEL WON"
            if (model.userId == userId && model.status == 2 && timeDifference > 9000000) btnSubmit.visibility = View.GONE
        }
        if (!makePublic) {
            btnRepost.visibility = View.GONE
        }
        if (UserNetwork.following == null) btnFollow.visibility = View.GONE else btnFollow.text = if (UserNetwork.following.contains(userID)) "UNFOLLOW" else "FOLLOW"
        btnDelete.setOnClickListener { v: View? ->
            if (btnDelete.text.toString().toLowerCase() == "flag") {
                val intent = Intent(context, FlagActivity::class.java)
                intent.putExtra("postId", postId)
                intent.putExtra("reportedUsername", model.username)
                intent.putExtra("reportedUserId", userID)
                context.startActivity(intent)
                dialog.cancel()
            } else {
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type)
                else {
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
                calculations.followMember(imgOverflow, userId, userID)
            } else unfollowPrompt(imgOverflow, userID, model.username)
            dialog.cancel()
        }
    }

    private fun unfollowPrompt(imgOverflow: ImageView, userID: String, username: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { _: DialogInterface?, _: Int -> }
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> calculations.unfollowMember(imgOverflow, userId, userID) }
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilteredPostHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilteredPostHolder(binding)
    }

    override fun onBindViewHolder(holder: FilteredPostHolder, i: Int) {
        val position = holder.adapterPosition
        val model = postList[position]
        val postId = snapIds[position].id
        val binding = holder.binding
        val finalMakePublic = model.status == 2 || Date().time - model.time > 18 * 60 * 60 * 1000

        with(binding){
            txtUsername.text = model.username
            imgStatus.visibility = if (model.status == 1) View.GONE else View.VISIBLE
            if (model.bookingCode != null && !model.bookingCode.isEmpty()) {
                txtCode.text = String.format(Locale.ENGLISH, "%s @%s", model.bookingCode, code[model.recommendedBookie - 1])
                txtCode.visibility = View.VISIBLE
            } else txtCode.visibility = View.GONE
            if (model.type == 0) {
                txtPostType.visibility = View.GONE
            } else {
                txtPostType.visibility = View.VISIBLE
                txtPostType.text = type[model.type - 1]
            }
            GlideApp.with(context).load(storageReference.child(model.userId))
                    .placeholder(R.drawable.dummy).error(getPlaceholderImage(model.userId[0]))
                    .signature(ObjectKey(model.userId + "_" + signature)).into(imgDp)

            //listen to username click and open user profile
            txtUsername.setOnClickListener {
                if (model.userId == userId) {
                    context.startActivity(Intent(context, MyProfileActivity::class.java))
                } else {
                    val intent = Intent(context, MemberProfileActivity::class.java)
                    intent.putExtra("userId", model.userId)
                    context.startActivity(intent)
                }
            }
            txtPost.text = model.content
            applyLinkfy(context, model.content, txtPost)
            txtTime.text = getTime(model.time)
            imgLike.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
            imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
            txtComment.text = if (model.commentsCount == 0L) "" else model.commentsCount.toString()
            txtLike.text = if (model.likesCount == 0L) "" else model.likesCount.toString()
            txtDislike.text = if (model.dislikesCount == 0L) "" else model.dislikesCount.toString()
        }

        //listen to dp click and open user profile
        binding.imgDp.setOnClickListener {
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        binding.imgShare.setOnClickListener {
            if (!finalMakePublic) {
                Snackbar.make(it, it.context.resources.getString(R.string.str_cannot_share_post), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareTips(it.context, model.username, model.content)
        }
        binding.containerPost.setOnClickListener {
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgComment.setOnClickListener {
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgLike.setOnClickListener {
            if (!getNetworkAvailability(context)) return@setOnClickListener
            val l = model.likes
            if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
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
                if (binding.imgLike.getState() == LikeButton.LIKED) {
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
        binding.imgDislike.setOnClickListener {
            if (!getNetworkAvailability(context)) return@setOnClickListener
            val dl = model.dislikes
            if (binding.imgLike.getState() == LikeButton.LIKED) {
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
                if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
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
        binding.imgOverflow.setOnClickListener { displayOverflow(model, model.userId, postId, model.status, model.type, binding.imgOverflow, finalMakePublic) }
    }

    inner class FilteredPostHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)
}