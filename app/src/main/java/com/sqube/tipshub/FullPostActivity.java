package com.sqube.tipshub;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import adapters.CommentAdapter;
import models.SerializedPost;

public class FullPostActivity extends AppCompatActivity {
    LinearLayout lnrCode;
    TextView mpost;
    TextView mUsername;
    TextView mTime;
    TextView mLikes, mDislikes, mComment, mCode, mType;
    ImageView imgOverflow;
    ImageView imgDp,imgLike, imgDislike, imgComment, imgShare, imgStatus, imgCode;
    RecyclerView commentsList;
    ActionBar actionBar;

    String userId;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);
        actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mpost = findViewById(R.id.txtPost);
        mUsername = findViewById(R.id.txtUsername);
        mTime = findViewById(R.id.txtTime);
        mLikes = findViewById(R.id.txtLike);
        mDislikes = findViewById(R.id.txtDislike);
        mComment = findViewById(R.id.txtComment);
        mCode = findViewById(R.id.txtCode);
        mType = findViewById(R.id.txtPostType);

        imgDp = findViewById(R.id.imgDp);
        imgLike = findViewById(R.id.imgLike);
        imgDislike = findViewById(R.id.imgComment);
        imgComment = findViewById(R.id.imgComment);
        imgShare = findViewById(R.id.imgShare);
        imgStatus = findViewById(R.id.imgStatus);
        imgCode = findViewById(R.id.imgCode);
        lnrCode = findViewById(R.id.lnrCode);

        commentsList = findViewById(R.id.listComments);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        commentsList.setLayoutManager(layoutManager);
        SerializedPost model = (SerializedPost) getIntent().getSerializableExtra("model");
        mpost.setText(model.getContent());
        String[] testString = {"A", "B", "C", "D", "E", "F"};
        CommentAdapter adapter = new CommentAdapter(testString);
        commentsList.setAdapter(adapter);

        loadPost();
    }

    private void loadPost() {
        SerializedPost model = (SerializedPost) getIntent().getSerializableExtra("model");
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

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        finish();
    }
}
