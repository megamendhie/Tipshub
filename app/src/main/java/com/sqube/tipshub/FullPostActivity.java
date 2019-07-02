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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import adapters.CommentAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Comment;
import models.Post;
import services.GlideApp;
import utils.Calculations;
import utils.Reusable;
import utils.SpaceTokenizer;

public class FullPostActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private FirebaseFirestore database;
    CollectionReference commentReference;
    DocumentReference postReference, childReference;
    LinearLayout lnrCode, lnrFullPost, lnrChildPost;
    TextView mpost, mUsername, mTime;
    private RequestOptions requestOptions = new RequestOptions();
    Query query;
    Calculations calculations;
    String comment;
    boolean childDisplayed;
    final String TAG = "FullPostActivity";
    Reusable reusable = new Reusable();
    TextView mLikes, mDislikes, mComment, mCode, mType;
    CircleImageView imgDp, imgMyDp;
    ImageView imgOverflow, imgLike, imgDislike, imgComment, imgShare, imgStatus, imgCode;
    MultiAutoCompleteTextView edtComment;
    FloatingActionButton fabPost;
    ProgressBar prgPost;
    RecyclerView commentsList;
    CommentAdapter commentAdapter;
    ActionBar actionBar;
    Post model;

    String userId, username, postId, childLink;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};
    private StorageReference storageReference;

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
        imgMyDp = findViewById(R.id.imgMyDp);
        imgLike = findViewById(R.id.imgLike); imgLike.setOnClickListener(this);
        imgDislike = findViewById(R.id.imgDislike); imgDislike.setOnClickListener(this);
        imgShare = findViewById(R.id.imgShare); imgShare.setOnClickListener(this);
        imgComment = findViewById(R.id.imgComment);
        imgStatus = findViewById(R.id.imgStatus);
        imgCode = findViewById(R.id.imgCode);
        prgPost = findViewById(R.id.prgPost); prgPost.setVisibility(View.VISIBLE);
        lnrCode = findViewById(R.id.lnrCode);
        lnrFullPost = findViewById(R.id.container_post);
        lnrFullPost.setVisibility(View.GONE);
        lnrChildPost = findViewById(R.id.container_child_post);
        lnrChildPost.setVisibility(View.GONE);
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);

        calculations = new Calculations(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();

        commentsList = findViewById(R.id.listComments);
        commentsList.setLayoutManager(new LinearLayoutManager(this));
        postId = getIntent().getStringExtra("postId");
        postReference = database.collection("posts").document(postId);

        String[] clubs = getResources().getStringArray(R.array.club_arrays);
        ArrayAdapter<String> club_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, clubs);
        edtComment.setAdapter(club_adapter);
        edtComment.setTokenizer(new SpaceTokenizer());
        edtComment.setThreshold(3);
        GlideApp.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(userId))
                .into(imgMyDp);
        
        //loadPost();
        listener();
        loadComment();

    }

    //listen for changes in likesCount, dislikesCount and update
    private void listener() {
        postReference.addSnapshotListener(FullPostActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    lnrFullPost.setVisibility(View.VISIBLE);
                    prgPost.setVisibility(View.GONE);
                    //retrieve post from database
                    model = documentSnapshot.toObject(Post.class);

                    //bind post contents to views
                    imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
                    mUsername.setText(model.getUsername());
                    mpost.setText(model.getContent());
                    mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));

                    //display booking code if available
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

                    //display likes, dislikes, and comments
                    imgLike.setColorFilter(model.getLikes().contains(userId)?
                            getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

                    imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                            getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.likeGrey));

                    mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
                    mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
                    mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));
                    GlideApp.with(getApplicationContext())
                            .setDefaultRequestOptions(requestOptions)
                            .load(storageReference.child(model.getUserId()))
                            .into(imgDp);
                    if(model.isHasChild()){
                        childLink = model.getChildLink();
                        displayChildContent();
                    }
                }
            }
        });
    }

    private void displayChildContent() {
        if(childDisplayed){
            return;
        }
        //initialize child post views
        final LinearLayout lnrChildCode = findViewById(R.id.lnrChildCode);
        final TextView childPost= findViewById(R.id.txtChildPost);
        final TextView childUsername = findViewById(R.id.txtChildUsername);
        final TextView childCode = findViewById(R.id.txtChildCode);
        final TextView childType = findViewById(R.id.txtChildType);
        final ImageView imgChildStatus = findViewById(R.id.imgChildStatus);
        final ImageView imgChildCode = findViewById(R.id.imgCode);
        final CircleImageView imgChildDp = findViewById(R.id.childDp);

        childDisplayed = true;
        database.collection("posts").document(childLink).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists())
                    return;
                Post childModel = task.getResult().toObject(Post.class); //retrieve child post

                //bind post to views
                imgChildStatus.setVisibility(childModel.getStatus()==1? View.GONE: View.VISIBLE);
                if(childModel.getBookingCode()!=null && !childModel.getBookingCode().isEmpty()){
                    childCode.setText(childModel.getBookingCode() + " @" + code[(childModel.getRecommendedBookie()-1)]);
                    childCode.setVisibility(View.VISIBLE);
                    imgChildCode.setVisibility(View.VISIBLE);
                    lnrChildCode.setVisibility(View.VISIBLE);
                }
                else{
                    lnrChildCode.setVisibility(View.GONE);
                    childCode.setVisibility(View.GONE);
                    imgChildCode.setVisibility(View.GONE);
                }
                if(childModel.getType()==0){
                    childType.setVisibility(View.GONE);
                }
                else{
                    childType.setVisibility(View.VISIBLE);
                    childType.setText(type[childModel.getType()-1]);
                }
                childUsername.setText(childModel.getUsername());
                childPost.setText(childModel.getContent());
                GlideApp.with(getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(storageReference.child(childModel.getUserId()))
                        .into(imgChildDp);
                lnrChildPost.setVisibility(View.VISIBLE); //display child layout if child post exists
            }
        });

    }

    private void loadComment() {
        //loads comment into commentList
        commentReference = database.collection("comments").document(postId)
                .collection("comments");
        query = commentReference.orderBy("time", Query.Direction.DESCENDING);
        commentAdapter = new CommentAdapter(postId, query, userId, FullPostActivity.this, getApplicationContext());
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
        String substring;
        switch (v.getId()){
            case R.id.imgLike:
                onLike();
                 substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onLike(postId, userId, model.getUserId(), substring);
                break;
            case R.id.imgDislike:
                onDislike();
                substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onDislike( postId, userId, model.getUserId(), substring);
                break;
            case R.id.imgShare:
                reusable.shareTips(FullPostActivity.this, model.getUsername(), model.getContent());
                break;
            case R.id.fabPost:
                postComment();
                break;
        }
    }

    private void onLike(){
        model.getDislikes().contains(userId);
        if(model.getDislikes().contains(userId)){
            imgLike.setColorFilter(getResources().getColor(R.color.colorPrimary));
            imgDislike.setColorFilter(getResources().getColor(R.color.likeGrey));
            mLikes.setText(String.valueOf(model.getLikesCount()+1));
            mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");
        }
        else{
            if(model.getLikes().contains(userId)){
                imgLike.setColorFilter(getResources().getColor(R.color.likeGrey));
                mLikes.setText(model.getLikesCount()-1>0?String.valueOf(model.getLikesCount()-1):"");
            }
            else{
                imgLike.setColorFilter(getResources().getColor(R.color.colorPrimary));
                mLikes.setText(String.valueOf(model.getLikesCount()+1));
            }
        }
    }

    private void onDislike(){
        if(model.getLikes().contains(userId)){
            imgLike.setColorFilter(getResources().getColor(R.color.likeGrey));
            imgDislike.setColorFilter(getResources().getColor(R.color.colorPrimary));
            mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
            mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
        }
        else{
            if(model.getDislikes().contains(userId)){
                imgDislike.setColorFilter(getResources().getColor(R.color.likeGrey));
                mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");
            }
            else{
                imgDislike.setColorFilter(getResources().getColor(R.color.colorPrimary));
                mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
            }
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
