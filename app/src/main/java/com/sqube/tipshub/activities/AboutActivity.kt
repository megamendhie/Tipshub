package com.sqube.tipshub.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityAboutBinding
import com.sqube.tipshub.databinding.ItemAboutBinding
import com.sqube.tipshub.utils.AboutUtil

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
            actionBar.title = "About"
        }
        val showCongratsImage = intent.getBooleanExtra("showCongratsImage", false)
        binding.imgCongrats.visibility = if (showCongratsImage) View.VISIBLE else View.GONE
        binding.listAbout.layoutManager = LinearLayoutManager(this)
        binding.listAbout.adapter = Adapt()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    inner class Adapt  : RecyclerView.Adapter<Holder>() {
        private var aboutList = AboutUtil.aboutList
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
            val binding = ItemAboutBinding.inflate(LayoutInflater.from(applicationContext), viewGroup, false)
            return Holder(binding)
        }

        override fun onBindViewHolder(holder: Holder, i: Int) {
            holder.bindText(aboutList[i])
        }

        override fun getItemCount(): Int {
            return aboutList.size
        }
    }

    inner class Holder(val binding: ItemAboutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindText(model: Map<String, String>) {
            with(binding){
                txtHeading.text = model["heading"]
                txtBody.text = HtmlCompat.fromHtml(model["body"]!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }
}