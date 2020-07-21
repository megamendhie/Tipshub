package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import fragments.BankerFragment;
import fragments.HomeFragment;
import fragments.NotificationFragment;
import fragments.RecommendedFragment;
import models.ProfileMedium;
import models.ProfileShort;
import models.UserNetwork;
import services.DailyNotificationWorker;
import services.NotificationCheckWorker;
import services.UserDataFetcher;
import utils.FirebaseUtil;

import static utils.Calculations.NOTIFICATION_WORKER_ID;
import static utils.Calculations.WORKER_ACTIVATED;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseUser user;
    private Intent serviceIntent;
    private ArrayList<String> unseenNotList = new ArrayList<>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson = new Gson();

    final int versionCode = BuildConfig.VERSION_CODE;
    final String FB_RC_KEY_TITLE = "update_title";
    final String FB_RC_KEY_DESCRIPTION = "update_description";
    final String FB_RC_KEY_FORCE_UPDATE_VERSION = "force_update_version";
    final String FB_RC_KEY_LATEST_VERSION = "latest_version";
    final HashMap<String, Object> defaultMap = new HashMap<>();

    private ActionBar actionBar;
    private BottomNavigationView btmNav;
    final Fragment fragmentH  = new HomeFragment();
    final Fragment fragmentR = new RecommendedFragment();
    final Fragment fragmentB = new BankerFragment();
    final Fragment fragmentN = new NotificationFragment();
    private Fragment fragmentActive = fragmentH;
    private FragmentManager fragmentManager = getSupportFragmentManager();

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
            startActivity(new Intent(MainActivity.this, LandActivity.class));
            finish();
            return;
        }

        serviceIntent = new Intent(MainActivity.this, UserDataFetcher.class);
        startService(serviceIntent);

        FirebaseUtil.getFirebaseAuthentication().addAuthStateListener(firebaseAuth -> {
            if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()==null){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("openMainActivity", true);
                finish();
                startActivity(intent);
            }
        });

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        actionBar.setTitle("Home");
        checkForUpdate();
        if(!timeIsValid())
            popUp();
        setBadge();
        loadFragment();
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                editor.putBoolean("fromEverybody", true);
            else
                editor.putBoolean("fromEverybody", false);
            editor.apply();
            fragmentManager.beginTransaction().detach(fragmentH).attach(fragmentH).commit();
            mDrawerLayout.closeDrawer(GravityCompat.START);
        });
        setWorkManager();
    }

    private void setWorkManager() {
        WorkManager workManager = WorkManager.getInstance(MainActivity.this);
        if(!prefs.getBoolean(WORKER_ACTIVATED, false)){
            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationCheckWorker.class, 30, TimeUnit.MINUTES)
                    .build();
            workManager.enqueue(workRequest);

            PeriodicWorkRequest workRequestDaily = new PeriodicWorkRequest.Builder(DailyNotificationWorker.class, 20, TimeUnit.HOURS)
                    .setInitialDelay(6, TimeUnit.HOURS).build();
            workManager.enqueue(workRequestDaily);

            String uuid = workRequest.getId().toString();
            editor.putBoolean(WORKER_ACTIVATED, true);
            editor.putString(NOTIFICATION_WORKER_ID, uuid);
            editor.apply();
        }
        else{
            UUID id = UUID.fromString(prefs.getString(NOTIFICATION_WORKER_ID, ""));
            workManager.getWorkInfoByIdLiveData(id).observe(this, workInfo -> {
                if (workInfo==null){
                    Log.i("WorkManager", "setWorkManager: Worker is null");
                    Snackbar.make(imgDp, "Worker is null", Snackbar.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceIntent!=null)
            stopService(serviceIntent);
    }

    private void setBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) btmNav.getChildAt(0);
        BottomNavigationItemView itemView =  (BottomNavigationItemView) menuView.getChildAt(3); //get notification item

        View notificationBadge= LayoutInflater.from(this).inflate(R.layout.notification_badge, menuView, false);
        notificationBadge.setVisibility(View.GONE);
        TextView txtBadge = notificationBadge.findViewById(R.id.badge);
        itemView.addView(notificationBadge);

        FirebaseUtil.getFirebaseFirestore().collection("notifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId).whereEqualTo("seen", false)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()){
                        int count = queryDocumentSnapshots.size();
                        txtBadge.setText(count>=9? count+"+" : String.valueOf(count));
                        notificationBadge.setVisibility(View.VISIBLE);
                        unseenNotList.clear();
                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments())
                            unseenNotList.add(snap.getId());
                    }
                    else
                        notificationBadge.setVisibility(View.GONE);

                });
    }

    private void clearNotification() {
        if(unseenNotList.isEmpty())
            return;
        for (int i=0; i < unseenNotList.size(); i++){
            FirebaseUtil.getFirebaseFirestore().collection("notifications").document(unseenNotList.get(i))
            .update("seen", true);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recommended_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.mnuSearch:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
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
                clearNotification();
                return true;
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                break;
            case R.id.nav_contact:
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_guide:
                startActivity(new Intent(MainActivity.this, GuideActivity.class));
                break;
            case R.id.nav_logout:
                showLogoutPrompt();
                break;
            case R.id.nav_account:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
        }
        return false;
    }

    private void showLogoutPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("Do you want to logout of Tipshub?")
                .setTitle("Logout")
                .setIcon(R.drawable.ic_power_settings_new_color_24dp)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()!=null)
                            Logout();
                        else
                            Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
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
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
        FCM.unsubscribeFromTopic(userId);

        ArrayList<String> sub_to = UserNetwork.getSubscribed();
        if(sub_to!=null && !sub_to.isEmpty()){
            for(String s: sub_to){
                FCM.unsubscribeFromTopic("sub_"+s);
            }
        }

        clearCache();
        if(user.getProviderData().get(1).getProviderId().equals("google.com")){
            FirebaseUtil.getFirebaseAuthentication().signOut();
            mGoogleSignInClient.signOut();
        }
        else
            FirebaseUtil.getFirebaseAuthentication().signOut();
    }

    private void clearCache() {
        UserNetwork.setFollowers(null);
        UserNetwork.setFollowing(null);
        UserNetwork.setSubscribed(null);
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
                    boolean visible = true;
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
        TextView btnLater = alertDialog.findViewById(R.id.btnLater);
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
        txtDescription.setText(Html.fromHtml(description));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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