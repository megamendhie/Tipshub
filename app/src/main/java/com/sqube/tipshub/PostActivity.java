package com.sqube.tipshub;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.ProfileMedium;
import services.GlideApp;
import utils.FirebaseUtil;
import utils.Reusable;
import utils.SpaceTokenizer;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView btnAdd;
    private MultiAutoCompleteTextView edtPost;
    private ProgressBar prgBar;
    private SharedPreferences prefs;
    FirebaseUser user;
    CollectionReference postReference;
    private RequestOptions requestOptions = new RequestOptions();

    private String username;
    private String userId;
    private String content;
    private int type =1;
    private String code;
    private int codeIndex = 0;
    private String CODE = "code", CODE_INDEX = "code_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }
        setContentView(R.layout.activity_post);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Post");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }

        if(savedInstanceState!=null){
            code = savedInstanceState.getString(CODE);
            codeIndex = savedInstanceState.getInt(CODE_INDEX,0);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        postReference = FirebaseUtil.getFirebaseFirestore().collection("posts");
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        edtPost = findViewById(R.id.edtPost);
        TextView txtNormal = findViewById(R.id.txtNormal);
        Spinner spnType = findViewById(R.id.spnPostType);
        Button btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAddCode); btnAdd.setOnClickListener(this);
        TextView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        CircleImageView imgDp = findViewById(R.id.imgDp);
        prgBar = findViewById(R.id.prgLogin);
        username = user.getDisplayName();
        userId = user.getUid();

        if(getIntent().getStringExtra("type").equals("tip")){
            spnType.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
            txtNormal.setVisibility(View.GONE);
            type = 1;
        }
        else if(getIntent().getStringExtra("type").equals("banker")){
            spnType.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);
            txtNormal.setText("Banker");
            txtNormal.setVisibility(View.VISIBLE);
            type = 6;
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

        //set Display picture
        GlideApp.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(FirebaseStorage.getInstance().getReference().child("profile_images").child(userId))
                .into(imgDp);
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
        String[] clubs = getResources().getStringArray(R.array.club_arrays);
        ArrayAdapter<String> club_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, clubs);
        edtPost.setAdapter(club_adapter);
        edtPost.setTokenizer(new SpaceTokenizer());
        edtPost.setThreshold(3);
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

        //get user verification status from SharePreference
        boolean isVerified = prefs.getBoolean("isVerified", false);
        postReference.add(new Post(username, userId, content, isVerified, 1, type, code, codeIndex));

        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Map<String, Object> updates = new HashMap<>();
                    if(type>0){
                        final ProfileMedium myProfile = documentSnapshot.toObject(ProfileMedium.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                        String currentDate = sdf.format(new Date().getTime());

                        if(type==6)
                            updates.put("d3_bankerPostTime", new Date().getTime());
                        else{
                            String lastPostDate = sdf.format(myProfile.getC8_lsPostTime());
                            long todayPostCount = myProfile.getC9_todayPostCount();
                            if(lastPostDate.equals(currentDate))
                                todayPostCount++;
                            else
                                todayPostCount =1;
                            updates.put("c8_lsPostTime", new Date().getTime());
                            updates.put("c9_todayPostCount", todayPostCount);
                        }
                        final long totalPostCount = myProfile.getE0a_NOG() + 1;
                        final long wonGamesCount = myProfile.getE0b_WG();
                        final long wonGamesPercentage = totalPostCount>0? ((wonGamesCount*100)/totalPostCount) : 0;

                        //retrieve stat for the posted game type
                        long[] stats = Reusable.getStatsForPost(myProfile, type);

                        updates.put("e0a_NOG", totalPostCount);
                        updates.put("e0c_WGP", wonGamesPercentage);
                        updates.put("e"+type + "a_NOG", stats[0]);
                        updates.put("e"+type + "c_WGP", stats[1]);
                    }
                    FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).set(updates, SetOptions.merge());
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CODE, code);
        outState.putInt(CODE_INDEX, codeIndex);
        super.onSaveInstanceState(outState);
    }
}