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
import models.Website

class WebsiteHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imgIcon: ImageView
    private val txtName: TextView
    fun setDisplay(website: Website) {
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
        itemView.setOnClickListener { view: View? ->
            itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                    website.link)))
        }
    }

    init {
        imgIcon = itemView.findViewById(R.id.imgIcon)
        txtName = itemView.findViewById(R.id.txtName)
    }
}