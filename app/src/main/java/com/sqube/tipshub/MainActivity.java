package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;
import services.UserDataFetcher;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private FirebaseUser user;

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
    NavigationView navigationView;
    View header;
    Button btnLogout;

    private CircleImageView imgDp;
    TextView txtName, txtUsername, txtFollowing, txtFollowers;
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

        //initialize DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);

        imgDp = header.findViewById(R.id.imgProfilePic);
        imgDp.setOnClickListener(this);
        txtName = header.findViewById(R.id.txtName);
        txtUsername = header.findViewById(R.id.txtUsername);
        txtFollowing = header.findViewById(R.id.txtFollowing);
        txtFollowers = header.findViewById(R.id.txtFollowers);

        btnLogout = header.findViewById(R.id.btnLogout); btnLogout.setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(auth.getCurrentUser()==null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
            }
        });
        database = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        actionBar.setTitle("Home");
        loadFragment();
        startService(new Intent(this, UserDataFetcher.class));
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

    private void loadFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container,fragmentN, "fragmentN").hide(fragmentN);
        fragmentTransaction.add(R.id.main_container,fragmentB, "fragmentB").hide(fragmentB);
        fragmentTransaction.add(R.id.main_container,fragmentR, "fragmentN").hide(fragmentR);
        fragmentTransaction.add(R.id.main_container,fragmentH, "fragmentN").commit();
    }

    public void setHeader(){
        database.collection("profiles").document(userId)
                .addSnapshotListener(MainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    ProfileShort profile = documentSnapshot.toObject(ProfileShort.class);
                    txtName.setText(profile.getA0_firstName()+" "+profile.getA1_lastName());
                    txtUsername.setText(profile.getA2_username());
                    txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                    txtFollowing.setText(String.valueOf(profile.getC5_following()));
                }
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        switch (id){
            case R.id.nav_home:
                if(fragmentActive==fragmentH){
                    fragmentManager.beginTransaction().detach(fragmentH).attach(fragmentH).commit();
                    return true;
                }
                actionBar.setTitle("Home");
                fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentH).commit();
                fragmentActive = fragmentH;
                return true;
            case R.id.nav_recommended:
                if(fragmentActive==fragmentR){
                    fragmentManager.beginTransaction().detach(fragmentR).attach(fragmentR).commit();
                    return true;
                }
                actionBar.setTitle("Recommended");
                fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentR).commit();
                fragmentActive = fragmentR;
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
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnLogout){
            if(auth.getCurrentUser()!=null)
                Logout();
            else
                Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_LONG).show();
        }
    }

    public void Logout(){
        final List<String> providers = auth.getCurrentUser().getProviders();
        if(providers.get(0).equals("google.com")){
            auth.signOut();
            mGoogleSignInClient.signOut();
        }
        else{
            auth.signOut();
        }
    }
}
