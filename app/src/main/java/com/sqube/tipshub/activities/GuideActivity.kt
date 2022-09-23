package com.sqube.tipshub.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding: ActivityGuideBinding? = null
    private val binding get()= _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "About/Privacy Policy"
        }
        binding.txtGuidelines.setOnClickListener(this)
        binding.txtPrivacyPolicy.setOnClickListener(this)
        binding.txtCAC.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onClick(v: View) {
        val intentBrowser = CustomTabsIntent.Builder().setToolbarColor(resources.getColor(R.color.colorPrimary)).build()
        when (v) {
            binding.txtGuidelines -> startActivity( Intent(this@GuideActivity, AboutActivity::class.java))
            binding.txtPrivacyPolicy -> intentBrowser.launchUrl(this@GuideActivity, Uri.parse("https://tipshub.co/privacy-policy/"))
            binding.txtCAC -> intentBrowser.launchUrl(this@GuideActivity, Uri.parse("https://tipshub.co/terms-and-conditions/"))
        }
    }
}