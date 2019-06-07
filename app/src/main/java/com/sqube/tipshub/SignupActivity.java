package com.sqube.tipshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private SharedPreferences prefs;
    private FirebaseUser user;
    private CircleImageView imgDp;
    private Uri filePath = null;
    SharedPreferences.Editor editor;
    EditText edtFirstName, edtLastName, edtEmail, edtConfirmEmail, edtPassword;
    private String userId, firstName, lastName, email, confirmEmail, password, provider, profileUrl, profileUrlTm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btnSignup = findViewById(R.id.btnSignup); btnSignup.setOnClickListener(this);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtConfirmEmail = findViewById(R.id.edtEmailAgain);
        edtPassword = findViewById(R.id.edtPassword);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgDp:
                grabImage();
                break;
            case R.id.btnSignup:
                registerUser();
                break;
        }
    }

    public void grabImage(){
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this);
    }

    public void uploadImage(){
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        storage.getReference().child("profile_images").child(userId).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    database.collection("profiles").document(userId).update("b2_dpUrl", url);
                                    progressDialog.dismiss();
                                    Toast.makeText(SignupActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                    imgDp.setImageURI(filePath);
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SignupActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void registerUser(){
        firstName = edtFirstName.getText().toString();
        lastName = edtLastName.getText().toString();
        email = edtEmail.getText().toString();
        confirmEmail = edtConfirmEmail.getText().toString();
        password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            edtFirstName.setError("Enter first name");
            return;}

        if (TextUtils.isEmpty(lastName)) {
            edtLastName.setError("Enter last name");
            return;}

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Enter email");
            return;}

        if (TextUtils.isEmpty(confirmEmail)) {
            edtEmail.setError("Confirm email");
            return;}

        if(!email.equals(confirmEmail)){
            edtEmail.setError("Email doesn't match");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Enter password");
            return;}

        if (password.length() < 5) {
            edtPassword.setError("password too small");
            return;}
        progressDialog.setMessage("Registering...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            editor.putString("PASSWORD", password);
                            editor.putString("EMAIL", email);
                            editor.apply();
                            Snackbar.make(edtEmail, "Registration successful.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            userId = user.getUid();
                            provider = "email";
                            database.collection("profiles").document(userId)
                                    .set(new Profile(firstName, lastName, email, provider ));
                            completeProfile();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(edtEmail, "Registration failed. " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void completeProfile(){
        /*
        Build a dialogView for user to set profile image
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_signup2, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();

        //Initialize variables
        final boolean[] numberValid = {false};
        final EditText edtUsername = dialog.findViewById(R.id.edtUsername);
        final EditText edtPhone = dialog.findViewById(R.id.editText_carrierNumber);
        final RadioGroup rdbGroup = dialog.findViewById(R.id.rdbGroupGender);
        imgDp = dialog.findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(edtPhone);
        Glide.with(this).load(R.drawable.sample_dp).into(imgDp);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> numberValid[0] =isValidNumber);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String phone = ccp.getFullNumber();
                String country = ccp.getSelectedCountryName();
                String gender ="";
                switch (rdbGroup.getCheckedRadioButtonId()) {
                    case R.id.rdbMale:
                        gender = "male";
                        break;
                    case R.id.rdbFemale:
                        gender = "female";
                        break;
                }
                //verify fields meet requirement
                if(TextUtils.isEmpty(username)){
                    edtUsername.setError("Enter username");
                    return;
                }
                if(username.length() < 3){
                    edtUsername.setError("Username too short");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    edtPhone.setError("Enter phone number");
                    return;
                }
                if(!numberValid[0]){
                    edtPhone.setError("Invalid phone number");
                    return;
                }
                if(TextUtils.isEmpty(gender)){
                    Toast.makeText(SignupActivity.this, "Select gender", Toast.LENGTH_LONG).show();
                    return;
                }

                //Map new user datails, and ready to save to db
                Map<String, String> url = new HashMap<>();
                url.put("a2_username", username);
                url.put("a4_gender", gender);
                url.put("b0_country", country);
                url.put("b1_phone", phone);

                //set the new username to firebase auth user
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                mAuth.getCurrentUser().updateProfile(profileUpdate);

                //save username, phone number, and gender to database
                database.collection("profiles").document(userId).set(url, SetOptions.merge());
                dialog.cancel();
                finish();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
            }
        });
    }

}
