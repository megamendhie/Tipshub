package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Locale;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

public class SubscriptionActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId, username, myId, myUsername;
    private FirebaseFirestore database;
    private CircleImageView imgDp;
    private ProfileShort profile;
    private TextView txtUsername;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtAmount, txtBenefitOne, txtBenefitTwo;
    private Button btnSubscribe;
    int[] amount = {0,0,0,0};
    String[] currency = {"&#8358;", "&#36;", "&#8364;", "&#8373;", "KES", "UGX", "TZS" };
    int cash=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }

        //initialize views
        imgDp = findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        txtUsername = findViewById(R.id.txtUsername); txtUsername.setOnClickListener(this);
        txtPost = findViewById(R.id.txtPost);
        txtWon = findViewById(R.id.txtWon);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtAmount = findViewById(R.id.txtAmount);
        txtBenefitOne = findViewById(R.id.txtBenefitOne);
        txtBenefitTwo = findViewById(R.id.txtBenefitTwo);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myId = user.getUid();
        myUsername = user.getDisplayName();
        userId = getIntent().getStringExtra("userId");
        database.collection("settings").document("subscriptions")
                .addSnapshotListener(SubscriptionActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            amount[0] = documentSnapshot.get("sub1", Integer.class)!=null? documentSnapshot.get("sub1", int.class): 0;
                            amount[1] = documentSnapshot.get("sub2", Integer.class)!=null? documentSnapshot.get("sub2", int.class): 0;
                            amount[2] = documentSnapshot.get("sub3", Integer.class)!=null? documentSnapshot.get("sub3", int.class): 0;
                            amount[3] = documentSnapshot.get("sub4", Integer.class)!=null? documentSnapshot.get("sub4", int.class): 0;
                        }
                    }
                });
        displayViews();
    }

    private void displayViews(){
        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                ProgressBar pg = findViewById(R.id.prgPost);
                pg.setVisibility(View.GONE);
                btnSubscribe.setOnClickListener(SubscriptionActivity.this);
                profile = documentSnapshot.toObject(ProfileShort.class);
                username = profile.getA2_username();
                txtUsername.setText(profile.getA2_username());
                txtPost.setText(profile.getE6a_NOG() + " banker tips  • ");
                txtWon.setText(profile.getE6b_WG()+ " won");
                String a = String.format(Locale.ENGLISH,"%d",profile.getE6c_WGP()); // accuracy of baner
                txtAccuracy.setText("Banker Accuracy: " + a + "%");
                txtBenefitOne.setText("See all banker tips from "+ username);
                txtBenefitTwo.setText("Receive notification when "+ username +" posts banker tips");

                //update amount tipster charges
                if(String.valueOf(profile.getD0_subAmount()).toLowerCase().equals("null"))
                    cash = amount[0];
                else
                    cash = amount[profile.getD0_subAmount()];
                String r = String.format(Locale.ENGLISH,"&#8358;%d for 2 weeks",cash);
                txtAmount.setText(Html.fromHtml(r));
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
            case R.id.txtUsername:
            case R.id.imgDp:
                Intent intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                break;
        }
    }
}
