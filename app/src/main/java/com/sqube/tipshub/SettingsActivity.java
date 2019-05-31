package com.sqube.tipshub;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

public class SettingsActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView imgCover;
    private CircleImageView imgDp;
    private EditText edtFirstName, edtLastName, edtUsername, edtEmail, edtBio, edtCarrierNumber, edtBankDetails;
    private CountryCodePicker ccp;
    RadioGroup rdgGender, rdgSub;
    RadioButton rdMale, rdFemale, rdSub0, rdSub1, rdSub2, rdSub3;
    Profile profile;
    FirebaseFirestore database;
    FirebaseUser user;
    String userId, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        imgCover = findViewById(R.id.imgCover);
        imgDp = findViewById(R.id.imgDp);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtBio = findViewById(R.id.edtBio);
        edtBankDetails = findViewById(R.id.edtBankDetails);
        edtCarrierNumber= findViewById(R.id.editText_carrierNumber);
        ccp = findViewById(R.id.ccp);
        rdgGender = findViewById(R.id.rdbGroupGender);
        rdgSub = findViewById(R.id.rdbGroupSub);
        rdMale = findViewById(R.id.rdbMale);
        rdFemale = findViewById(R.id.rdbFemale);
        rdSub0 = findViewById(R.id.rdbSub0);
        rdSub1 = findViewById(R.id.rdbSub1);
        rdSub2 = findViewById(R.id.rdbSub2);
        rdSub3 = findViewById(R.id.rdbSub3);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
        database = FirebaseFirestore.getInstance();
        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                profile = documentSnapshot.toObject(Profile.class);
                edtFirstName.setText(profile.getA0_firstName());
                edtLastName.setText(profile.getA1_lastName());
                edtUsername.setText(profile.getA2_username());
                edtEmail.setText(profile.getA3_email());
                edtBio.setText(profile.getA5_bio());
                edtBankDetails.setText(profile.getA9_bank());
                ccp.setFullNumber(profile.getB1_phone());
                switch (profile.getA4_gender()) {
                    case "male":
                        rdMale.toggle();
                        break;
                    case "female":
                        rdFemale.toggle();
                        break;
                }
                switch ((String.valueOf(profile.getD0_subAmount()))) {
                    case "1":
                        rdSub1.toggle();
                        break;
                    case "2":
                        rdSub2.toggle();
                        break;
                    case "3":
                        rdSub1.toggle();
                        break;
                    default:
                        rdSub0.toggle();

                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
