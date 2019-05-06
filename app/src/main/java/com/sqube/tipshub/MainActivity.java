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
    //android.support.v4.app.Fragment fragment;
    final Fragment fragmentH  = new HomeFragment();
    final Fragment fragmentR = new RecommendedFragment();
    final Fragment fragmentB = new BankerFragment();
    final Fragment fragmentN = new NotificationFragment();
    Fragment fragmentActive = fragmentH;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction;
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
        btmNav.setOnNavigationItemSelectedListener(this);


        actionBar.setTitle("Home");
        loadFragmentAgain();
        startService(new Intent(this, UserDataFetcher.class));
    }

    private void loadFragmentAgain() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container,fragmentN, "fragmentN").hide(fragmentN).commit();
        fragmentTransaction.add(R.id.main_container,fragmentB, "fragmentB").hide(fragmentB).commit();
        fragmentTransaction.add(R.id.main_container,fragmentR, "fragmentN").hide(fragmentR).commit();
        fragmentTransaction.add(R.id.main_container,fragmentH, "fragmentN").commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_home:
                if(fragmentActive==fragmentH){
                    fragmentTransaction.detach(fragmentH).attach(fragmentH).commit();
                    return true;
                }
                actionBar.setTitle("Home");
                fragmentTransaction.hide(fragmentActive).show(fragmentH).commit();
                fragmentActive = fragmentH;
                return true;
            case R.id.nav_recommended:
                if(fragmentActive==fragmentR){
                    fragmentTransaction.detach(fragmentR).attach(fragmentR).commit();
                    return true;
                }
                actionBar.setTitle("Recommended");
                fragmentTransaction.hide(fragmentActive).show(fragmentR).commit();
                fragmentActive = fragmentR;
                return true;
            case R.id.nav_banker:
                if(fragmentActive==fragmentB){
                    fragmentTransaction.detach(fragmentB).attach(fragmentB).commit();
                    return true;
                }
                actionBar.setTitle("Sure Banker");
                fragmentTransaction.hide(fragmentActive).show(fragmentB).commit();
                fragmentActive = fragmentB;
                return true;
            case R.id.nav_notification:
                if(fragmentActive==fragmentN){
                    fragmentTransaction.detach(fragmentN).attach(fragmentN).commit();
                    return true;
                }
                actionBar.setTitle("Notifications");
                fragmentTransaction.hide(fragmentActive).show(fragmentN).commit();
                fragmentActive = fragmentN;
                return true;
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
