package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import models.Comment;
import models.Report;
import utils.Calculations;

public class FlagActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnPost;
    private TextView btnClose;
    private MultiAutoCompleteTextView edtComment;
    private String comment;
    private String postId;
    private String TAG = "FlagActivity";
    private ProgressBar progressBar;
    FirebaseFirestore database;
    DocumentReference postReference;
    Calculations calculations;

    String userId, username, postContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
        edtComment = findViewById(R.id.edtPost);
        btnPost = findViewById(R.id.btnPost); btnPost.setOnClickListener(this);
        btnClose = findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        progressBar = findViewById(R.id.prgLogin);

        postId = getIntent().getStringExtra("postId");
        database = FirebaseFirestore.getInstance();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
        postId = getIntent().getStringExtra("postId");
        postReference = database.collection("posts").document(postId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPost:
                increaseCommentCount();
                break;
            case R.id.btnClose:
                popUp();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void reportPost() {
        comment = edtComment.getText().toString().trim();
        if(TextUtils.isEmpty(comment) || comment.length() < 3){
            edtComment.setError("Type your reason");
            return;
        }
        CollectionReference commentReference = database.collection("comments").document(postId)
                        .collection("comments");
        CollectionReference reportReference = database.collection("report");

        commentReference.add(new Comment(username, userId, comment, true))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        reportReference.add(new Report(username, userId, comment, postId));
                        progressBar.setVisibility(View.GONE);
                        comment= "";
                        edtComment.setText("");
                        Snackbar.make(edtComment, "Comment added", Snackbar.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    public void increaseCommentCount(){
        comment = edtComment.getText().toString();
        if(TextUtils.isEmpty(comment)){
            edtComment.setError("Type your comment");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
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
                long commentsCount = snapshot.getLong("commentsCount") + 1;
                long reportCount = snapshot.getLong("reportCount") + 1;
                Map<String, Object> upd = new HashMap<>();

                upd.put("commentsCount", commentsCount);
                upd.put("reportCount", reportCount);
                transaction.update(postReference, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        reportPost();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(FlagActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void popUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FlagActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("Save this comment?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        //do nothing
                    }
                })
                .show();
    }
}
