package com.sqube.tipshub.adapters

import android.content.Intent
import android.text.Html
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.R
import com.sqube.tipshub.activities.MemberProfileActivity
import com.sqube.tipshub.databinding.ItemSubscriptionBinding
import com.sqube.tipshub.models.Subscription
import com.sqube.tipshub.services.GlideApp
import com.sqube.tipshub.utils.Reusable.Companion.getNewDate
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.TIPSHUB

class SubscriptionAdapter(query: Query?) : FirestoreRecyclerAdapter<Subscription, SubscriptionAdapter.SubscriptionViewHolder>(FirestoreRecyclerOptions.Builder<Subscription>()
        .setQuery(query!!, Subscription::class.java)
        .build()) {
    private val storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int, model: Subscription) {
        with(holder.binding){
            txtUsername.text = model.subTo
            txtStarDate.text = getNewDate(model.dateStart)
            txtEndDate.text = getNewDate(model.dateEnd)
            txtAmount.text = Html.fromHtml(model.amount)
            txtStatus.text = if (model.isActive) "active" else "ended"

            if (model.subToId == TIPSHUB)
                GlideApp.with(root.context).load(R.drawable.icn_mid).into(imgDp)
            else
                GlideApp.with(root.context).load(storageReference.child(model.subToId)).placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(model.subToId[0])).into(imgDp)
            txtUsername.setOnClickListener { v: View? ->
                if (model.subToId == TIPSHUB) return@setOnClickListener
                val intent = Intent(root.context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.subToId)
                root.context.startActivity(intent)
            }
            imgDp.setOnClickListener(View.OnClickListener {
                if (model.subToId == TIPSHUB) return@OnClickListener
                val intent = Intent(root.context, MemberProfileActivity::class.java)
                intent.putExtra("userId", model.subToId)
                root.context.startActivity(intent)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionViewHolder(binding)
    }

    class SubscriptionViewHolder(val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) { }

}