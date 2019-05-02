package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import utils.Calculations;

public class RepostActivity extends AppCompatActivity implements View.OnClickListener {
    ActionBar actionBar;
    Button btnPost, btnClose;
    private EditText edtPost;
    private ProgressBar prgBar;
    private TextView txtPost, txtChildUsername, txtChildType;
    private ImageView imgStatus;
    private CircleImageView imgDp, childDp;
    Post model;

    FirebaseFirestore database;
    FirebaseUser user;
    DocumentReference postReference;
    private String username;
    private String userId;
    private String content;
    private String childLink;

    boolean postExist=false;
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};

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
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();

        btnPost = findViewById(R.id.btnPost); btnPost.setOnClickListener(this);
        btnClose = findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        prgBar = findViewById(R.id.prgLogin);

        edtPost = findViewById(R.id.edtPost);
        txtPost = findViewById(R.id.txtPost);
        imgStatus = findViewById(R.id.imgStatus);
        txtChildType = findViewById(R.id.txtChildType);
        txtChildUsername = findViewById(R.id.txtChildUsername);
        childLink = getIntent().getStringExtra("postId");
        postReference = database.collection("posts").document(childLink);

        loadPost();
    }


    private void loadPost() {
        model = (Post) getIntent().getSerializableExtra("model");
        if(model==null){
            return;
        }
        postExist=true;

        //set visibility for status and type
        imgStatus.setVisibility(model.getStatus() ==1? View.GONE: View.VISIBLE);
        if(model.getType()==0){
            txtChildType.setVisibility(View.GONE);
        }
        else{
            txtChildType.setVisibility(View.VISIBLE);
            txtChildType.setText(type[model.getType()-1]);
        }

        txtChildUsername.setText(model.getUsername());
        txtPost.setText(model.getContent());
    }

    //listen to changes on child node. Update
    private void listener() {
        postReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                model = documentSnapshot.toObject(Post.class);
                if(model==null){
                    return;
                }
                imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
                if(model.getType()==0){
                    txtChildType.setVisibility(View.GONE);
                }
                else{
                    txtChildType.setVisibility(View.VISIBLE);
                    txtChildType.setText(type[model.getType()-1]);
                }
                txtChildUsername.setText(model.getUsername());
                txtPost.setText(model.getContent());
            }
        });
    }

    public void post(){
        content = edtPost.getText().toString();
        if(content.length()<3){
            Toast.makeText(this, "Content too small", Toast.LENGTH_LONG).show();
            return;
        }
        prgBar.setVisibility(View.VISIBLE);
        final DocumentReference postPath =  database.collection("posts").document(childLink);
        final Calculations calculations= new Calculations(getApplicationContext());

        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.i("RepostActivity", "apply: likes entered");
                DocumentSnapshot snapshot = transaction.get(postPath);
                //check if post still exists
                if(!snapshot.exists()){
                    Log.i("RepostActivity", "apply: like doesn't exist");
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long likesCount = snapshot.getLong("likesCount");
                long dislikesCount = snapshot.getLong("dislikesCount");
                long repostCount = snapshot.getLong("repostCount") + 1;
                long time = snapshot.getLong("time");
                Map<String, Object> upd = new HashMap<>();

                //recalculate child post relevance
                double postRelevance = calculations.getPostRelevance(likesCount, dislikesCount, repostCount);
                double timeRelevance = calculations.getTimeRelevance(postRelevance, time);
                upd.put("repostCount", repostCount);
                upd.put("relevance", postRelevance);
                upd.put("timeRelevance", timeRelevance);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("RepostActivity", "Transaction success!");
                        //Repost the content
                        Post post = new Post(username, userId, content, 1, 0, childLink, model.getUsername(),
                                model.getUserId(), model.getContent(), model.getType(), model.getImgUrl1(), model.getImgUrl2(),
                                model.getBookingCode(), model.getRecommendedBookie());
                        database.collection("posts").add(post);

                        //add to recommended user
                        if(!userId.equals(model.getUserId())){
                            calculations.recommend(userId, model.getUserId());
                        }
                        setLastPostTime();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("RepostActivity", "Transaction failure.", e);
                        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });

       //send notification
    }

    public void setLastPostTime(){
        database.collection("profiles").document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    //update time for last post
                    long lastPostTime = new Date().getTime();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("c8_lsPostTime", lastPostTime);
                    database.collection("profiles").document(userId).set(updates, SetOptions.merge());
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(RepostActivity.this,"Post sending failed", Toast.LENGTH_LONG).show();
                }

            }
        });
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
                post();
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
