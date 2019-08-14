package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class GuideActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txtGuidelines, txtPrivacyPolicy, txtCAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("About/Privacy Policy");
        }

        txtGuidelines = findViewById(R.id.txtGuidelines);
        txtGuidelines.setOnClickListener(this);
        txtPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy);
        txtPrivacyPolicy.setOnClickListener(this);
        txtCAC = findViewById(R.id.txtCAC);
        txtCAC.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.txtGuidelines:
                intent = new Intent(GuideActivity.this, AboutActivity.class);
                break;
            case R.id.txtPrivacyPolicy:
                intent = new Intent(GuideActivity.this, NewsStoryActivity.class);
                intent.putExtra("url", "https://tipshub.co/privacy-policy/");
                break;
            case R.id.txtCAC:
                intent = new Intent(GuideActivity.this, NewsStoryActivity.class);
                intent.putExtra("url", "https://tipshub.co/terms-and-conditions/");
                break;
        }
        if(intent!=null)
            startActivity(intent);
    }
}
