package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import adapters.PeopleAdapter;
import utils.Calculations;
import utils.FirebaseUtil;

public class FollowerListActivity extends AppCompatActivity {
    private RecyclerView peopleList;
    private TextView txtNote;
    private ArrayList<String> listOfPeople = new ArrayList<>();
    private String userId;
    private FirebaseUser user;
    private PeopleAdapter peopleAdapter;

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

        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            userId= Calculations.GUEST;
        else
            userId = user.getUid();

        FirebaseUtil.getFirebaseFirestore().collection(searchType).document(personId).get()
                .addOnCompleteListener(task -> {
                    if(task.getResult()==null || !task.getResult().exists()){
                        txtNote.setVisibility(View.VISIBLE);
                        return;
                    }
                    if(task.getResult().contains("list")){
                        listOfPeople = (ArrayList<String>) task.getResult().get("list");
                        peopleAdapter = new PeopleAdapter(getApplicationContext(),userId, listOfPeople);
                        peopleList.setAdapter(peopleAdapter);
                    }
                })
                .addOnFailureListener(e -> txtNote.setVisibility(View.VISIBLE));
    }


    @Override
    public void onResume() {
        super.onResume();
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            userId= Calculations.GUEST;
        else
            userId = user.getUid();
        if(peopleAdapter!=null)
            peopleAdapter.setUserId(userId);
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
