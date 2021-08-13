package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import models.Website
import java.util.*

class WebsiteAdapter(var siteList: ArrayList<Website>?) : RecyclerView.Adapter<WebsiteHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebsiteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sport_site, parent, false)
        return WebsiteHolder(view)
    }

    override fun onBindViewHolder(holder: WebsiteHolder, position: Int) {
        holder.setDisplay(siteList!![position])
    }

    override fun getItemCount(): Int {
        return if (siteList == null || siteList!!.isEmpty()) 0 else siteList!!.size
    }
}