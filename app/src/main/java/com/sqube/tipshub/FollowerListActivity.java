package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import adapters.PeopleAdapter;

public class FollowerListActivity extends AppCompatActivity {
    private RecyclerView peopleList;
    private TextView txtNote;
    private FirebaseFirestore database;
    ArrayList<String> listOfPeople = new ArrayList<>();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_list);
        txtNote = findViewById(R.id.txtNote);
        peopleList = findViewById(R.id.peopleList);
        peopleList.setLayoutManager(new LinearLayoutManager(FollowerListActivity.this));

        database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        Intent intent = getIntent();
        String type = intent.getStringExtra("search_type");
        String personId = intent.getStringExtra("personId");

        database.collection(type).document(personId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult()==null || !task.getResult().exists()){
                    txtNote.setVisibility(View.VISIBLE);
                    return;
                }
                if(task.getResult().contains("list")){
                    listOfPeople = (ArrayList<String>) task.getResult().get("list");
                    peopleList.setAdapter(new PeopleAdapter(FollowerListActivity.this,
                            getApplicationContext(),userId, listOfPeople));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                txtNote.setVisibility(View.VISIBLE);
            }
        });
    }
}
