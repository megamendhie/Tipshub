package com.sqube.tipshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.PerformanceAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import fragments.BankersFragment;
import fragments.PostFragment;
import fragments.ReviewFragment;
import models.ProfileMedium;

public class MyProfileActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseUser user;
    private String userId, username;
    private FirebaseFirestore database;
    private CircleImageView imgDp;
    ProfileMedium profile;
    private RecyclerView recyclerView;
    PerformanceAdapter adapter;
    Fragment postFragment, bankerFragment, reviewFragment;
    ArrayList<Map<String, Object>> performanceList = new ArrayList<>();
    private TextView txtName, txtUsername, txtBio;
    private TextView txtPost, txtAccuracy;
    private TextView txtFollowers, txtFollowing, txtSubscribers, txtSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        imgDp = findViewById(R.id.imgDp);
        txtName = findViewById(R.id.txtFullName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtPost = findViewById(R.id.txtPost);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        txtSubscribers = findViewById(R.id.txtSubscribers);
        txtSubscriptions = findViewById(R.id.txtSubscribing);
        recyclerView = findViewById(R.id.performanceList);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(MyProfileActivity.this, SettingsActivity.class));
            }
        });
        adapter = new PerformanceAdapter(this, performanceList);
        recyclerView.setLayoutManager(lm);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
        database = FirebaseFirestore.getInstance();

        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                profile = documentSnapshot.toObject(ProfileMedium.class);
                txtName.setText(profile.getA0_firstName()+" "+ profile.getA1_lastName());
                txtUsername.setText("@"+profile.getA2_username());
                actionBar.setTitle(profile.getA0_firstName()+" "+ profile.getA1_lastName());
                txtBio.setText(profile.getA5_bio());
                txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                txtFollowing.setText(String.valueOf(profile.getC5_following()));
                txtSubscribers.setText(String.valueOf(profile.getC6_subscribers()));
                txtSubscriptions.setText(String.valueOf(profile.getC7_subscriptions()));
                txtPost.setText(profile.getE0a_NOG() + " tips  • ");
                txtAccuracy.setText(profile.getE0b_WG()+ " won");

                //set Display picture
                Glide.with(getApplicationContext())
                        .load(profile.getB2_dpUrl())
                        .into(imgDp);

                if(profile.getE0a_NOG()>0){
                    for(int i=1; i<=6; i++){
                        Map<String, Object> row = getRow(i);
                        if(!row.isEmpty())
                            performanceList.add(row);
                    }
                    recyclerView.setAdapter(adapter);
                }
            }
        });
        setupViewPager(viewPager); //set up view pager with fragments
    }

    private  Map<String, Object> getRow(int i) {
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
        bundle.putString("userId", userId);
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

    public class ViewPagerAdapter extends FragmentPagerAdapter{
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
}
