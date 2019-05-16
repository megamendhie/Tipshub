package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

public class SubscriptionActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseUser user;
    private String userId, username, myId, myUsername;
    private FirebaseFirestore database;
    private CircleImageView imgDp;
    ProfileShort profile;
    private TextView txtUsername;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtAmount, txtAmountLocal, txtBenefitOne, txtBenefitTwo;
    private Button btnSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        //initialize views
        imgDp = findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        txtUsername = findViewById(R.id.txtUsername);
        txtPost = findViewById(R.id.txtPost);
        txtWon = findViewById(R.id.txtWon);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtAmount = findViewById(R.id.txtAmount);
        txtAmountLocal = findViewById(R.id.txtAmountLocal);
        txtBenefitOne = findViewById(R.id.txtBenefitOne);
        txtBenefitTwo = findViewById(R.id.txtBenefitTwo);
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        myId = user.getUid();
        myUsername = user.getDisplayName();
        userId = getIntent().getStringExtra("userId");
        displayViews();
    }

    private void displayViews(){
        txtBenefitOne.setText("> See all banker tips from "+ username);
        txtBenefitTwo.setText(">Receive notification when "+ username +" posts banker tips");
        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                profile = documentSnapshot.toObject(ProfileShort.class);
                txtUsername.setText(profile.getA2_username());
                txtPost.setText(profile.getE6a_NOG() + " banker tips  • ");
                txtWon.setText(profile.getE6b_WG()+ " won");
                txtAccuracy.setText("Accuracy: " + profile.getE6c_WGP()+ "%");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.imgDp){
            Intent intent = new Intent(this, MemberProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }
}
