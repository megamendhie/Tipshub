package com.sqube.tipshub

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ContactActivity : AppCompatActivity(), View.OnClickListener {
    var pkMgt: PackageManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = ""
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
        val crdWhatsapp = findViewById<CardView>(R.id.crdWhatsapp)
        crdWhatsapp.setOnClickListener(this)
        val crdTwitter = findViewById<CardView>(R.id.crdTwitter)
        crdTwitter.setOnClickListener(this)
        val crdFacebook = findViewById<CardView>(R.id.crdFacebook)
        crdFacebook.setOnClickListener(this)
        val crdEmail = findViewById<CardView>(R.id.crdEmail)
        crdEmail.setOnClickListener(this)
        pkMgt = packageManager
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.crdEmail -> sendEmail()
            R.id.crdWhatsapp -> startWhatsapp()
            R.id.crdTwitter -> startBrowser("https://twitter.com/tipshub_co")
            R.id.crdFacebook -> startBrowser("https://web.facebook.com/Tipshub-110212083647469")
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
        val uri = Uri.parse("http://api.whatsapp.com/send?phone=$toNumber&text=$mssg")
        try {
            val whatsApp = Intent(Intent.ACTION_VIEW)
            whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            whatsApp.data = uri
            pkMgt!!.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA)
            startActivity(whatsApp)
        } catch (e: PackageManager.NameNotFoundException) {
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