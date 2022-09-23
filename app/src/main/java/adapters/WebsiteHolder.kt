package adapters

import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemSportSiteBinding
import com.sqube.tipshub.models.Website

class WebsiteHolder internal constructor(private val binding: ItemSportSiteBinding) : RecyclerView.ViewHolder(binding.root) {
    private val customTab = CustomTabsIntent.Builder()
            .setToolbarColor(binding.root.context.resources.getColor(R.color.colorPrimary))
            .build()
    fun setDisplay(website: Website) {
        with(binding){
            txtName.text = website.name
            root.setOnClickListener { customTab.launchUrl(root.context, Uri.parse(website.link)) }
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(website.icon)
            storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                Log.i("PeopleAdapter", "onComplete: $uri")
                try {
                    Glide.with(imgIcon.context).load(uri.toString()).fitCenter().into(imgIcon)
                } catch (e: Exception) {
                    Log.w("{PeopleAdapter", "imgIcon GlideApp: " + e.message)
                }
            }
        }
    }

}