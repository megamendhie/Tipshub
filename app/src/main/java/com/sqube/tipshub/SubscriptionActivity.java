package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;
import models.UserNetwork;

public class SubscriptionActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId, username, myId, myUsername;
    private FirebaseFirestore database;
    DatabaseReference dbRef;
    String countryCode, currencyCode, userDefaultCountry = "nigeria";
    private CircleImageView imgDp;
    private ProfileShort profile;
    private TextView txtUsername;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtAmount, txtBenefitOne, txtBenefitTwo;
    private Button btnSubscribe;
    int[] amount = {0,0,0,0};
    int value;
    final String[] currencySymbol = {"&#8358;", "&#36;", "&#8364;", "&#xa3;", "&#8373;", "KES ", "UGX ", "TZS ", "ZAR " };
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
        dbRef = FirebaseDatabase.getInstance().getReference().child("SystemConfig").child("Subscription");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myId = user.getUid();
        myUsername = user.getDisplayName();
        userId = getIntent().getStringExtra("userId");
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
                if(UserNetwork.getProfile()!=null && !UserNetwork.getProfile().getB0_country().isEmpty())
                    userDefaultCountry = UserNetwork.getProfile().getB0_country();
                currencyCode = getCurrency(userDefaultCountry);
                setPrice(currencyCode);
            }
        });
    }

    private void setPrice(final String currency){
        dbRef.child(currency).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("SubAct", "onDataChange: " + currency);
                amount[0] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[1] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[2] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[3] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;

                //update amount tipster charges
                if(String.valueOf(profile.getD0_subAmount()).toLowerCase().equals("null"))
                    cash = amount[0];
                else
                    cash = amount[profile.getD0_subAmount()];
                String r = String.format(Locale.ENGLISH,"%s%d for 2 weeks", currencySymbol[value],cash);
                txtAmount.setText(Html.fromHtml(r));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private String getCurrency(String country){
        switch (country.toLowerCase()){
            case "nigeria":
                value=0;
                countryCode = "NG";
                return "NGN";
            case "ghana":
                value=4;
                countryCode = "GH";
                return "GHS";
            case "kenya":
                value = 5;
                countryCode = "KE";
                return "KES";
            case "uganda":
                value = 6;
                countryCode = "NG";
                return "UGX";
            case "tanzania":
                value = 7;
                countryCode = "NG";
                return "TZS";
            case "south africa":
                value=8;
                countryCode = "NG";
                return "ZAR";
            case "united kingdom":
                value=3;
                countryCode = "NG";
                return "GBP";
            case "austria":
            case "belgium":
            case "cyprus":
            case "estonia":
            case "finland":
            case "france":
            case "germany":
            case "greece":
            case "ireland":
            case "italy":
            case "Latvia":
            case "Lithuania":
            case "Luxembourg":
            case "Malta":
            case "Portugal":
            case "Slovakia":
            case "Slovenia":
            case "Spain":
                value=2;
                countryCode = "NG";
                return "EUR";
            default:
                value=1;
                countryCode = "NG";
                return "USD";
        }
    }
}
