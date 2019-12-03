package com.sqube.tipshub;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener {
    PackageManager pkMgt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }

        CardView crdWhatsapp = findViewById(R.id.crdWhatsapp);
        crdWhatsapp.setOnClickListener(this);
        CardView crdTwitter = findViewById(R.id.crdTwitter);
        crdTwitter.setOnClickListener(this);
        CardView crdFacebook = findViewById(R.id.crdFacebook);
        crdFacebook.setOnClickListener(this);
        CardView crdEmail = findViewById(R.id.crdEmail);
        crdEmail.setOnClickListener(this);

        pkMgt = getPackageManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.crdEmail:
                sendEmail();
                break;
            case R.id.crdWhatsapp:
                startWhatsapp();
                break;
            case R.id.crdTwitter:
                startBrowser("https://twitter.com/tipshub_co");
                break;
            case R.id.crdFacebook:
                startBrowser("https://web.facebook.com/Tipshub-110212083647469");
                break;
        }
    }

    private void sendEmail(){
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("mailto:swiftqube@gmail.com"));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Tipshub Help Request");
        startActivity(Intent.createChooser(sendIntent, "Select:"));
    }


    private void startWhatsapp() {
        String mssg = "Hello Tipshub";
        String toNumber = "2348132014755";
        Uri uri = Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+mssg);
        try {
            Intent whatsApp = new Intent(Intent.ACTION_VIEW);
            whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            whatsApp.setData(uri);
            pkMgt.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            startActivity(whatsApp);
        }
        catch (PackageManager.NameNotFoundException e){
            Toast.makeText(this, "No WhatApp installed", Toast.LENGTH_LONG).show();
        }
    }

    private void startBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setData(uri);
        startActivity(intent);
    }
}
