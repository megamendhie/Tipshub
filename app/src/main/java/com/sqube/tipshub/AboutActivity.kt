package com.sqube.tipshub

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import utils.AboutUtil

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
            actionBar.title = "About"
        }
        val listAbout = findViewById<RecyclerView>(R.id.listAbout)
        val showCongratsImage = intent.getBooleanExtra("showCongratsImage", false)
        val imgCongrats = findViewById<ImageView>(R.id.imgCongrats)
        imgCongrats.visibility = if (showCongratsImage) View.VISIBLE else View.GONE
        listAbout.layoutManager = LinearLayoutManager(this)
        listAbout.adapter = Adapt()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    inner class Adapt internal constructor() : RecyclerView.Adapter<Holder>() {
        var aboutList = AboutUtil.aboutList
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.item_about, viewGroup, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, i: Int) {
            holder.bindText(aboutList[i])
        }

        override fun getItemCount(): Int {
            return aboutList.size
        }
    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeading: TextView
        var txtBody: TextView
        private fun bindText(model: Map<String, String>) {
            txtHeading.text = model["heading"]
            txtBody.text = Html.fromHtml(model["body"])
        }

        init {
            txtHeading = itemView.findViewById(R.id.txtHeading)
            txtBody = itemView.findViewById(R.id.txtBody)
        }
    }
}