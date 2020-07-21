package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import adapters.PerformanceAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import fragments.BankersFragment;
import fragments.PostFragment;
import fragments.ReviewFragment;
import models.ProfileMedium;
import models.UserNetwork;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

public class MemberProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private ActionBar actionBar;
    private String userId;
    private String USER_ID = "userId";
    private RequestOptions requestOptions = new RequestOptions();
    private FirebaseFirestore database;
    private FirebaseUser user;
    private CircleImageView imgDp;
    private LinearLayout[] lnrLayout = new LinearLayout[4];
    private ProfileMedium profile;
    private RecyclerView recyclerView;
    PerformanceAdapter adapter;
    private String myID, imgUrl;
    Fragment postFragment, bankerFragment, reviewFragment;
    ArrayList<Map<String, Object>> performanceList = new ArrayList<>();
    private TextView btnFollow, btnSubscribe, txtWhatsapp;
    private TextView txtName, txtUsername, txtBio;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtFollowers, txtFollowing, txtSubscribers, txtSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        btnFollow = findViewById(R.id.btnFollow); btnFollow.setOnClickListener(this);
        btnSubscribe = findViewById(R.id.btnSubscribe); btnSubscribe.setOnClickListener(this);
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        imgDp = findViewById(R.id.imgDp);
        txtWhatsapp = findViewById(R.id.txtWhatsapp);
        txtName = findViewById(R.id.txtFullName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtPost = findViewById(R.id.txtPost);
        txtWon = findViewById(R.id.txtWon);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        txtSubscribers = findViewById(R.id.txtSubscribers);
        txtSubscriptions = findViewById(R.id.txtSubscription);
        lnrLayout[0] = findViewById(R.id.lnrFollowing);
        lnrLayout[1] = findViewById(R.id.lnrFollowers);
        lnrLayout[2] = findViewById(R.id.lnrSubscribers);
        lnrLayout[3] = findViewById(R.id.lnrSubscription);
        for(LinearLayout l: lnrLayout)
            l.setOnClickListener(this);
        recyclerView = findViewById(R.id.performanceList);

        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            myID = Calculations.GUEST;
        else
            myID = user.getUid();
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        adapter = new PerformanceAdapter(performanceList);
        recyclerView.setLayoutManager(lm);
        if(savedInstanceState!=null)
            userId = savedInstanceState.getString(USER_ID);
        else
            userId = getIntent().getStringExtra(USER_ID);
        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userId)? "FOLLOWING": "FOLLOW");

        database = FirebaseFirestore.getInstance();
        requestOptions.placeholder(R.drawable.dummy);
        setupViewPager(viewPager); //set up view pager with fragments
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            myID = Calculations.GUEST;
        else
            myID = user.getUid();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        database.collection("profiles").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot==null||!documentSnapshot.exists())
                        return;
                    profile = documentSnapshot.toObject(ProfileMedium.class);
                    imgUrl = profile.getB2_dpUrl();
                    if(!imgUrl.isEmpty())
                        imgDp.setOnClickListener(MemberProfileActivity.this);
                    String name = String.format(Locale.getDefault(),"%s %s", profile.getA0_firstName(),profile.getA1_lastName());
                    actionBar.setTitle(name);
                    txtName.setText(name);
                    txtUsername.setText(String.format(Locale.getDefault(),"@%s",profile.getA2_username()));
                    txtBio.setText(profile.getA5_bio());
                    txtBio.setVisibility(profile.getA5_bio().isEmpty()?View.GONE:View.VISIBLE);
                    Reusable.applyLinkfy(MemberProfileActivity.this, profile.getA5_bio(), txtBio);
                    if(profile.isD5_allowChat())
                        txtWhatsapp.setVisibility(View.VISIBLE);
                    txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                    txtFollowing.setText(String.valueOf(profile.getC5_following()));
                    txtSubscribers.setText(String.valueOf(profile.getC6_subscribers()));
                    txtSubscriptions.setText(String.valueOf(profile.getC7_subscriptions()));
                    String tips = profile.getE0a_NOG()>1? "tips": "tip";
                    txtPost.setText(String.format(Locale.getDefault(),"%d  %s  • ", profile.getE0a_NOG(), tips));
                    txtWon.setText(String.format(Locale.getDefault(),"%d  won  • ", profile.getE0b_WG()));
                    txtAccuracy.setText(String.format(Locale.getDefault(),"%.1f%%", (double) profile.getE0c_WGP()));

                    if(UserNetwork.getSubscribed()!=null && profile.isC1_banker()
                            && !UserNetwork.getSubscribed().contains(userId))
                        btnSubscribe.setVisibility(View.VISIBLE);

                    //set Display picture
                    Glide.with(getApplicationContext())
                            .setDefaultRequestOptions(requestOptions)
                            .load(profile.getB2_dpUrl())
                            .into(imgDp);

                    if(!performanceList.isEmpty())
                        return;

                    if(profile.getE0a_NOG()>0){
                        for(int i=1; i<=6; i++){
                            Map<String, Object> row = getRow(i);
                            if(!row.isEmpty())
                                performanceList.add(row);
                        }
                        recyclerView.setAdapter(adapter);
                    }
                });

    }

    private Map<String, Object> getRow(int i) {
        Map<String, Object> row = new HashMap<>();
        switch (i){
            case 1:
                if(profile.getE1a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE1a_NOG());
                    row.put("WG", profile.getE1b_WG());
                    row.put("WGP", profile.getE1c_WGP());
                }
                break;
            case 2:
                if(profile.getE2a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE2a_NOG());
                    row.put("WG", profile.getE2b_WG());
                    row.put("WGP", profile.getE2c_WGP());
                }
                break;
            case 3:
                if(profile.getE3a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE3a_NOG());
                    row.put("WG", profile.getE3b_WG());
                    row.put("WGP", profile.getE3c_WGP());
                }
                break;
            case 4:
                if(profile.getE4a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE4a_NOG());
                    row.put("WG", profile.getE4b_WG());
                    row.put("WGP", profile.getE4c_WGP());
                }
                break;
            case 5:
                if(profile.getE5a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE5a_NOG());
                    row.put("WG", profile.getE5b_WG());
                    row.put("WGP", profile.getE5c_WGP());
                }
                break;
            case 6:
                if(profile.getE6a_NOG()>0){
                    row.put("type", i);
                    row.put("NOG", profile.getE6a_NOG());
                    row.put("WG", profile.getE6b_WG());
                    row.put("WGP", profile.getE6c_WGP());
                }
                break;
        }
        return row;
    }

    private void setupViewPager(ViewPager viewPager) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
        postFragment = new PostFragment();
        bankerFragment = new BankersFragment();
        reviewFragment = new ReviewFragment();
        //passing bunder with userId to fragment
        postFragment.setArguments(bundle);
        bankerFragment.setArguments(bundle);
        reviewFragment.setArguments(bundle);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(postFragment, "Posts");
        adapter.addFragment(bankerFragment, "Bankers");
        adapter.addFragment(reviewFragment, "Review");
        viewPager.setAdapter(adapter);
    }

    private void showDp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MemberProfileActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView;
        dialogView = inflater.inflate(R.layout.image_viewer, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();
        ImageView imgProfile = dialog.findViewById(R.id.imgDp);
        //set Display picture
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(profile.getB2_dpUrl())
                .into(imgProfile);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, FollowerListActivity.class);
        intent.putExtra("personId", userId);
        switch (v.getId()){
            case R.id.imgDp:
                showDp();
                break;
            case R.id.lnrFollowers:
                if(Integer.valueOf(txtFollowers.getText().toString())<1)
                    return;
                intent.putExtra("search_type", "followers");
                startActivity(intent);
                break;
            case R.id.lnrFollowing:
                if(Integer.valueOf(txtFollowing.getText().toString())<1)
                    return;
                intent.putExtra("search_type", "followings");
                startActivity(intent);
                break;
            case R.id.lnrSubscribers:
                if(Integer.valueOf(txtSubscribers.getText().toString())<1)
                    return;
                intent.putExtra("search_type", "subscribers");
                startActivity(intent);
                break;
            case R.id.lnrSubscription:
                if(Integer.valueOf(txtSubscriptions.getText().toString())<1)
                    return;
                intent.putExtra("search_type", "subscribed_to");
                startActivity(intent);
                break;
            case R.id.btnFollow:
                if (userId.equals(Calculations.GUEST)) {
                    loginPrompt();
                    return;
                }
                if(!Reusable.getNetworkAvailability(this)){
                    Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(btnFollow.getText().equals("FOLLOW")){
                    Calculations calculations = new Calculations(MemberProfileActivity.this);
                    calculations.followMember(btnFollow, myID, userId);
                    profile.setC4_followers(profile.getC4_followers()+1);
                    txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                    btnFollow.setText("FOLLOWING");
                }
                else
                    unfollowPrompt();
                break;
            case R.id.btnSubscribe:
                if (userId.equals(Calculations.GUEST)) {
                    loginPrompt();
                    return;
                }
                Intent intentSub = new Intent(getApplicationContext(), SubscriptionActivity.class);
                intentSub.putExtra(USER_ID, userId);
                startActivity(intentSub);
                break;
        }
    }

    public void startChat(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MemberProfileActivity.this,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(String.format("Do you want to chat with %s?", profile.getA2_username()))
                .setTitle("Start chat")
                .setNegativeButton("No", (dialogInterface, i) -> {
                    //do nothing
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PackageManager pkMgt = getPackageManager();
                    String toNumber = profile.getB1_phone();
                    Uri uri = Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text=");
                    try {
                        Intent whatsApp = new Intent(Intent.ACTION_VIEW);
                        whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        whatsApp.setData(uri);
                        pkMgt.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                        startActivity(whatsApp);
                    }
                    catch (PackageManager.NameNotFoundException e){
                        Toast.makeText(MemberProfileActivity.this, "No WhatApp installed", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void loginPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MemberProfileActivity.this,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(MemberProfileActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .show();
    }

    private void unfollowPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MemberProfileActivity.this,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(String.format("Do you want to unfollow %s?", profile.getA2_username()))
                .setTitle("Unfollow")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Calculations calculations = new Calculations(MemberProfileActivity.this);
                        calculations.unfollowMember(btnFollow, myID, userId);
                        profile.setC4_followers(Math.max(0, profile.getC4_followers()-1));
                        txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                        btnFollow.setText("FOLLOW");
                    }
                })
                .show();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(USER_ID, userId);
        super.onSaveInstanceState(outState);
    }
}