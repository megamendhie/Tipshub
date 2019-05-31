package com.sqube.tipshub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnLogin, btnSignup, gSignIn;
    private final static int RC_SIGN_IN = 123;
    EditText edtEmail, edtPassword;
    FirebaseFirestore database;
    FirebaseStorage storage;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ProgressBar prgLogin;
    private String userId, firstName, lastName, email, provider, profileUrl, profileUrlTm;
    Uri profileUri, profileUriTm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.btnLogin); btnLogin.setOnClickListener(this);
        btnSignup = findViewById(R.id.btnSignup); btnSignup.setOnClickListener(this);
        gSignIn = findViewById(R.id.gSignIn); gSignIn.setOnClickListener(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        prgLogin = findViewById(R.id.prgLogin);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Log.i("LoginActivity", "onActivityResult: account not Retrieved");
            }

        }
    }

    private void signInWithGoogle() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void authWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userId = mAuth.getCurrentUser().getUid();
                    Log.i("onComplete", "onAuthWithGoogle: login successful");
                    Log.i("onComplete", "onAuthWithGoogle: provider: " + mAuth.getCurrentUser().getProviderId());
                    Log.i("onComplete", "photoUrl: " + mAuth.getCurrentUser().getPhotoUrl().toString());
                    database.collection("profiles").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Log.i("onComplete", "get() successful " + task.getResult().toString());
                                    if(task.isSuccessful()){
                                        if(!task.getResult().exists()){
                                            final String displayName = mAuth.getCurrentUser().getDisplayName();
                                            String[] names = displayName.split(" ");
                                            firstName = names[0];
                                            lastName = names[1];
                                            email = mAuth.getCurrentUser().getEmail();
                                            provider = "google.com";
                                            database.collection("profiles").document(userId)
                                                    .set(new Profile(firstName, lastName, email, provider ));
                                            completeProfile();
                                        }
                                        else{
                                            finish();
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                    }
                                }
                    });
                }
                else{
                    edtPassword.setEnabled(true);
                    edtEmail.setEnabled(true);
                    prgLogin.setVisibility(View.GONE);
                    Log.i("onComplete", "onAuthWithGoogle: login unsuccessful" + task.getException().getMessage());
                }

            }
        });
    }

    public void uploadDp(){
        storage.getReference().child("profile_image_thumbnails").child(userId)
                .putFile(profileUriTm)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String, String> url = new HashMap<>();
                                        url.put("b3_dpTmUrl", uri.toString());
                                        database.collection("profiles").document(userId).set(url);
                                    }
                                });
                    }
        });
        storage.getReference().child("profile_images").child(userId)
                .putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, String> url = new HashMap<>();
                                url.put("b2_dpUrl", uri.toString());
                                database.collection("profiles").document(userId).set(url);
                            }
                        });
            }
        });
    }

    public void completeProfile(){
        /*
        Build a dialogView for user to set profile image
         */
        if(mAuth.getCurrentUser().getPhotoUrl()!=null){
            profileUriTm = mAuth.getCurrentUser().getPhotoUrl();
            profileUrlTm = profileUriTm.toString();
            profileUrl = profileUrlTm.replace("s96-c/photo.jpg", "s400-c/photo.jpg");
            profileUri = Uri.parse(profileUrl);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_signup2, null);
        builder.setView(dialogView);

        final AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();
        final EditText edtUsername = dialog.findViewById(R.id.edtUsername);
        final EditText edtPhone = dialog.findViewById(R.id.edtUsername);
        final RadioGroup rdbGroup = dialog.findViewById(R.id.rdbGroupGender);
        CircleImageView imgDp = dialog.findViewById(R.id.imgDp);
        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        final EditText editTextCarrierNumber= dialog.findViewById(R.id.editText_carrierNumber);
        ccp.registerCarrierNumberEditText(editTextCarrierNumber);
        Glide.with(this).load(profileUri).into(imgDp);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String gender ="";
                String country = ccp.getSelectedCountryName();
                switch (rdbGroup.getCheckedRadioButtonId()) {
                    case R.id.rdbMale:
                        gender = "male";
                        break;
                    case R.id.rdbFemale:
                        gender = "female";
                        break;
                }
                //verify fields meet requirement
                if(TextUtils.isEmpty(gender)){
                    Toast.makeText(LoginActivity.this, "Select gender", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(username)){
                    Toast.makeText(LoginActivity.this, "Choose username", Toast.LENGTH_LONG).show();
                    return;
                }
                if(username.length() < 2){
                    Toast.makeText(LoginActivity.this, "Username is too short", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(phone) || phone.length()<3){
                    Toast.makeText(LoginActivity.this, "Enter phone number", Toast.LENGTH_LONG).show();
                    return;
                }

                if(phone.charAt(0) == '0')
                    phone = phone.substring(1);
                phone = ccp.getSelectedCountryCode()+phone;
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
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
        //uploadDp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                break;
            case R.id.btnSignup:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                break;
            case R.id.gSignIn:
                edtPassword.setEnabled(false);
                edtEmail.setEnabled(false);
                prgLogin.setVisibility(View.VISIBLE);
                signInWithGoogle();
                break;
        }
    }
}
