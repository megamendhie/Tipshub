package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

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
import utils.FirebaseUtil;
import utils.Reusable;

import static utils.Reusable.getPlaceholderImage;

public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private ActionBar actionBar;
    private String userId, imgUrl;
    private CircleImageView imgDp;
    private LinearLayout[] lnrLayout = new LinearLayout[4];
    ProfileMedium profile;
    private RecyclerView recyclerView;
    Fragment postFragment, bankerFragment, reviewFragment;
    ArrayList<Map<String, Object>> performanceList = new ArrayList<>();
    private TextView txtName, txtUsername, txtBio;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtFollowers, txtFollowing, txtSubscribers, txtSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        imgDp = findViewById(R.id.imgDp);
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
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        TextView btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> startActivity(new Intent(MyProfileActivity.this, SettingsActivity.class)));
        recyclerView.setLayoutManager(lm);
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
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

    private void showDp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView;
        dialogView = inflater.inflate(R.layout.image_viewer, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();
        ImageView imgProfile = dialog.findViewById(R.id.imgDp);
        //set Display picture
        Glide.with(getApplicationContext()).load(profile.getB2_dpUrl())
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(userId.charAt(0)))
                .into(imgProfile);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot==null||!documentSnapshot.exists())
                    return;
                profile = documentSnapshot.toObject(ProfileMedium.class);
                imgUrl = profile.getB2_dpUrl();
                if(!imgUrl.isEmpty())
                    imgDp.setOnClickListener(MyProfileActivity.this);
                String name = String.format(Locale.getDefault(),"%s %s", profile.getA0_firstName(),profile.getA1_lastName());
                actionBar.setTitle(name);
                txtName.setText(name);
                txtUsername.setText(String.format(Locale.getDefault(),"@%s",profile.getA2_username()));
                txtBio.setText(profile.getA5_bio());
                Reusable.applyLinkfy(MyProfileActivity.this, profile.getA5_bio(), txtBio);
                txtFollowers.setText(String.valueOf(profile.getC4_followers()));
                txtFollowing.setText(String.valueOf(profile.getC5_following()));
                txtSubscribers.setText(String.valueOf(profile.getC6_subscribers()));
                txtSubscriptions.setText(String.valueOf(profile.getC7_subscriptions()));
                String tips = profile.getE0a_NOG()>1? "tips": "tip";
                txtPost.setText(String.format(Locale.getDefault(),"%d  %s  • ", profile.getE0a_NOG(), tips));
                txtWon.setText(String.format(Locale.getDefault(),"%d  won  • ", profile.getE0b_WG()));
                txtAccuracy.setText(String.format(Locale.getDefault(),"%.1f%%", (double) profile.getE0c_WGP()));

                //set Display picture
                Glide.with(getApplicationContext()).load(profile.getB2_dpUrl())
                        .placeholder(R.drawable.dummy)
                        .error(getPlaceholderImage(userId.charAt(0)))
                        .into(imgDp);

                if(!performanceList.isEmpty())
                    return;
                if(profile.getE0a_NOG()>0){
                    for(int i=1; i<=6; i++){
                        Map<String, Object> row = getRow(i);
                        if(!row.isEmpty())
                            performanceList.add(row);
                    }
                    recyclerView.setAdapter(new PerformanceAdapter(performanceList));
                }
            }
        });
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
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
