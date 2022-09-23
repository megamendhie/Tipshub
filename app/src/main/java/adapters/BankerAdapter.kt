package adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.sqube.tipshub.*
import com.sqube.tipshub.activities.*
import com.sqube.tipshub.databinding.ItemPostBankerBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.FirebaseUtil.firebaseStorage
import com.sqube.tipshub.utils.GUEST
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.shareTips
import com.sqube.tipshub.views.DislikeButton
import com.sqube.tipshub.views.LikeButton
import java.util.*

class BankerAdapter(query: Query?, userID: String, val context: Context, private val anchorSnackbar: Boolean) : FirestoreRecyclerAdapter<Post, BankerAdapter.BankerPostHolder>(FirestoreRecyclerOptions.Builder<Post>()
        .setQuery(query!!, Post::class.java)
        .build()) {
    private var userId: String = userID
    private val calculations: Calculations
    private val storageReference = firebaseStorage!!.reference.child("profile_images")
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BankerPostHolder, position: Int, model: Post) {
        val binding = holder.binding
        var makeVisible = false
        var makePublic = false
        val postId = snapshots.getSnapshot(position).id

        with(binding){
            txtUsername.text = model.username
            imgStatus.visibility = if (model.status == 1) View.GONE else View.VISIBLE
            if (model.bookingCode != null && !model.bookingCode.isEmpty()) {
                txtCode.text = String.format("${model.bookingCode} @${code[model.recommendedBookie - 1]}")
                txtCode.visibility = View.VISIBLE
            } else txtCode.visibility = View.GONE
            if (model.type == 0) {
                txtPostType.visibility = View.GONE
            } else {
                txtPostType.visibility = View.VISIBLE
                txtPostType.text = type[model.type - 1]
            }
            if (model.userId == userId) makeVisible = true else if (UserNetwork.subscribed != null && UserNetwork.subscribed.contains(userId)) makeVisible = true
            if (model.status == 2 || Date().time - model.time > 18 * 60 * 60 * 1000) makePublic = true
            if (makeVisible || makePublic) {
                lnrSub.visibility = View.GONE
            } else {
                txtPost.maxLines = 6
                txtSub.text = String.format("Subscribe to ${model.username}")
            }
            txtPost.text = model.content
            applyLinkfy(context, model.content, txtPost)
            txtTime.text = getTime(model.time)
            imgLike.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
            imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
            txtComment.text = if (model.commentsCount == 0L) "" else model.commentsCount.toString()
            txtLike.text = if (model.likesCount == 0L) "" else model.likesCount.toString()
            txtDislike.text = if (model.dislikesCount == 0L) "" else model.dislikesCount.toString()
            GlideApp.with(root.context).load(storageReference.child(model.userId))
                    .placeholder(R.drawable.dummy).error(getPlaceholderImage(model.userId[0]))
                    .signature(ObjectKey(model.userId + "_" + signature)).into(imgDp)
        }

        //listen to dp click and open user profile
        binding.imgDp.setOnClickListener { v ->
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }

        //listen to username click and open user profile
        binding.txtUsername.setOnClickListener { v ->
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        val finalMakePublic = makePublic
        val finalMakeVisible = makeVisible
        binding.imgShare.setOnClickListener {
            if (!finalMakePublic) {
                Snackbar.make(it, context.resources.getString(R.string.str_cannot_share_post), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareTips(binding.root.context, model.username, model.content)
        }
        binding.txtPost.setOnClickListener { v ->
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
        binding.containerPost.setOnClickListener { v ->
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
        binding.imgComment.setOnClickListener {
            //display full post with comments if visibility or public is set true
            if (!finalMakePublic && !finalMakeVisible) {
                Snackbar.make(it, "Access denied", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgLike.setOnClickListener {
            if (userId == GUEST) {
                loginPrompt(binding.imgLike)
                return@setOnClickListener
            }
            if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
                binding.imgLike.setState(LikeButton.LIKED)
                binding.imgDislike.setState(LikeButton.NOT_LIKED)
                binding.txtLike.text = (model.likesCount + 1).toString()
                binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
            } else {
                if (binding.imgLike.getState() == LikeButton.LIKED) {
                    binding.imgLike.setState(LikeButton.NOT_LIKED)
                    binding.txtLike.setText(if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else "")
                } else {
                    binding.imgLike.setState(LikeButton.LIKED)
                    binding.txtLike.setText((model.likesCount + 1).toString())
                }
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onLike(postId, userId, model.userId, substring)
        }
        binding.imgDislike.setOnClickListener { v ->
            if (userId == GUEST) {
                loginPrompt(binding.imgDislike)
                return@setOnClickListener
            }
            if (binding.imgLike.getState() == LikeButton.LIKED) {
                binding.imgLike.setState(LikeButton.NOT_LIKED)
                binding.imgDislike.setState(DislikeButton.DISLIKED)
                binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
                binding.txtDislike.text = (model.dislikesCount + 1).toString()
            } else {
                if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
                    binding.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                    binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
                } else {
                    binding.imgDislike.setState(DislikeButton.DISLIKED)
                    binding.txtDislike.text = (model.dislikesCount + 1).toString()
                }
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onDislike(postId, userId, model.userId, substring)
        }
        binding.imgOverflow.setOnClickListener { displayOverflow(model, model.userId, postId, model.status, model.type, binding.imgOverflow, finalMakePublic) }
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
                        .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> calculations.onPostWon(imgOverflow, postId, userId, type) }
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
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type) else {
                    firebaseFirestore!!.collection("posts").document(postId).delete()
                    Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
            }
            dialog.cancel()
        }
        btnRepost.setOnClickListener { v: View? ->
            if (userId == GUEST) {
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
            if (userId == GUEST) {
                loginPrompt(btnFollow)
                return@setOnClickListener
            }
            if (btnFollow.text == "FOLLOW") {
                calculations.followMember(imgOverflow, userId, userID)
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
                .setNegativeButton("No") { _: DialogInterface?, _: Int -> }
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> calculations.unfollowMember(imgOverflow, userId, userID)}
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankerPostHolder {
        val binding = ItemPostBankerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankerPostHolder(binding)
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    inner class BankerPostHolder(val binding: ItemPostBankerBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        setUserId(userID)
        calculations = Calculations(context)
    }
}