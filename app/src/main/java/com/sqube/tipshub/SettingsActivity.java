package com.sqube.tipshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

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
    private ProgressDialog progressDialog;
    FirebaseFirestore database;
    FirebaseUser user;
    String userId, username;
    private Uri filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        imgCover = findViewById(R.id.imgCover);
        imgDp = findViewById(R.id.imgDp); imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabImage();
            }
        });
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
        progressDialog = new ProgressDialog(this);
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

                //set Display picture
                Glide.with(getApplicationContext())
                        .load(profile.getB2_dpUrl())
                        .into(imgDp);
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


    public void grabImage(){
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                uploadImage();
                imgDp.setImageURI(filePath);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void uploadImage(){
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        storage.getReference().child("profile_images").child(userId).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    database.collection("profiles").document(userId).update("b2_dpUrl", url);
                                    progressDialog.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                    imgDp.setImageURI(filePath);
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage((int) progress + "%" + " completed" );
                    }
                })
        ;
    }
}
