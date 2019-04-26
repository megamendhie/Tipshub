package com.sqube.tipshub;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import javax.annotation.Nullable;

import adapters.CommentAdapter;
import models.Post;
import utils.Calculations;
import utils.Reusable;

public class FullPostActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore database;
    LinearLayout lnrCode;
    TextView mpost;
    TextView mUsername;
    TextView mTime;
    Query query;
    Calculations calculations;
    Reusable reusable = new Reusable();
    TextView mLikes, mDislikes, mComment, mCode, mType;
    ImageView imgOverflow;
    ImageView imgDp,imgLike, imgDislike, imgComment, imgShare, imgStatus, imgCode;
    RecyclerView commentsList;
    CommentAdapter commentAdapter;
    ActionBar actionBar;
    Post model;

    String userId, postId;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);
        actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Post");
        mpost = findViewById(R.id.txtPost);
        mUsername = findViewById(R.id.txtUsername);
        mTime = findViewById(R.id.txtTime);
        mLikes = findViewById(R.id.txtLike);
        mDislikes = findViewById(R.id.txtDislike);
        mComment = findViewById(R.id.txtComment);
        mCode = findViewById(R.id.txtCode);
        mType = findViewById(R.id.txtPostType);

        imgDp = findViewById(R.id.imgDp);
        imgLike = findViewById(R.id.imgLike); imgLike.setOnClickListener(this);
        imgDislike = findViewById(R.id.imgDislike); imgDislike.setOnClickListener(this);
        imgShare = findViewById(R.id.imgShare); imgShare.setOnClickListener(this);

        imgComment = findViewById(R.id.imgComment);
        imgStatus = findViewById(R.id.imgStatus);
        imgCode = findViewById(R.id.imgCode);
        lnrCode = findViewById(R.id.lnrCode);

        calculations = new Calculations(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        commentsList = findViewById(R.id.listComments);
        commentsList.setLayoutManager(new LinearLayoutManager(this));
        postId = getIntent().getStringExtra("postId");
        loadPost();
        loadComment();
        listener();

    }

    private void listener() {
        database.collection("posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    model = documentSnapshot.toObject(Post.class);
                    imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
                    if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
                        mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
                        mCode.setVisibility(View.VISIBLE);
                        imgCode.setVisibility(View.VISIBLE);
                        lnrCode.setVisibility(View.VISIBLE);
                    }
                    else{
                        lnrCode.setVisibility(View.GONE);
                        mCode.setVisibility(View.GONE);
                        imgCode.setVisibility(View.GONE);
                    }
                    imgLike.setColorFilter(model.getLikes().contains(userId)?
                            getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

                    imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                            getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

                    mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
                    mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
                    mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));
                }
            }
        });
    }

    private void loadPost() {
        model = (Post) getIntent().getSerializableExtra("model");
        imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
            mCode.setVisibility(View.VISIBLE);
            imgCode.setVisibility(View.VISIBLE);
            lnrCode.setVisibility(View.VISIBLE);
        }
        else{
            lnrCode.setVisibility(View.GONE);
            mCode.setVisibility(View.GONE);
            imgCode.setVisibility(View.GONE);
        }
        if(model.getType()==0){
            mType.setVisibility(View.GONE);
        }
        else{
            mType.setVisibility(View.VISIBLE);
            mType.setText(type[model.getType()-1]);
        }
        mUsername.setText(model.getUsername());
        mpost.setText(model.getContent());
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        imgLike.setColorFilter(model.getLikes().contains(userId)?
                getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

        imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

        mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

    }

    private void loadComment() {
        query = database.collection("comments").document(postId)
                .collection("comments").orderBy("time", Query.Direction.DESCENDING);
        commentAdapter = new CommentAdapter(query, userId, FullPostActivity.this, getApplicationContext());
        commentsList.setAdapter(commentAdapter);
        if(commentAdapter!=null){
            commentAdapter.startListening();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgLike:
                calculations.onLike(postId, userId);
                break;
            case R.id.imgDislike:
                calculations.onDislike(postId, userId);
                break;
            case R.id.imgShare:
                reusable.shareTips(FullPostActivity.this, model.getUsername(), model.getContent());
                break;
        }
    }
}
