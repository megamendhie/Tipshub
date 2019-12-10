package com.sqube.tipshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
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

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressDialog progressDialog;
    private ProgressBar prgLogin;
    private FirebaseUser user;
    private CircleImageView imgDp;
    private final static int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private Uri filePath = null;
    private Button btnSignup;
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
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
                if(!Reusable.getNetworkAvailability(getApplicationContext())){
                    Snackbar.make(edtEmail, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                registerUserWithEmail();
                break;
            case R.id.gSignIn:
                btnSignup.setEnabled(false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_contact:
                startActivity(new Intent(SignupActivity.this, ContactActivity.class));
                break;
            case R.id.nav_guide:
                startActivity(new Intent(SignupActivity.this, GuideActivity.class));
                break;
            default:
                finish();
                break;
        }
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

        FirebaseUtil.getFirebaseStorage().getReference().child("profile_images").child(userId).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).update("b2_dpUrl", url);
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

    private void enableViews(String message){
        prgLogin.setVisibility(View.GONE);
        btnSignup.setEnabled(true);
        edtPassword.setEnabled(true);
        edtEmail.setEnabled(true);
        edtFirstName.setEnabled(true);
        edtLastName.setEnabled(true);
        edtPassword.setEnabled(true);
        edtConfirmEmail.setEnabled(true);
        Snackbar.make(edtEmail, message, Snackbar.LENGTH_SHORT).show();
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
                enableViews("Cannot login at this time");
                e.printStackTrace();
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
        FirebaseUtil.getFirebaseAuthentication().signInWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
                userId = user.getUid();
                FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).get()
                        .addOnCompleteListener(task1 -> {
                            Log.i("onComplete", "get() successful " + task1.getResult().toString());
                            if(task1.isSuccessful()){
                                if(!task1.getResult().exists()){
                                    final String displayName = user.getDisplayName();
                                    String[] names = displayName.split(" ");
                                    firstName = names[0];
                                    if(names.length>1)
                                        lastName = names[1];
                                    else
                                        lastName = "";
                                    email = user.getEmail();
                                    provider = "google.com";
                                    FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                                            .set(new Profile(firstName, lastName, email, provider ));
                                    Reusable.grabImage(user.getPhotoUrl().toString());
                                    completeProfile();
                                }
                                else{
                                    finish();
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                }
                            }
                        });
            }
            else{
                final String message = "Login unsuccessful. " + task.getException().getMessage();
                enableViews(message);
                FirebaseUtil.getFirebaseAuthentication().signOut();
                mGoogleSignInClient.signOut();
            }

        });
    }

    private void registerUserWithEmail(){
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
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registering...");
        progressDialog.show();

        FirebaseUtil.getFirebaseAuthentication().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        editor.putString("PASSWORD", password);
                        editor.putString("EMAIL", email);
                        editor.apply();
                        Snackbar.make(edtEmail, "Registration successful.", Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
                        userId = user.getUid();
                        provider = "email";
                        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                                .set(new Profile(firstName, lastName, email, provider ));
                        completeProfile();
                    }
                    else
                        Snackbar.make(edtEmail, "Registration failed. Check your details.", Snackbar.LENGTH_LONG).show();
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
        final TextView txtError = dialog.findViewById(R.id.txtError);
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

        btnSave.setOnClickListener(v -> {
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
                txtError.setText("Enter username");
                txtError.setVisibility(View.VISIBLE);
                return;
            }
            if(username.length() < 3){
                edtUsername.setError("Username too short");
                txtError.setText("Username too short");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(TextUtils.isEmpty(phone)){
                txtError.setText("Enter phone number");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(!numberValid[0]){
                txtError.setText("Phone number is incorrect");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(TextUtils.isEmpty(gender)){
                txtError.setText("Select gender (M/F)");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            txtError.setVisibility(View.GONE);

            String finalGender = gender;
            FirebaseUtil.getFirebaseFirestore().collection("profiles")
                    .whereEqualTo("a2_username", username).limit(1).get()
                    .addOnCompleteListener(task -> {
                        if(task.getResult() == null || !task.getResult().isEmpty()){
                            edtUsername.setError("Username already exist");
                            Toast.makeText(SignupActivity.this, "Username already exist. Try another one", Toast.LENGTH_SHORT).show();
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
                        user.updateProfile(profileUpdate);

                        //save username, phone number, and gender to database
                        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).set(url, SetOptions.merge());
                        Reusable.updateAlgoliaIndex(firstName, lastName, username, userId, 0, true); //add to Algolia index

                        dialog.cancel();
                        finish();
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        Intent intent = new Intent(SignupActivity.this, AboutActivity.class);
                        intent.putExtra("showCongratsImage", true);
                        startActivity(intent);
                    });
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}