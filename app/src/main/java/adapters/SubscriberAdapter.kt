package adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.MemberProfileActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemSubscriptionBinding
import models.Subscription
import services.GlideApp
import utils.Reusable.Companion.getNewDate
import utils.Reusable.Companion.getPlaceholderImage

class SubscriberAdapter(query: Query?) : FirestoreRecyclerAdapter<Subscription, SubscriptionAdapter.SubscriptionViewHolder>(FirestoreRecyclerOptions.Builder<Subscription>()
        .setQuery(query!!, Subscription::class.java)
        .build()) {
    private val storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    private val status = arrayOf("", "pending", "PAID")
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SubscriptionAdapter.SubscriptionViewHolder, position: Int, model: Subscription) {
        with(holder.binding){
            txtUsername.text = model.subFrom
            txtStarDate.text = getNewDate(model.dateStart)
            txtEndDate.text = getNewDate(model.dateEnd)
            txtAmount.text = Html.fromHtml(model.tipsterAmount)

            if (model.status < status.size) txtStatus.text = status[model.status]
            GlideApp.with(root.context).load(storageReference.child(model.subFromId))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(model.subFromId[0]))
                    .into(imgDp)
            txtUsername.setOnClickListener { v ->
                val intent = Intent(root.context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.subFromId)
                root.context.startActivity(intent)
            }
            imgDp.setOnClickListener { v ->
                val intent = Intent(root.context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.subFromId)
                root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionAdapter.SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionAdapter.SubscriptionViewHolder(binding)
    }

}