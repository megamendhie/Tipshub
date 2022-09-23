package com.sqube.tipshub.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding: ActivityContactBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = ""
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        binding.crdWhatsapp.setOnClickListener(this)
        binding.crdTwitter.setOnClickListener(this)
        binding.crdFacebook.setOnClickListener(this)
        binding.crdEmail.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onClick(v: View) {
        when (v) {
            binding.crdEmail -> sendEmail()
            binding.crdWhatsapp -> startWhatsapp()
            binding.crdTwitter -> startBrowser("https://twitter.com/tipshub_co")
            binding.crdFacebook -> startBrowser("https://web.facebook.com/Tipshub-110212083647469")
        }
    }

    private fun sendEmail() {
        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = Uri.parse("mailto:swiftqube@gmail.com")
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Tipshub Help Request")
        startActivity(Intent.createChooser(sendIntent, "Select:"))
    }

    private fun startWhatsapp() {
        val mssg = "Hello Tipshub"
        val toNumber = "2349041463249"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$toNumber&text=$mssg")
        try {
            val whatsApp = Intent(Intent.ACTION_VIEW)
            whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            whatsApp.data = uri
            startActivity(whatsApp)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("TAG", "startWhatsapp: "+e.message)
            Toast.makeText(this, "No WhatApp installed", Toast.LENGTH_LONG).show()
        }
    }

    private fun startBrowser(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        intent.data = uri
        startActivity(intent)
    }
}