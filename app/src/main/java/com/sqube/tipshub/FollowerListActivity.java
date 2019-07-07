package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import adapters.PeopleAdapter;

public class FollowerListActivity extends AppCompatActivity {
    private RecyclerView peopleList;
    private TextView txtNote;
    ArrayList<String> listOfPeople = new ArrayList<>();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_list);
        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        String searchType = intent.getStringExtra("search_type");
        String personId = intent.getStringExtra("personId");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getTitle(searchType));
        }
        txtNote = findViewById(R.id.txtNote);
        peopleList = findViewById(R.id.peopleList);
        peopleList.setLayoutManager(new LinearLayoutManager(FollowerListActivity.this));

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        database.collection(searchType).document(personId).get()
                .addOnCompleteListener(task -> {
                    if(task.getResult()==null || !task.getResult().exists()){
                        txtNote.setVisibility(View.VISIBLE);
                        return;
                    }
                    if(task.getResult().contains("list")){
                        listOfPeople = (ArrayList<String>) task.getResult().get("list");
                        peopleList.setAdapter(new PeopleAdapter(FollowerListActivity.this,
                                getApplicationContext(),userId, listOfPeople));
                    }
                })
                .addOnFailureListener(e -> txtNote.setVisibility(View.VISIBLE));
    }

    private String getTitle(String searchType){
        switch (searchType){
            case "followings":
                return "Following";
            case "subscribers":
                return "Subscribers";
            case "subscribed_to":
                return "Subscribed To";
            default:
                return "Followers";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
