package adapters

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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.sqube.tipshub.*
import com.sqube.tipshub.R
import com.sqube.tipshub.activities.*
import com.sqube.tipshub.databinding.ItemPostBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.SnapId
import com.sqube.tipshub.models.UserNetwork
import services.GlideApp
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.FirebaseUtil.storageReference
import com.sqube.tipshub.utils.Reusable.Companion.applyLinkfy
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.Reusable.Companion.signature
import com.sqube.tipshub.views.DislikeButton
import com.sqube.tipshub.views.LikeButton
import java.util.*

class FilteredPostAdapter(private val search: Boolean, val userId: String, val context: Context, val postList: ArrayList<Post?>,
                          val snapIds: ArrayList<SnapId>) : RecyclerView.Adapter<PostHolder>() {
    private val tag = "PostAdapter"
    private var listener: ListenerRegistration? = null
    private val calculations = Calculations(context)
    private val collectionReference = firebaseFirestore!!.collection("posts")
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type = arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    private val time = Date().time

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (search) listener!!.remove()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int {
        return snapIds.size
    }

    private fun displayChildContent(model: Post?, binding: ItemPostBinding) {

        //holder.imgChildStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if (model!!.childBookingCode != null && !model.childBookingCode.isEmpty()) {
            binding.txtChildCode.text = String.format(Locale.ENGLISH, "%s @%s", model.childBookingCode, code[model.childBookie - 1])
            binding.txtChildCode.visibility = View.VISIBLE
        }
        else binding.txtChildCode.visibility = View.GONE
        if (model.childType == 0) binding.txtChildType.visibility = View.GONE
        else {
            binding.txtChildType.visibility = View.VISIBLE
            binding.txtChildType.text = type[model.childType - 1]
        }
        binding.txtChildUsername.text = model.childUsername
        binding.txtChildPost.text = model.childContent
        applyLinkfy(context, model.childContent, binding.txtChildPost)
        firebaseFirestore!!.collection("posts").document(model.childLink).get()
                .addOnSuccessListener(OnSuccessListener { documentSnapshot ->
                    if (!documentSnapshot.exists()) return@OnSuccessListener
                    binding.imgChildStatus.setVisibility(if (documentSnapshot.toObject(Post::class.java)!!.status == 1) View.INVISIBLE else View.VISIBLE)
                })
        GlideApp.with(context)
                .load(storageReference.child(model.childUserId))
                .placeholder(getPlaceholderImage(model.childUserId[0]))
                .error(getPlaceholderImage(model.childUserId[0]))
                .signature(ObjectKey(model.childUserId + "_" + signature))
                .into(binding.childDp)

        //listen to dp click and open user profile
        binding.childDp.setOnClickListener {
            if (model.childUserId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.childUserId)
                context.startActivity(intent)
            }
        }
        //listen to username click and open user profile
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

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
    private fun displayOverflow(model: Post?, userID: String, postId: String, status: Int, type: Int, imgOverflow: ImageView) {
        val builder = AlertDialog.Builder(imgOverflow.rootView.context)
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
        val timeDifference = Date().time - model!!.time
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
                if (model.type > 0) calculations.onDeletePost(imgOverflow, postId, userId, status == 2, type)
                else {
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
                val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
                builder.setMessage(Html.fromHtml(message))
                        .setPositiveButton("Yes") { _: DialogInterface?, i: Int -> calculations.onPostWon(imgOverflow, postId, userId, type) }
                        .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                        .show()
            }
        })
        if (UserNetwork.following == null) btnFollow.visibility = View.GONE else btnFollow.text = if (UserNetwork.following.contains(userID)) "UNFOLLOW" else "FOLLOW"
        btnRepost.setOnClickListener { v: View? ->
            val intent = Intent(context, RepostActivity::class.java)
            intent.putExtra("postId", postId)
            intent.putExtra("model", model)
            context.startActivity(intent)
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
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> calculations.unfollowMember(imgOverflow, userId, userID) }
                .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostHolder, i: Int) {
        val binding = holder.binding
        val position = holder.adapterPosition
        val postId = snapIds[position].id
        holder.setPostId(postId)
        val model = postList[holder.adapterPosition]
        firebaseFirestore!!.collection("posts").document(postId)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                    Log.i(tag, "onEvent: pos= $position")
                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        if (postList.size > position) {
                            postList.removeAt(position)
                            snapIds.removeAt(position)
                            notifyDataSetChanged()
                        }
                    } else {
                        val snapModel = documentSnapshot.toObject(Post::class.java)
                        binding.imgStatus.visibility = if(snapModel!!.status == 1) View.GONE else View.VISIBLE
                        binding.imgLike.setState(if (snapModel.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
                        binding.imgDislike.setState(if (snapModel.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
                        binding.txtComment.text = if(snapModel.commentsCount == 0L) "" else snapModel.commentsCount.toString()
                        binding.txtLike.text = if(snapModel.likesCount == 0L) "" else snapModel.likesCount.toString()
                        binding.txtDislike.text = if(snapModel.dislikesCount == 0L) "" else snapModel.dislikesCount.toString()
                        if (postList.size > position) postList[position] = snapModel
                    }
                }

        with(binding){
            imgStatus.setVisibility(if (model!!.status == 1) View.GONE else View.VISIBLE)
            imgLike.setState(if (model.likes.contains(userId)) LikeButton.LIKED else LikeButton.NOT_LIKED)
            imgDislike.setState(if (model.dislikes.contains(userId)) DislikeButton.DISLIKED else DislikeButton.NOT_DISLIKED)
            txtComment.text = if (model.commentsCount == 0L) "" else model.commentsCount.toString()
            txtLike.text = if (model.likesCount == 0L) "" else model.likesCount.toString()
            txtDislike.text = if (model.dislikesCount == 0L) "" else model.dislikesCount.toString()
            imgStatus.visibility = if (model.status == 1) View.GONE else View.VISIBLE
            txtUsername.text = model.username
            crdChildPost.visibility = if(model.isHasChild) View.VISIBLE else View.GONE
            txtPost.text = model.content
            applyLinkfy(context, model.content, txtPost)
            txtTime.text = getTime(model.time)
            imgShare.setOnClickListener { shareTips(binding.imgShare.getContext(), model.username, model.content) }

            GlideApp.with(context)
                    .load(storageReference.child(model.userId))
                    .placeholder(getPlaceholderImage(model.userId[0]))
                    .error(getPlaceholderImage(model.userId[0]))
                    .signature(ObjectKey(model.userId + "_" + signature)).into(imgDp)

        }

        if (model!!.bookingCode != null && !model.bookingCode.isEmpty()) {
            binding.txtCode.text = String.format(Locale.ENGLISH, "%s @%s",
                    model.bookingCode, code[model.recommendedBookie - 1])
            binding.txtCode.visibility = View.VISIBLE
        }
        else binding.txtCode.visibility = View.GONE
        if (model.type == 0) binding.txtPostType.visibility = View.GONE
        else {
            binding.txtPostType.visibility = View.VISIBLE
            binding.txtPostType.text = type[model.type - 1]
        }

        binding.imgDp.setOnClickListener {
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        binding.txtUsername.setOnClickListener {
            if (model.userId == userId) {
                context.startActivity(Intent(context, MyProfileActivity::class.java))
            } else {
                val intent = Intent(context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.userId)
                context.startActivity(intent)
            }
        }
        binding.txtPost.setOnClickListener {
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgComment.setOnClickListener { v ->
            val intent = Intent(context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }
        binding.imgLike.setOnClickListener { v ->
            val l = model.likes
            if (binding.imgDislike.getState() === DislikeButton.DISLIKED) {
                binding.imgLike.setState(LikeButton.LIKED)
                binding.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                binding.txtLike.text = (model.likesCount + 1).toString()
                binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""

                //get list of userIds that disliked
                val dl = model.dislikes
                dl.remove(userId)
                l.add(userId)
                model.dislikes = dl
                model.likes = l
                model.likesCount = model.likesCount + 1
                model.dislikesCount = model.dislikesCount - 1
            }
            else {
                //get list of userIds that liked
                if (binding.imgLike.getState() === LikeButton.LIKED) {
                    binding.imgLike.setState(LikeButton.NOT_LIKED)
                    binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
                    l.remove(userId)
                    model.likesCount = model.likesCount - 1
                } else {
                    binding.imgLike.setState(LikeButton.LIKED)
                    binding.txtLike.text = (model.likesCount + 1).toString()
                    l.add(userId)
                    model.likesCount = model.likesCount + 1
                }
                model.likes = l
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onLike(postId, userId, model.userId, substring)
        }
        binding.imgDislike.setOnClickListener { v ->
            val dl = model.dislikes
            if (binding.imgLike.getState() === LikeButton.LIKED) {
                binding.imgLike.setState(LikeButton.NOT_LIKED)
                binding.imgDislike.setState(DislikeButton.DISLIKED)
                binding.txtLike.text = if (model.likesCount - 1 > 0) (model.likesCount - 1).toString() else ""
                binding.txtDislike.text = (model.dislikesCount + 1).toString()

                //get list of userIds that liked
                val l = model.likes
                l.remove(userId)
                dl.add(userId)
                model.likes = l
                model.dislikes = dl
                model.likesCount = model.likesCount - 1
                model.dislikesCount = model.dislikesCount + 1
            }
            else {
                //get list of userIds that disliked
                if (binding.imgDislike.getState() === DislikeButton.DISLIKED) {
                    binding.imgDislike.setState(DislikeButton.NOT_DISLIKED)
                    binding.txtDislike.text = if (model.dislikesCount - 1 > 0) (model.dislikesCount - 1).toString() else ""
                    dl.remove(userId)
                    model.dislikesCount = model.dislikesCount - 1
                } else {
                    binding.imgDislike.setState(DislikeButton.DISLIKED)
                    binding.txtDislike.text = (model.dislikesCount + 1).toString()
                    dl.add(userId)
                    model.dislikesCount = model.dislikesCount + 1
                }
                model.dislikes = dl
            }
            val substring = model.content.substring(0, Math.min(model.content.length, 90))
            calculations.onDislike(postId, userId, model.userId, substring)
        }
        binding.imgOverflow.setOnClickListener { v -> displayOverflow(model, model.userId, postId, model.status, model.type, binding.imgOverflow) }
        if (model.isHasChild) {
            displayChildContent(model, binding)
        }
    }

    init {
        if (search) listener = collectionReference.orderBy("time").startAt(Date().time)
                .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (queryDocumentSnapshots == null) return@addSnapshotListener
                    for (change in queryDocumentSnapshots.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            Log.i(tag, "onEvent: added again $time")
                            val post = change.document.toObject(Post::class.java)
                            if (post.userId != userId) if (UserNetwork.following == null || !UserNetwork.following.contains(post.userId)) return@addSnapshotListener
                            postList.add(0, post)
                            snapIds.add(0, SnapId(change.document.id, post.time))
                            notifyDataSetChanged()
                        }
                    }
                }
    }
}