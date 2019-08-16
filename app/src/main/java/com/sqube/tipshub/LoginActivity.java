package com.sqube.tipshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.facebook.shimmer.ShimmerFrameLayout;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;
import utils.FirebaseUtil;
import utils.Reusable;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin;
    private final static int RC_SIGN_IN = 123;
    private EditText edtEmail, edtPassword;
    private CircleImageView imgDp;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar prgLogin;
    private ProgressDialog progressDialog;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String provider;
    private Uri filePath = null;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ShimmerFrameLayout shimmerLayout = findViewById(R.id.shimmer);
        btnLogin = findViewById(R.id.btnLogin); btnLogin.setOnClickListener(this);
        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(this);
        Button gSignIn = findViewById(R.id.gSignIn);
        gSignIn.setOnClickListener(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        prgLogin = findViewById(R.id.prgLogin);
        progressDialog = new ProgressDialog(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        if(!prefs.getString("PASSWORD", "X%p8kznAA1").equals("X%p8kznAA1")){
            edtEmail.setText(prefs.getString("EMAIL","email@domain.com"));
            edtPassword.setText(prefs.getString("PASSWORD", "X%p8kznAA1"));
        }
        shimmerLayout.startShimmer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgDp:
                grabImage();
                break;
            case R.id.btnLogin:
                signInWithEmail();
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
                e.printStackTrace();
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

    private void signInWithGoogle() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signInWithEmail(){
        email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            edtEmail.setError("Enter email");
            return;
        }
        if(TextUtils.isEmpty(password)){
            edtPassword.setError("Enter password");
            return;
        }
        FirebaseUtil.getFirebaseAuthentication().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            editor.putString("PASSWORD", edtPassword.getText().toString().trim());
                            editor.putString("EMAIL", edtEmail.getText().toString().trim());
                            editor.apply();
                            Snackbar.make(btnLogin, "Login successful", Snackbar.LENGTH_SHORT).show();
                            user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
                            finish();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()!=null)
                    FirebaseUtil.getFirebaseAuthentication().signOut();
                Snackbar.make(btnLogin, "Login failed. " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void authWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseUtil.getFirebaseAuthentication().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
                    userId = user.getUid();

                    FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult()==null || !task.getResult().exists()){
                                            String displayName = user.getDisplayName();
                                            String[] names = displayName.split(" ");
                                            firstName = names[0];
                                            lastName = names[1];
                                            email = user.getEmail();
                                            provider = "google.com";
                                            FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                                                    .set(new Profile(firstName, lastName, email, provider ));
                                            Reusable.grabImage(user.getPhotoUrl().toString());
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
                    final String message = "Login unsuccessful. " + task.getException().getMessage();
                    FirebaseUtil.getFirebaseAuthentication().signOut();
                    mGoogleSignInClient.signOut();
                    Snackbar.make(btnLogin, message, Snackbar.LENGTH_LONG).show();
                    Log.i("onComplete", message);
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

    public void completeProfile(){
        /*
        Build a dialogView for user to set profile image
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_signup2, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();

        //Initialize variables
        final boolean[] numberValid = new boolean[1];
        final EditText edtUsername = dialog.findViewById(R.id.edtUsername);
        final EditText edtPhone = dialog.findViewById(R.id.edtPhone);
        final RadioGroup rdbGroup = dialog.findViewById(R.id.rdbGroupGender);
        imgDp = dialog.findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        final EditText editTextCarrierNumber= dialog.findViewById(R.id.editText_carrierNumber);
        ccp.registerCarrierNumberEditText(editTextCarrierNumber);
        assert imgDp != null;
        Glide.with(this).load(user.getPhotoUrl()).into(imgDp);
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
                    Toast.makeText(LoginActivity.this, "Select gender", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    edtPhone.setError("Enter phone number");
                    Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!numberValid[0]){
                    edtPhone.setError("Invalid phone number");
                    Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                String finalGender = gender;
                FirebaseUtil.getFirebaseFirestore().collection("profiles")
                        .whereEqualTo("a2_username", username).limit(1).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult() == null || !task.getResult().isEmpty()) {
                                    edtUsername.setError("Username already exist");
                                    Toast.makeText(LoginActivity.this, "Username already exist", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //Map new user datails, and ready to save to db
                                Map<String, String> url = new HashMap<>();
                                url.put("a2_username", username);
                                url.put("a4_gender", finalGender);
                                url.put("b0_country", country);
                                url.put("b1_phone", phone);

                                //set the new username to firebase auth user
                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                FirebaseUtil.getFirebaseAuthentication().getCurrentUser().updateProfile(profileUpdate);

                                //save username, phone number, and gender to database
                                FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).set(url, SetOptions.merge());
                                dialog.cancel();
                                finish();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void grabImage(){
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this);
    }

    public void uploadImage(){
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        FirebaseUtil.getFirebaseStorage().getReference().child("profile_images").child(userId).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).update("b2_dpUrl", url);
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                    imgDp.setImageURI(filePath);
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
