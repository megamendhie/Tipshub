package com.sqube.tipshub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import adapters.CommentAdapter;
import models.Comment;
import models.Post;
import utils.Calculations;
import utils.Reusable;

public class FullPostActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private FirebaseFirestore database;
    CollectionReference commentReference;
    DocumentReference postReference;
    LinearLayout lnrCode;
    TextView mpost;
    TextView mUsername;
    TextView mTime;
    Query query;
    Calculations calculations;
    String comment;
    final String TAG = "FullPostActivity";
    Reusable reusable = new Reusable();
    TextView mLikes, mDislikes, mComment, mCode, mType;
    ImageView imgOverflow;
    ImageView imgDp,imgLike, imgDislike, imgComment, imgShare, imgStatus, imgCode;
    EditText edtComment;
    FloatingActionButton fabPost;
    RecyclerView commentsList;
    CommentAdapter commentAdapter;
    ActionBar actionBar;
    Post model;

    String userId, username, postId;
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
        fabPost = findViewById(R.id.fabPost); fabPost.setOnClickListener(this);
        edtComment = findViewById(R.id.edtComment); edtComment.addTextChangedListener(this);

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
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid().toString();
        username = user.getDisplayName();

        commentsList = findViewById(R.id.listComments);
        commentsList.setLayoutManager(new LinearLayoutManager(this));
        postId = getIntent().getStringExtra("postId");
        postReference = database.collection("posts").document(postId);
        loadPost();
        loadComment();
        listener();

    }

    //listen for changes in likesCount, dislikesCount and update
    private void listener() {
        postReference.addSnapshotListener(FullPostActivity.this, new EventListener<DocumentSnapshot>() {
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
        if(model==null){
            return;
        }
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
        //loads comment into commentList
        commentReference = database.collection("comments").document(postId)
                .collection("comments");
        query = commentReference.orderBy("time", Query.Direction.DESCENDING);
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
                calculations.onLike(postId, userId, model.getUserId());
                break;
            case R.id.imgDislike:
                calculations.onDislike(postId, userId);
                break;
            case R.id.imgShare:
                reusable.shareTips(FullPostActivity.this, model.getUsername(), model.getContent());
                break;
            case R.id.fabPost:
                postComment();
                break;
        }
    }

    private void postComment() {
        comment = edtComment.getText().toString();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(FullPostActivity.this, "Comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            commentReference.add(new Comment(username, userId, comment))
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Snackbar.make(edtComment, "Comment added", Snackbar.LENGTH_SHORT).show();
                    comment= "";
                    edtComment.setText("");
                    increaseCommentCount();
                    if(!userId.equals(model.getUserId())){
                        calculations.recommend(userId, model.getUserId());
                    }
                }
            });
        }
    }

    public void increaseCommentCount(){
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.i(TAG, "apply: likes entered");
                DocumentSnapshot snapshot = transaction.get(postReference);
                //check if post still exists
                if(!snapshot.exists()){
                    Log.i(TAG, "apply: like doesn't exist");
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long commentsCount = snapshot.getLong("commentsCount");
                Map<String, Object> upd = new HashMap<>();
                commentsCount +=1;
                upd.put("commentsCount", commentsCount);
                transaction.update(postReference, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(FullPostActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });

        //send notification
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() > 1) {
            comment = edtComment.getText().toString();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
