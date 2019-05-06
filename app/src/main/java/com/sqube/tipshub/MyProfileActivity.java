package com.sqube.tipshub;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

public class MyProfileActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseUser user;
    private String userId, username;
    private FirebaseFirestore database;
    private CircleImageView imgDp;
    private TextView txtName, txtUsername, txtBio;
    private TextView txtFollowers, txtFollowing, txtSubscribers, txtSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        imgDp = findViewById(R.id.imgDp);
        txtName = findViewById(R.id.txtFullName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        txtSubscribers = findViewById(R.id.txtSubscribers);
        txtSubscriptions = findViewById(R.id.txtSubscribing);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
        database = FirebaseFirestore.getInstance();
        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                ProfileShort profile = documentSnapshot.toObject(ProfileShort.class);
                txtName.setText(profile.getA0_firstName()+" "+ profile.getA1_lastName());
                txtUsername.setText("@"+profile.getA2_username());
                txtBio.setText(profile.getA5_bio());
                txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                txtFollowing.setText(String.valueOf(profile.getC5_following()));
                txtSubscribers.setText(String.valueOf(profile.getC6_subscribers()));
                txtSubscriptions.setText(String.valueOf(profile.getC7_subscriptions()));
                for(int i=1; i<=6; i++){
                    /*
                    long =

                    //values for No Of Games, Won Games, and Won Games Percentage for 3-5 odds
                    e1a_NOG;
                    e1b_WG;
                    e1c_WGP;

                    //values for No Of Games, Won Games, and Won Games Percentage for 6-10 odds
                    e2a_NOG;
                    e2b_WG;
                    e2c_WGP;

                    //values for No Of Games, Won Games, and Won Games Percentage for 11-50 odds
                    e3a_NOG;
                    e3b_WG;
                    e3c_WGP;

                    //values for No Of Games, Won Games, and Won Games Percentage for 50+ odds
                    e4a_NOG;
                    e4b_WG;
                    e4c_WGP;

                    //values for No Of Games, Won Games, and Won Games Percentage for draws
                    e5a_NOG;
                    e5b_WG;
                    e5c_WGP;

                    //values for No Of Games, Won Games, and Won Games Percentage for banker
                    e6a_NOG;
                    e6b_WG;
                    e6c_WGP;
                    */
                }
            }
        });
        setupViewPager(viewPager); //set up view pager with fragments


    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PostFragment(), "Posts");
        adapter.addFragment(new BankersFragment(), "Bankers");
        adapter.addFragment(new ReviewFragment(), "Review");
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
}
