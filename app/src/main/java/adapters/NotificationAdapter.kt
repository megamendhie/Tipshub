package adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sqube.tipshub.activities.FullPostActivity
import com.sqube.tipshub.activities.MemberProfileActivity
import com.sqube.tipshub.activities.MyProfileActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemNotificationBinding
import com.sqube.tipshub.models.Notification
import services.GlideApp
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.Reusable.Companion.getTime
import com.sqube.tipshub.utils.Reusable.Companion.signature
import com.sqube.tipshub.utils.TIPSHUB

class NotificationAdapter(query: Query?, userID: String) : FirestoreRecyclerAdapter<Notification, NotificationAdapter.PostHolder>(FirestoreRecyclerOptions.Builder<Notification>()
        .setQuery(query!!, Notification::class.java)
        .build()) {
    private val tag = "postAdapter"
    private val userId: String
    private val storageReference: StorageReference
    init {
        Log.i(tag, "PostAdapter: created")
        userId = userID
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PostHolder, position: Int, model: Notification) {
        Log.i(tag, "onBindViewHolder: executed")
        holder.onBind(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    inner class PostHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root){

        fun onBind(model: Notification){
            with(model){
                binding.txtTime.text = getTime(time)
                binding.txtTitle.text = title
                binding.txtMessage.text = message
                binding.imgType.visibility = View.VISIBLE
            }

            when (model.action) {
                "liked" -> Glide.with(binding.root.context).load(R.drawable.ic_thumbs_up_color_alt).into(binding.imgType)
                "disliked" -> Glide.with(binding.root.context).load(R.drawable.ic_thumbs_down_color_alt).into(binding.imgType)
                "reposted" -> Glide.with(binding.root.context).load(R.drawable.ic_retweet_color).into(binding.imgType)
                "subEnd", "subscribed" -> Glide.with(binding.root.context).load(R.drawable.ic_favorite_color_24dp).into(binding.imgType)
                else -> binding.imgType.visibility = View.INVISIBLE
            }

            if (model.sentFrom == TIPSHUB) GlideApp.with(binding.root.context).load(R.drawable.icn_mid).into(binding.imgDp)
            else GlideApp.with(binding.root.context).load(storageReference.child(model.sentFrom))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(model.sentFrom[0]))
                    .signature(ObjectKey(model.sentFrom + "_" + signature))
                    .into(binding.imgDp)

            binding.container.setOnClickListener {
                if (model.intentUrl == TIPSHUB) return@setOnClickListener
                when (model.type) {
                    "comment", "post" -> {
                        val intent = Intent(binding.root.context, FullPostActivity::class.java)
                        intent.putExtra("postId", model.intentUrl)
                        binding.root.context.startActivity(intent)
                    }
                    "following", "subscription" -> if (model.sentFrom == userId) {
                        binding.root.context.startActivity(Intent(binding.root.context, MyProfileActivity::class.java))
                    } else {
                        val intent = Intent(binding.root.context, MemberProfileActivity::class.java)
                        intent.putExtra("userId", model.sentFrom)
                        binding.root.context.startActivity(intent)
                    }
                }
            }
            binding.imgDp.setOnClickListener {
                if (model.sentFrom == TIPSHUB) return@setOnClickListener
                if (model.sentFrom == userId) {
                    binding.root.context.startActivity(Intent(binding.root.context, MyProfileActivity::class.java))
                } else {
                    val intent = Intent(binding.root.context, MemberProfileActivity::class.java)
                    intent.putExtra("userId", model.sentFrom)
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }
}