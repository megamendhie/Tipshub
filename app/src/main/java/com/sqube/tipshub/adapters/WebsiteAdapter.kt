package com.sqube.tipshub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.databinding.ItemSportSiteBinding
import com.sqube.tipshub.models.Website
import java.util.*

class WebsiteAdapter(val siteList: ArrayList<Website>) : RecyclerView.Adapter<WebsiteHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebsiteHolder {
        val binding = ItemSportSiteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WebsiteHolder(binding)
    }

    override fun onBindViewHolder(holder: WebsiteHolder, position: Int) {
        holder.setDisplay(siteList[position])
    }

    override fun getItemCount(): Int {
        return siteList.size
    }
}