package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import fragments.BankerFragment;
import fragments.HomeFragment;
import fragments.NotificationFragment;
import fragments.RecommendedFragment;
import models.ProfileMedium;
import models.ProfileShort;
import services.UserDataFetcher;
import utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseUser user;
    private Intent serviceIntent;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private Gson gson = new Gson();

    final int versionCode = BuildConfig.VERSION_CODE;
    final String FB_RC_KEY_TITLE = "update_title";
    final String FB_RC_KEY_DESCRIPTION = "update_description";
    final String FB_RC_KEY_FORCE_UPDATE_VERSION = "force_update_version";
    final String FB_RC_KEY_LATEST_VERSION = "latest_version";
    final HashMap<String, Object> defaultMap = new HashMap<>();

    ActionBar actionBar;
    BottomNavigationView btmNav;
    final Fragment fragmentH  = new HomeFragment();
    final Fragment fragmentR = new RecommendedFragment();
    final Fragment fragmentB = new BankerFragment();
    final Fragment fragmentN = new NotificationFragment();
    Fragment fragmentActive = fragmentH;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction;

    private DrawerLayout mDrawerLayout;
    private CircleImageView imgDp;
    TextView txtName, txtUsername, txtTips, txtFollowing, txtFollowers;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize actionBar
        actionBar = getSupportActionBar();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        //initialize bottomNavigationView
        btmNav = findViewById(R.id.bottom_navigation);
        btmNav.setOnNavigationItemSelectedListener(this);

        //initialize Preference
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        //initialize DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        MenuItem switchMenuItem = navigationView.getMenu().findItem(R.id.nav_switch);
        View actionView = switchMenuItem.getActionView();
        SwitchCompat aSwitch = actionView.findViewById(R.id.drawer_switch);

        //confirm if user reading posts from everybody
        if(prefs.getBoolean("fromEverybody", true))
            aSwitch.setChecked(true);
        else
            aSwitch.setChecked(false);

        imgDp = header.findViewById(R.id.imgProfilePic);
        imgDp.setOnClickListener(this);
        txtName = header.findViewById(R.id.txtName);
        txtName.setOnClickListener(this);
        txtUsername = header.findViewById(R.id.txtUsername);
        txtUsername.setOnClickListener(this);
        txtTips = header.findViewById(R.id.txtTips);
        txtFollowing = header.findViewById(R.id.txtFollowing);
        txtFollowers = header.findViewById(R.id.txtFollowers);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this, LandingActivity.class));
            finish();
            return;
        }
        FirebaseUtil.getFirebaseAuthentication().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()==null){
                    stopService(serviceIntent);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
            }
        });

        serviceIntent = new Intent(this, UserDataFetcher.class);
        startService(serviceIntent);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        actionBar.setTitle("Home");
        checkForUpdate();
        if(!timeIsValid())
            popUp();
        loadFragment();

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    editor.putBoolean("fromEverybody", true);
                else
                    editor.putBoolean("fromEverybody", false);
                editor.apply();
                fragmentManager.beginTransaction().detach(fragmentH).attach(fragmentH).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void popUp(){
        String message = "<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Error: Incorrect time</strong></span></p>\n" +
                "<p>Your phone time is incorrect and this may affect some app functions. Kindly set your phone time.</p>";
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setCancelable(false).setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setHeader();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                if (fragmentActive == fragmentH) {
                    fragmentManager.beginTransaction().detach(fragmentH).attach(fragmentH).commit();
                    return true;
                }
                actionBar.setTitle("Home");
                fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentH).commit();
                fragmentActive = fragmentH;
                return true;
            case R.id.nav_recommended:
                if (fragmentActive != fragmentR) {
                    actionBar.setTitle("Recommended");
                    fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentR).commit();
                    fragmentActive = fragmentR;
                }
                return true;
            case R.id.nav_banker:
                if(fragmentActive==fragmentB){
                    fragmentManager.beginTransaction().detach(fragmentB).attach(fragmentB).commit();
                    return true;
                }
                actionBar.setTitle("Sure Banker");
                fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentB).commit();
                fragmentActive = fragmentB;
                return true;
            case R.id.nav_notification:
                if(fragmentActive==fragmentN){
                    fragmentManager.beginTransaction().detach(fragmentN).attach(fragmentN).commit();
                    return true;
                }
                actionBar.setTitle("Notifications");
                fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentN).commit();
                fragmentActive = fragmentN;
                return true;
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                break;
            case R.id.nav_contact:
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_guide:
                startActivity(new Intent(MainActivity.this, GuideActivity.class));
                break;
            case R.id.nav_logout:
                if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()!=null)
                    Logout();
                else
                    Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_account:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgProfilePic:
            case R.id.txtName:
            case R.id.txtUsername:
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                break;
        }
    }

    private boolean timeIsValid(){
        String json = prefs.getString("profile", "");
        ProfileMedium myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        if(myProfile ==null)
            return true;

        Date currentTime = new Date();
        Date lastSeen = new Date(myProfile.getA8_lastSeen());
        if(lastSeen.after(currentTime))
            return false;
        Log.i("MainActivity", "timeIsValid: happended now");
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                .update("a8_lastSeen", currentTime.getTime());
        return true;
    }

    private void loadFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container,fragmentN, "fragmentN").hide(fragmentN);
        fragmentTransaction.add(R.id.main_container,fragmentB, "fragmentB").hide(fragmentB);
        fragmentTransaction.add(R.id.main_container,fragmentR, "fragmentN").hide(fragmentR);
        fragmentTransaction.add(R.id.main_container,fragmentH, "fragmentN").commit();
    }

    public void setHeader(){
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                .addSnapshotListener(MainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot!=null && documentSnapshot.exists()){
                    ProfileShort profile = documentSnapshot.toObject(ProfileShort.class);
                    txtName.setText(String.format(Locale.getDefault(), "%s %s",profile.getA0_firstName(),profile.getA1_lastName()));
                    txtUsername.setText(profile.getA2_username());
                    txtTips.setText(profile.getE0a_NOG()>1? profile.getE0a_NOG()+ " tips": profile.getE0a_NOG()+ " tip");
                    txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                    txtFollowing.setText(String.valueOf(profile.getC5_following()));
                    Glide.with(MainActivity.this)
                            .load(profile.getB2_dpUrl())
                            .into(imgDp);
                }
            }
        });
    }

    public void Logout(){
        FirebaseMessaging FCM = FirebaseMessaging.getInstance();
        FCM.unsubscribeFromTopic(userId); //unsubscribe user from corresponding channel with userId
        if(user.getProviderData().get(1).getProviderId().equals("google.com")){
            FirebaseUtil.getFirebaseAuthentication().signOut();
            mGoogleSignInClient.signOut();
        }
        else{
            FirebaseUtil.getFirebaseAuthentication().signOut();
        }
    }

    private void checkForUpdate() {
        // Hashmap which contains the default values for all the parameter defined in the remote config server
        defaultMap.put(FB_RC_KEY_TITLE, "Update Available");
        defaultMap.put(FB_RC_KEY_DESCRIPTION, "A new version of the application is available please click below to update the latest version.");
        defaultMap.put(FB_RC_KEY_FORCE_UPDATE_VERSION, ""+versionCode);
        defaultMap.put(FB_RC_KEY_LATEST_VERSION, ""+versionCode);

        // To set the default values for the remote config parameters
        mFirebaseRemoteConfig.setDefaults(defaultMap);
        /*
        // To enable the developer mode
        mFirebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build());
       */

        Task<Void> fetchTask=mFirebaseRemoteConfig.fetch(BuildConfig.DEBUG?0: TimeUnit.HOURS.toSeconds(12));

        fetchTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // After config data is successfully fetched, it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                    Boolean visible = true;
                    String title = mFirebaseRemoteConfig.getString(FB_RC_KEY_TITLE);
                    String description = mFirebaseRemoteConfig.getString(FB_RC_KEY_DESCRIPTION);
                    int forceUpdateVersion = Integer.parseInt(mFirebaseRemoteConfig.getString(FB_RC_KEY_FORCE_UPDATE_VERSION));
                    int latestAppVersion = Integer.parseInt(mFirebaseRemoteConfig.getString(FB_RC_KEY_LATEST_VERSION));
                    Log.i("Move", "onComplete: version code: "+ versionCode + "latest code: " + latestAppVersion);
                    if (latestAppVersion > versionCode){
                        if(forceUpdateVersion>versionCode)
                            visible = false;
                        updateAlert(title, description, visible);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Fetch Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAlert(String title, String description, boolean visible){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_alert, null);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button btnUpdate = alertDialog.findViewById(R.id.btnUpdate);
        Button btnLater = alertDialog.findViewById(R.id.btnLater);
        btnLater.setVisibility(visible? View.VISIBLE : View.GONE);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateApp();
            }
        });
        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
        TextView txtTitle = alertDialog.findViewById(R.id.txtTitle);
        TextView txtDescription = alertDialog.findViewById(R.id.txtDescription);
        txtTitle.setText(title);
        txtDescription.setText(description);
    }

    public void rateApp(){
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

}