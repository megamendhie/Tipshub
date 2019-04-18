package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import services.UserDataFetcher;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView btmNav;
    android.support.v4.app.Fragment fragment;
    FragmentManager fragmentManager = getSupportFragmentManager();

    ActionBar actionBar;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        btmNav = findViewById(R.id.bottom_navigation);
        auth = FirebaseAuth.getInstance();
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
        btmNav.setOnNavigationItemSelectedListener(this);


        actionBar.setTitle("Home");
        loadFragment(new HomeFragment());
        startService(new Intent(this, UserDataFetcher.class));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_home:
                actionBar.setTitle("Home");
                fragment = new HomeFragment();
                loadFragment(fragment);
                return true;
            case R.id.nav_recommended:
                actionBar.setTitle("Recommended");
                fragment = new RecommendedFragment();
                loadFragment(fragment);
                return true;
            case R.id.nav_banker:
                actionBar.setTitle("Sure Banker");
                fragment = new BankerFragment();
                loadFragment(fragment);
                return true;
            case R.id.nav_notification:
                actionBar.setTitle("Notifications");
                fragment = new NotificationFragment();
                loadFragment(fragment);
                return true;
        }
        return false;
    }

    public void loadFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
