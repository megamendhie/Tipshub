package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import services.UserDataFetcher;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    BottomNavigationView btmNav;
    android.support.v4.app.Fragment fragment;
    FragmentManager fragmentManager = getSupportFragmentManager();
    private DrawerLayout mDrawerLayout;
    View header;
    NavigationView navigationView;
    Button btnLogout;

    ActionBar actionBar;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        btmNav = findViewById(R.id.bottom_navigation);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        navigationView = findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0);
        btnLogout = header.findViewById(R.id.btnLogout); btnLogout.setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
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
