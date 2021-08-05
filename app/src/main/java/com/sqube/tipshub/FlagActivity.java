package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import models.Comment;
import models.Report;
import utils.FirebaseUtil;

public class FlagActivity extends AppCompatActivity implements View.OnClickListener {
    private MultiAutoCompleteTextView edtComment;
    private String comment;
    private String postId, reportedUsername, reportedUserId;
    private String TAG = "FlagActivity";
    private ProgressBar progressBar;
    private SharedPreferences prefs;
    DocumentReference postReference;

    String userId;
    String username;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edtComment = findViewById(R.id.edtPost);
        Button btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(this);
        TextView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        progressBar = findViewById(R.id.prgLogin);
        postId = getIntent().getStringExtra("postId");

        final FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();

        //get from intent reported post, username, and userId
        postId = getIntent().getStringExtra("postId");
        reportedUsername = getIntent().getStringExtra("reportedUsername");
        reportedUserId = getIntent().getStringExtra("reportedUserId");
        postReference = FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        if (TextUtils.isEmpty(comment) || comment.length() < 3) {
            edtComment.setError("Type your reason");
            return;
        }
        CollectionReference commentReference = FirebaseUtil.getFirebaseFirestore().collection("comments").document(postId)
                .collection("comments");
        CollectionReference reportReference = FirebaseUtil.getFirebaseFirestore().collection("report");

        //get user verification status from SharePreference
        boolean isVerified = prefs.getBoolean("isVerified", false);
        commentReference.add(new Comment(username, userId, comment, postId, true, isVerified))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        reportReference.add(new Report(username, userId, comment, postId, reportedUsername, reportedUserId));
                        progressBar.setVisibility(View.GONE);
                        comment = "";
                        edtComment.setText("");
                        Snackbar.make(edtComment, "Comment added", Snackbar.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    public void increaseCommentCount() {
        comment = edtComment.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            edtComment.setError("Type your comment");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUtil.getFirebaseFirestore().runTransaction((Transaction.Function<Void>) transaction -> {
            Log.i(TAG, "apply: likes entered");
            DocumentSnapshot snapshot = transaction.get(postReference);

            //check if post still exists
            if (!snapshot.exists()) {
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
        })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Transaction success!");
                    reportPost();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Transaction failure.", e);
                    Toast.makeText(FlagActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void popUp() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(FlagActivity.this, R.style.CustomMaterialAlertDialog);
        builder.setMessage("Save this comment?")
                .setPositiveButton("Save", (dialogInterface, i) -> finish())
                .setNegativeButton("Delete", (dialogInterface, i) -> {
                    finish();
                    //do nothing
                })
                .show();
    }
}
