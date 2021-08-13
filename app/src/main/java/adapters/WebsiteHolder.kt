package adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemSportSiteBinding
import models.Website

class WebsiteHolder internal constructor(private val binding: ItemSportSiteBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setDisplay(website: Website) {
        with(binding){
            txtName.text = website.name
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(website.icon)
            storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                Log.i("PeopleAdapter", "onComplete: $uri")
                try {
                    Glide.with(imgIcon.context).load(uri.toString()).fitCenter().into(imgIcon)
                } catch (e: Exception) {
                    Log.w("{PeopleAdapter", "imgIcon GlideApp: " + e.message)
                }
            }
            root.setOnClickListener { root.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website.link)))
            }
        }

    }

}