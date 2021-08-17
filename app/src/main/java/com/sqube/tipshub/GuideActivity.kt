package com.sqube.tipshub

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GuideActivity : AppCompatActivity(), View.OnClickListener {
    var txtGuidelines: TextView? = null
    var txtPrivacyPolicy: TextView? = null
    var txtCAC: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle("About/Privacy Policy")
        }
        txtGuidelines = findViewById(R.id.txtGuidelines)
        txtGuidelines.setOnClickListener(this)
        txtPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy)
        txtPrivacyPolicy.setOnClickListener(this)
        txtCAC = findViewById(R.id.txtCAC)
        txtCAC.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onClick(v: View) {
        var intent: Intent? = null
        when (v.id) {
            R.id.txtGuidelines -> intent = Intent(this@GuideActivity, AboutActivity::class.java)
            R.id.txtPrivacyPolicy -> {
                intent = Intent(this@GuideActivity, NewsStoryActivity::class.java)
                intent.putExtra("url", "https://tipshub.co/privacy-policy/")
            }
            R.id.txtCAC -> {
                intent = Intent(this@GuideActivity, NewsStoryActivity::class.java)
                intent.putExtra("url", "https://tipshub.co/terms-and-conditions/")
            }
        }
        intent?.let { startActivity(it) }
    }
}