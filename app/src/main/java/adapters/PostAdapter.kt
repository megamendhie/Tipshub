package adapters

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sqube.tipshub.*
import com.sqube.tipshub.activities.*
import com.sqube.tipshub.databinding.ItemPostBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.GUEST
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.shareTips
import com.sqube.tipshub.views.DislikeButton
import com.sqube.tipshub.views.LikeButton
import java.util.*

class PostAdapter(response: FirestoreRecyclerOptions<Post?>?, userID: String, context: Context) : FirestoreRecyclerAdapter<Post, PostHolder>(response!!) {
    private val TAG = "PostAdapter"
    private val context: Context
    private var userId: String = userID
    private val storageReference: StorageReference
    private val calculations: Calculations
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PostHolder, position: Int, model: Post) {
        Log.i(TAG, "onBindViewHolder: executed")
        if (model.type == 6 && getItem(position).status != 2) {
            holder.binding.root.visibility = View.GONE
            holder.binding.root.layoutParams = RecyclerView.LayoutParams(0,0)
            return
        }
        val postId = snapshots.getSnapshot(position).id
        val binding = holder.binding
        holder.setPostId(postId)

        //bind data to views
        with(binding){
            txtUsername.text = model.username
            imgStatus.visibility = if (model.status == 1) View.GONE else View.VISIBLE
            crdChildPost.visibility = if (model.isHasChild) View.VISIBLE else View.GONE
            txtPost.text = model.content
            applyLinkfy(context, model.content, txtPost)
            txtTime.text = getTime(model.time)
            imgLike.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
            imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
            txtComment.text = if (model.commentsCount == 0L) "" else model.commentsCount.toString()
            txtLike.text = if (model.likesCount == 0L) "" else model.likesCount.toString()
            txtDislike.text = if (model.dislikesCount == 0L) "" else model.dislikesCount.toString()
            if (model.isHasChild) displayChildContent(model, this)

            GlideApp.with(context).load(storageReference.child(model.userId))
                    .placeholder(getPlaceholderImage(model.userId[0])).error(getPlaceholderImage(model.userId[0]))
                    .signature(ObjectKey(model.userId + "_" + signature)).into(imgDp)
        }

        if (model.bookingCode != null && !model.bookingCode.isEmpty()) {
            binding.txtCode.text = String.format(Locale.getDefault(), "%s @%s", model.bookingCode, code[model.recommendedBookie - 1])
            binding.txtCode.visibility = View.VISIBLE
        } else binding.txtCode.visibility = View.GONE
        if (model.type == 0) binding.txtPostType.visibility = View.GONE
        else {
            binding.txtPostType.visibility = View.VISIBLE
            binding.txtPostType.text = type[model.type - 1]
        }

        //listen to dp click and open user profile
        binding.imgDp.setOnClickListener {
            if (model.userId == userId) context.startActivity(Intent(context, MyProfileActivity::class.java))
            else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        //listen to username click and open user profile
        binding.txtUsername.setOnClickListener {
            if (model.userId == userId) context.startActivity(Intent(context, MyProfileActivity::class.java))
            else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }

        binding.imgShare.setOnClickListener { shareTips(binding.root.context, model.username, model.content) }
        binding.txtPost.setOnClickListener {
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgComment.setOnClickListener {
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgLike.setOnClickListener { view ->
            if (userId == GUEST) {
                loginPrompt(binding.imgLike)
                return@setOnClickListener
            }
            if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
                binding.imgLike.setState(LikeButton.LIKED)
                binding.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                binding.txtLike.text = (model.likesCount + 1).toString()
                binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
            }
            else if (binding.imgLike.getState() == LikeButton.LIKED) {
                binding.imgLike.setState(LikeButton.NOT_LIKED)
                binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
            }
            else {
                binding.imgLike.setState(LikeButton.LIKED)
                binding.txtLike.text = (model.likesCount + 1).toString()
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
            }
            else if (binding.imgDislike.getState() == DislikeButton.DISLIKED) {
                binding.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
            }
            else {
                binding.imgDislike.setState(DislikeButton.DISLIKED)
                binding.txtDislike.text = (model.dislikesCount + 1).toString()
            }

            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onDislike(postId, userId, model.userId, substring)
        }
        binding.imgOverflow.setOnClickListener { v -> displayOverflow(model, model.userId, postId, model.status, model.type, binding.imgOverflow) }
    }

    private fun displayChildContent(model: Post, binding: ItemPostBinding) {
        with(binding){
            if (model.childBookingCode != null && !model.childBookingCode.isEmpty()) {
                txtChildCode.text = String.format(Locale.getDefault(), "%s @%s", model.childBookingCode, code[model.childBookie - 1])
                txtChildCode.visibility = View.VISIBLE
            } else txtChildCode.visibility = View.GONE
            if (model.childType == 0) {
                txtChildType.visibility = View.GONE
            } else {
                txtChildType.visibility = View.VISIBLE
                txtChildType.text = type[model.childType - 1]
            }
            txtChildUsername.text = model.childUsername
            txtChildPost.text = model.childContent
            applyLinkfy(context, model.childContent, txtChildPost)
            firebaseFirestore!!.collection("posts").document(model.childLink).get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        if (!documentSnapshot.exists()) return@addOnSuccessListener
                        binding.imgChildStatus.visibility = if (documentSnapshot.toObject(Post::class.java)!!.status == 1) View.INVISIBLE else View.VISIBLE
                    }
            GlideApp.with(context).load(storageReference.child(model.childUserId))
                    .placeholder(getPlaceholderImage(model.childUserId[0]))
                    .error(getPlaceholderImage(model.childUserId[0]))
                    .signature(ObjectKey(model.childUserId + "_" + signature))
                    .into(childDp)
        }

        binding.childDp.setOnClickListener {
            if (model.childUserId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.childUserId)
                context.startActivity(intent)
            }
        }
        binding.txtChildUsername.setOnClickListener {
            if (model.childUserId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.childUserId)
                context.startActivity(intent)
            }
        }
    }

    private fun loginPrompt(view: View) {
        val builder = AlertDialog.Builder(view.rootView.context, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { _: DialogInterface?, i: Int -> view.context.startActivity(Intent(view.context, LoginActivity::class.java)) }
                .show()
    }

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
    private fun displayOverflow(model: Post, userID: String, postId: String, status: Int, type: Int, imgOverflow: ImageView) {
        val builder = android.app.AlertDialog.Builder(imgOverflow.rootView.context)
        val inflater = LayoutInflater.from(imgOverflow.rootView.context)
        val dialogView = if (userID == userId) inflater.inflate(R.layout.dialog_mine, null) else inflater.inflate(R.layout.dialog_member, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)
        val btnRepost = dialog.findViewById<Button>(R.id.btnRepost)
        val btnFollow = dialog.findViewById<Button>(R.id.btnFollow)
        val timeDifference = Date().time - model.time
        if (model.userId == userId && model.type > 0 && timeDifference > 9000000) btnDelete.isEnabled = false
        if (model.userId == userId && model.type == 0) btnSubmit.visibility = View.GONE else if (model.userId == userId && timeDifference > 144000000) btnSubmit.visibility = View.GONE else {
            if (model.userId == userId && model.status == 2 && timeDifference <= 9000000) btnSubmit.text = "CANCEL WON"
            if (model.userId == userId && model.status == 2 && timeDifference > 9000000) btnSubmit.visibility = View.GONE
        }
        btnDelete.setOnClickListener { v: View? ->
            if (btnDelete.text.toString().toLowerCase() == "flag") {
                val intent = Intent(context, FlagActivity::class.java)
                intent.putExtra("postId", postId)
                intent.putExtra("reportedUsername", model.username)
                intent.putExtra("reportedUserId", userID)
                context.startActivity(intent)
                dialog.cancel()
            } else {
                Log.i(TAG, "onClick: " + model.type)
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type) else {
                    firebaseFirestore!!.collection("posts").document(postId).delete()
                    Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
                dialog.cancel()
            }
        }
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
                val builder = AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
                builder.setMessage(Html.fromHtml(message))
                        .setPositiveButton("Yes") { _: DialogInterface?, i: Int -> calculations.onPostWon(imgOverflow, postId, userId, type) }
                        .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                        .show()
            }
        })

        if (UserNetwork.following == null) btnFollow.visibility = View.GONE else btnFollow.text = if (UserNetwork.following.contains(userID)) "UNFOLLOW" else "FOLLOW"
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
            dialog.cancel()
        }
        btnFollow.setOnClickListener { v: View? ->
            if (!getNetworkAvailability(context)) {
                Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show()
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
        val builder = AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { _: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { _: DialogInterface?, i: Int -> calculations.unfollowMember(imgOverflow, userId, userID) }
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    fun setUserId(userId: String){
        this.userId = userId
    }

    init {
        Log.i(TAG, "PostAdapter: created")
        this.context = context
        setUserId(userID)
        calculations = Calculations(context)
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    }
}