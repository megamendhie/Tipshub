package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import adapters.CommentAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Comment;
import models.Post;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;
import utils.SpaceTokenizer;

public class FullPostActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private CollectionReference commentReference;
    private DocumentReference postReference;
    private LinearLayout lnrCode, lnrFullPost, lnrChildPost;
    private TextView mpost, mUsername, mTime;
    private TextView mLikes, mDislikes, mComment, mCode, mType;
    private CircleImageView imgDp, imgChildDp;
    private ImageView imgOverflow;
    private ImageView imgLike;
    private ImageView imgDislike;
    private ImageView imgStatus;
    private ImageView imgCode;
    private MultiAutoCompleteTextView edtComment;
    private FloatingActionButton fabPost;
    private ProgressBar prgPost;
    private RecyclerView commentsList;
    private Post model;

    private RequestOptions requestOptions = new RequestOptions();
    private Intent intent = null;

    private String POST_ID = "postId";
    private String comment;
    private final String TAG = "FullPostActivity";
    private String userId, username, postId, childLink;
    private boolean childDisplayed;
    private SharedPreferences prefs;
    Calculations calculations;

    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Post");
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mpost = findViewById(R.id.txtPost);
        mUsername = findViewById(R.id.txtUsername); mUsername.setOnClickListener(this);
        mTime = findViewById(R.id.txtTime);
        mLikes = findViewById(R.id.txtLike);
        mDislikes = findViewById(R.id.txtDislike);
        mComment = findViewById(R.id.txtComment);
        mCode = findViewById(R.id.txtCode);
        mType = findViewById(R.id.txtPostType);
        fabPost = findViewById(R.id.fabPost); fabPost.setOnClickListener(this);
        edtComment = findViewById(R.id.edtComment); edtComment.addTextChangedListener(this);

        imgOverflow = findViewById(R.id.imgOverflow); imgOverflow.setOnClickListener(this);
        imgDp = findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        CircleImageView imgMyDp = findViewById(R.id.imgMyDp);
        imgLike = findViewById(R.id.imgLike); imgLike.setOnClickListener(this);
        imgDislike = findViewById(R.id.imgDislike); imgDislike.setOnClickListener(this);
        ImageView imgRepost = findViewById(R.id.imgRepost);
        imgRepost.setOnClickListener(this);
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
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        final FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();

        commentsList = findViewById(R.id.listComments);
        if(savedInstanceState!=null)
            postId = savedInstanceState.getString(POST_ID);
        else
            postId = getIntent().getStringExtra(POST_ID);
        postReference = FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId);

        String[] clubs = getResources().getStringArray(R.array.club_arrays);
        ArrayAdapter<String> club_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, clubs);
        edtComment.setAdapter(club_adapter);
        edtComment.setTokenizer(new SpaceTokenizer());
        edtComment.setThreshold(4);
        GlideApp.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(userId))
                .signature(new ObjectKey(userId+"_"+Reusable.getSignature()))
                .into(imgMyDp);

        listener();
        loadComment();

    }

    //listen for changes in likesCount, dislikesCount and update
    private void listener() {
        postReference.addSnapshotListener(FullPostActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot==null||!documentSnapshot.exists()){
                    Toast.makeText(FullPostActivity.this, "Content doesn't exist", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    lnrFullPost.setVisibility(View.VISIBLE);
                    prgPost.setVisibility(View.GONE);
                    //retrieve post from database
                    model = documentSnapshot.toObject(Post.class);

                    //bind post contents to views
                    imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
                    mUsername.setText(model.getUsername());
                    mpost.setText(model.getContent());
                    Reusable.applyLinkfy(getApplicationContext(), model.getContent(), mpost);
                    mTime.setText(Reusable.getTime(model.getTime()));

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
                            getResources().getColor(R.color.likeGold): getResources().getColor(R.color.likeGrey));

                    imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                            getResources().getColor(R.color.likeGold): getResources().getColor(R.color.likeGrey));

                    mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
                    mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
                    mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));
                    GlideApp.with(getApplicationContext())
                            .setDefaultRequestOptions(requestOptions)
                            .load(storageReference.child(model.getUserId()))
                            .signature(new ObjectKey(model.getUserId()+"_"+Reusable.getSignature()))
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
        imgChildDp = findViewById(R.id.childDp); imgChildDp.setOnClickListener(this);

        childDisplayed = true;
        FirebaseUtil.getFirebaseFirestore().collection("posts").document(childLink).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists()){
                    childPost.setText("This content has been deleted");
                    imgChildDp.setVisibility(View.GONE);
                    childUsername.setVisibility(View.GONE);
                    childType.setVisibility(View.GONE);
                    imgChildStatus.setVisibility(View.GONE);
                    imgChildCode.setVisibility(View.GONE);
                    childCode.setVisibility(View.GONE);
                    lnrChildPost.setBackgroundResource(R.color.placeholder_bg);
                    lnrChildPost.setVisibility(View.VISIBLE); //display child layout if child post exists
                    return;
                }
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
                Reusable.applyLinkfy(getApplicationContext(), childModel.getContent(), childPost);
                GlideApp.with(getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(storageReference.child(childModel.getUserId()))
                        .signature(new ObjectKey(childModel.getUserId()+"_"+Reusable.getSignature()))
                        .into(imgChildDp);
                lnrChildPost.setVisibility(View.VISIBLE); //display child layout if child post exists
                lnrChildPost.setOnClickListener(FullPostActivity.this);
            }
        });
    }

    //Displays overflow containing options like follow, subscribe, disagree, etc.
    private void displayOverflow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView;
        if(model.getUserId().equals(userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button btnSubmit, btnDelete, btnShare, btnFollow;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnShare = dialog.findViewById(R.id.btnShare);
        btnFollow = dialog.findViewById(R.id.btnFollow);

        long timeDifference = new Date().getTime() - model.getTime();
        if(model.getUserId().equals(userId)&& model.getType()>0 && timeDifference > 9000000)
            btnDelete.setEnabled(false);
        if(model.getUserId().equals(userId) && model.getType()==0)
            btnSubmit.setVisibility(View.GONE);
        else if(model.getUserId().equals(userId)&& timeDifference > 144000000)
            btnSubmit.setVisibility(View.GONE);
        else {
            if (model.getUserId().equals(userId) && model.getStatus() == 2 && timeDifference <= 9000000)
                btnSubmit.setText("CANCEL WON");
            if (model.getUserId().equals(userId) && model.getStatus() == 2 && timeDifference > 9000000)
                btnSubmit.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnDelete.getText().toString().toLowerCase().equals("flag")){
                    intent = new Intent(FullPostActivity.this, FlagActivity.class);
                    intent.putExtra("postId", postId);
                    intent.putExtra("reportedUsername", model.getUsername());
                    intent.putExtra("reportedUserId", model.getUserId());
                    startActivity(intent);
                    dialog.cancel();
                }
                else{
                    Log.i(TAG, "onClick: "+ model.getType());
                    if(model.getType()>0)
                        calculations.onDeletePost(imgOverflow, postId, userId,model.getStatus()==2, model.getType());
                    else {
                        FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId).delete();
                        Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show();
                    }
                    dialog.cancel();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getType()>0)
                    popUp();
                dialog.cancel();
            }
            private void popUp(){
                String message = "<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Your tips have delivered?</strong></span></p>\n" +
                        "<p>By clicking 'YES', you confirm that your prediction has delivered.</p>\n" +
                        "<p>Your account may be suspended or terminated if that's not true.</p>";
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setMessage(Html.fromHtml(message))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                calculations.onPostWon(imgOverflow, postId, userId, model.getType());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //do nothing
                            }
                        })
                        .show();
            }
        });

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(model.getUserId())? "UNFOLLOW": "FOLLOW");

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reusable.shareTips(FullPostActivity.this, model.getUsername(), model.getContent());
                dialog.cancel();
            }
        });

        btnFollow.setOnClickListener(v -> {
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, model.getUserId());
            }
            else{
                calculations.unfollowMember(imgOverflow, userId, model.getUserId());
            }
            dialog.cancel();
        });
    }

    private void loadComment() {
        //loads comment into commentList
        commentReference = FirebaseUtil.getFirebaseFirestore().collection("comments");
        Query query = commentReference.whereEqualTo("commentOn", postId).orderBy("time", Query.Direction.DESCENDING);
        CommentAdapter commentAdapter = new CommentAdapter(postId, query, userId, FullPostActivity.this, getApplicationContext());
        commentsList.setAdapter(commentAdapter);
        if(commentAdapter !=null){
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
            case R.id.txtUsername:
            case R.id.imgDp:
                if(model.getUserId().equals(userId)){
                    startActivity(new Intent(this, MyProfileActivity.class));
                }
                else{
                    intent = new Intent(this, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    startActivity(intent);
                }
                break;
            case R.id.childDp:
                if(model.getChildUserId().equals(userId)){
                    startActivity(new Intent(this, MyProfileActivity.class));
                }
                else{
                    intent = new Intent(this, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getChildUserId());
                    startActivity(intent);
                }
                break;
            case R.id.imgOverflow:
                displayOverflow();
                break;
            case R.id.container_child_post:
                intent = new Intent(getApplicationContext(), FullPostActivity.class);
                intent.putExtra("postId", childLink);
                startActivity(intent);
                break;
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
            case R.id.imgRepost:
                intent = new Intent(FullPostActivity.this, RepostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                startActivity(intent);
                break;
            case R.id.fabPost:
                increaseCommentCount();
                break;
        }
    }

    private void sendNotification(String content) {
        String substring = content.substring(0, Math.min(content.length(), 90));
        calculations.setCount(model.getRepostCount());
        calculations.sendPushNotification(true, userId, model.getUserId(), postId, "commented on", "post", substring);
    }

    private void onLike(){
        if(model.getDislikes().contains(userId)){
            imgLike.setColorFilter(getResources().getColor(R.color.likeGold));
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
                imgLike.setColorFilter(getResources().getColor(R.color.likeGold));
                mLikes.setText(String.valueOf(model.getLikesCount()+1));
            }
        }
    }

    private void onDislike(){
        if(model.getLikes().contains(userId)){
            imgLike.setColorFilter(getResources().getColor(R.color.likeGrey));
            imgDislike.setColorFilter(getResources().getColor(R.color.likeGold));
            mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
            mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
        }
        else{
            if(model.getDislikes().contains(userId)){
                imgDislike.setColorFilter(getResources().getColor(R.color.likeGrey));
                mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");
            }
            else{
                imgDislike.setColorFilter(getResources().getColor(R.color.likeGold));
                mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
            }
        }
    }

    private void postComment() {
        boolean isVerified = prefs.getBoolean("isVerified", false);
        commentReference.add(new Comment(username, userId, comment, postId, false, isVerified))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        final String content = comment;
                        comment= "";
                        if(!userId.equals(model.getUserId())){
                            calculations.recommend(userId, model.getUserId());
                            sendNotification(content);
                        }
                    }
                });
    }

    public void increaseCommentCount(){
        comment = edtComment.getText().toString();
        if(TextUtils.isEmpty(comment)){
            edtComment.setError("Type your comment");
            return;
        }
        else{
            fabPost.setEnabled(false);
            edtComment.setEnabled(false);
        }

        FirebaseUtil.getFirebaseFirestore().runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.i(TAG, "apply: likes entered");
                DocumentSnapshot snapshot = transaction.get(postReference);

                //check if post still exists
                if(!snapshot.exists()){
                    Toast.makeText(FullPostActivity.this, "Seems the post has been deleted", Toast.LENGTH_SHORT).show();
                    fabPost.setEnabled(true);
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
                        postComment();
                        fabPost.setEnabled(true);
                        edtComment.setEnabled(true);
                        edtComment.setText("");
                        Snackbar.make(edtComment, "Comment added", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fabPost.setEnabled(true);
                        Toast.makeText(FullPostActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(POST_ID, postId);
        super.onSaveInstanceState(outState);
    }
}
