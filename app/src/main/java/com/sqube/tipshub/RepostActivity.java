package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import models.Post;
import utils.Calculations;

public class RepostActivity extends AppCompatActivity implements View.OnClickListener {
    ActionBar actionBar;
    Button btnPost, btnClose;
    FirebaseFirestore database;
    CollectionReference usersReference;
    CollectionReference postReference;

    private String username;
    private String userId;
    private String content;
    private int status;
    private int type;

    private String childLink;
    private String childUsername;
    private String childUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repost);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Post");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
        database = FirebaseFirestore.getInstance();
        postReference = database.collection("posts");
        btnPost = findViewById(R.id.btnPost); btnPost.setOnClickListener(this);
        btnClose = findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
    }

    public void post(){
        postReference.document("[POST URL]").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists()){
                    Toast.makeText(RepostActivity.this, "Original post has been deleted", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        postReference.document("[POST URL]").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Calculations calculations = new Calculations();
                Post post = documentSnapshot.toObject(Post.class);
                post.setRepostCount(post.getRepostCount()+1);
                double postRelevance = calculations.getPostRelevance(post.getLikesCount(), post.getDislikesCount(), post.getRepostCount());
                Map<String, Object> update = new HashMap<>();
                update.put("repostCount", post.getRepostCount()+1);
                update.put("relevance", postRelevance);
                postReference.document("[POST URL]").set(update);
            }
        });

       postReference.add(new Post(username, userId, content, status, type, childLink, childUsername, childUserId));
       //Add user to reposters list
       //send notification
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPost:
                /*
                post();
                sendNotification();
                 */
                Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.btnClose:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void sendNotification() {

    }
}
