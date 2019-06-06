package com.sqube.tipshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSignup;
    private FirebaseAuth mAuth;
    FirebaseFirestore database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    SharedPreferences prefs;
    FirebaseUser user;
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
            case R.id.btnSignup:
                registerUser();
                break;
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
        progressDialog.setMessage("Registering Please Wait...");
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
        final boolean[] numberValid = new boolean[1];
        final EditText edtUsername = dialog.findViewById(R.id.edtUsername);
        final EditText edtPhone = dialog.findViewById(R.id.edtUsername);
        final RadioGroup rdbGroup = dialog.findViewById(R.id.rdbGroupGender);
        CircleImageView imgDp = dialog.findViewById(R.id.imgDp);
        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        final EditText editTextCarrierNumber= dialog.findViewById(R.id.editText_carrierNumber);
        ccp.registerCarrierNumberEditText(editTextCarrierNumber);
        Glide.with(this).load(R.drawable.sample_dp).into(imgDp);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        ccp.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                numberValid[0] =isValidNumber;
            }
        });
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
                if(username.length() < 2){
                    edtUsername.setError("Username too short");
                    return;
                }
                if(TextUtils.isEmpty(gender)){
                    Toast.makeText(SignupActivity.this, "Select gender", Toast.LENGTH_LONG).show();
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

                //Map new user datails, and ready to save to db
                Map<String, String> url = new HashMap<>();
                url.put("a2_username", username);
                url.put("a4_gender", gender);
                url.put("b0_country", country);
                url.put("b1_phone", phone);
                url.put("b2_dpUrl", profileUrl);
                url.put("b3_dpTmUrl", profileUrlTm);

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
        //uploadDp();
    }

}
