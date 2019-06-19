package com.sqube.tipshub;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class AccountActivity extends AppCompatActivity {
    RecyclerView listSubscribers, listSubscriptions;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        listSubscribers = findViewById(R.id.listSubscribers);
        listSubscribers.setLayoutManager(new LinearLayoutManager(this));
        listSubscriptions = findViewById(R.id.listSubscriptions);
        listSubscriptions.setLayoutManager(new LinearLayoutManager(this));
    }
}
