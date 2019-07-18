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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
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
import utils.Reusable;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private ProgressBar prgLogin;
    private SharedPreferences prefs;
    private FirebaseUser user;
    private CircleImageView imgDp;
    private final static int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private Uri filePath = null;
    SharedPreferences.Editor editor;
    EditText edtFirstName, edtLastName, edtEmail, edtConfirmEmail, edtPassword;
    private String userId, firstName, lastName, email, confirmEmail, password, provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Sign Up");
        }
        Button gSignIn = findViewById(R.id.gSignIn);
        gSignIn.setOnClickListener(this);
        prgLogin = findViewById(R.id.prgLogin);
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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(SignupActivity.this, gso);
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
            case R.id.gSignIn:
                edtPassword.setEnabled(false);
                edtEmail.setEnabled(false);
                edtFirstName.setEnabled(false);
                edtLastName.setEnabled(false);
                edtPassword.setEnabled(false);
                edtConfirmEmail.setEnabled(false);
                prgLogin.setVisibility(View.VISIBLE);
                signInWithGoogle();
                break;
        }
    }

    private void signInWithGoogle() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
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
        if(requestCode==RC_SIGN_IN ){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authWithGoogle(account);
                Log.i("LoginActivity", "onActivityResult: account Retrieved successfully");
            } catch (ApiException e) {
                prgLogin.setVisibility(View.GONE);
                edtPassword.setEnabled(true);
                edtEmail.setEnabled(true);
                edtFirstName.setEnabled(true);
                edtLastName.setEnabled(true);
                edtPassword.setEnabled(true);
                edtConfirmEmail.setEnabled(true);
                Log.i("LoginActivity", "onActivityResult: account not Retrieved");
            }

        }
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

    public void authWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    user = mAuth.getCurrentUser();
                    userId = user.getUid();
                    database.collection("profiles").document(userId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Log.i("onComplete", "get() successful " + task.getResult().toString());
                            if(task.isSuccessful()){
                                if(!task.getResult().exists()){
                                    final String displayName = user.getDisplayName();
                                    String[] names = displayName.split(" ");
                                    firstName = names[0];
                                    lastName = names[1];
                                    email = user.getEmail();
                                    provider = "google.com";
                                    database.collection("profiles").document(userId)
                                            .set(new Profile(firstName, lastName, email, provider ));
                                    Reusable.grabImage(user.getPhotoUrl().toString());
                                    completeProfile();
                                }
                                else{
                                    finish();
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                }
                            }
                        }
                    });
                }
                else{
                    edtPassword.setEnabled(true);
                    edtEmail.setEnabled(true);
                    prgLogin.setVisibility(View.GONE);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                edtPassword.setEnabled(true);
                edtEmail.setEnabled(true);
                prgLogin.setVisibility(View.GONE);
            }
        });
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
        if(user.getPhotoUrl()==null)
            Glide.with(this).load(R.drawable.dummy).into(imgDp);
        else
            Glide.with(this).load(user.getPhotoUrl()).into(imgDp);

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
                user.updateProfile(profileUpdate);

                //save username, phone number, and gender to database
                database.collection("profiles").document(userId).set(url, SetOptions.merge());
                dialog.cancel();
                finish();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
            }
        });
    }

}