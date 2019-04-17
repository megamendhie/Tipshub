package com.sqube.tipshub;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.Post;
import models.Profile;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {
    private ActionBar actionBar;
    private Button btnPost;
    private TextView btnClose, btnAdd, txtNormal;
    private EditText edtPost;
    private ProgressBar prgBar;
    private Spinner spnType;
    FirebaseFirestore database;
    FirebaseAuth auth;
    FirebaseUser user;
    CollectionReference usersReference;
    CollectionReference postReference;

    private String username;
    private String userId;
    private String content;
    private int type =1;
    private String code;
    private int codeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }
        setContentView(R.layout.activity_post);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Post");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        postReference = database.collection("posts");
        edtPost = findViewById(R.id.edtPost);
        txtNormal = findViewById(R.id.txtNormal);
        spnType = findViewById(R.id.spnPostType);
        btnPost = findViewById(R.id.btnPost); btnPost.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAddCode); btnAdd.setOnClickListener(this);
        btnClose = findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        prgBar = findViewById(R.id.prgLogin);
        username = user.getDisplayName();
        userId = user.getUid();
        if(getIntent().getStringExtra("type").equals("tip")){
            spnType.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
            txtNormal.setVisibility(View.GONE);
            type = 1;
        }
        else{
            spnType.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);
            txtNormal.setVisibility(View.VISIBLE);
            type = 0;
        }
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtPost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() > 2) {
                    content = edtPost.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                content = edtPost.getText().toString();
                if(content.length()<4){
                    Toast.makeText(PostActivity.this, "Content too small", Toast.LENGTH_LONG).show();
                    return;
                }
                post();
                break;
            case R.id.btnClose:
                finish();
                break;
            case R.id.btnAddCode:
                addCode();
                break;
        }
    }

    private void addCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.tips_code_view, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();
        final EditText edtCode = dialog.findViewById(R.id.edtCode);
        final Spinner spnCode = dialog.findViewById(R.id.spnCode);
        final Button btnAddCode = dialog.findViewById(R.id.btnDialogAdd);
        final Button btnCloseCode = dialog.findViewById(R.id.btnDialogCancel);
        spnCode.setSelection(codeIndex>0? codeIndex-1:0);
        edtCode.setText(code);
        btnCloseCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeIndex=0;
                code="";
                dialog.cancel();
                btnAdd.setText("ADD CODE");

            }
        });
        btnAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = edtCode.getText().toString();
                codeIndex = spnCode.getSelectedItemPosition() + 1;
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(PostActivity.this, "No Code", Toast.LENGTH_LONG).show();
                    return;
                }
                if(code.length()<4){
                    Toast.makeText(PostActivity.this, "Code incomplete", Toast.LENGTH_LONG).show();
                    return;
                }
                dialog.cancel();
                btnAdd.setText("CODE: "+code);

            }
        });
    }

    public void post(){
        prgBar.setVisibility(View.VISIBLE);
        postReference.add(new Post(username, userId, content, 1, type, code, codeIndex));
        database.collection("profiles").document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    final Profile myProfile = documentSnapshot.toObject(Profile.class);
                    final long lastPostTime = new Date().getTime();
                    final long postCount = myProfile.getE0a_NOG();
                    final long wonGamesCount = myProfile.getE0b_WG();
                    long wonGamesPercentage = (wonGamesCount*100)/(1+postCount);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("c8_lsPostTime", lastPostTime);
                    updates.put("e0a_NOG", (postCount+1));
                    updates.put("e0c_WGP", wonGamesPercentage);
                    database.collection("profiles").document(userId).set(updates, SetOptions.merge());
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(PostActivity.this,"Post sending failed", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}