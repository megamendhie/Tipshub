package com.sqube.tipshub;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.MenuItem;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import adapters.FilteredPostAdapter;
import adapters.PostAdapter;
import models.Post;
import models.ProfileMedium;
import models.SnapId;
import models.UserNetwork;
import utils.FirebaseUtil;

public class ExtendedHomeActivity extends AppCompatActivity {
    private RecyclerView homeFeed;
    private Gson gson = new Gson();
    private SharedPreferences prefs;
    private boolean fromEverybody = true;
    private String userId, username;
    private String json;
    private ProfileMedium myProfile;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<SnapId> snapIds= new ArrayList<>();

    private PostAdapter postAdapter;
    private FilteredPostAdapter fAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        homeFeed = findViewById(R.id.postList);
        homeFeed.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionMenu fabMenu = findViewById(R.id.fabMenu);
        FloatingActionButton fabNormal = findViewById(R.id.fabNormal);
        FloatingActionButton fabTip = findViewById(R.id.fabPost);

        Intent intent  = new Intent(ExtendedHomeActivity.this, PostActivity.class);
        fromEverybody = getIntent().getBooleanExtra("fromEverybody", false);
        fAdapter = new FilteredPostAdapter(true, userId, this, postList, snapIds);
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user!=null) {
            userId = user.getUid();
            username = user.getDisplayName();
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        json = prefs.getString("profile", "");
        myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);

        fabTip.setOnClickListener(v -> {
            fabMenu.close(false);
            if(hasReachedMax()){
                popUp();
                return;
            }
            intent.putExtra("type", "tip");
            startActivity(intent);
        });

        fabNormal.setOnClickListener(v -> {
            fabMenu.close(false);
            intent.putExtra("type", "normal");
            startActivity(intent);
        });

        selectPostToLoad();
    }

    private void selectPostToLoad() {
        if(fromEverybody){
            loadPostFbAdapter();
        }
        else{
            homeFeed.setAdapter(fAdapter);
            loadMerged();
        }
    }

    private void loadPostFbAdapter() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(80);
        FirestoreRecyclerOptions<Post> response = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        postAdapter = new PostAdapter(response, userId, ExtendedHomeActivity.this, false);
        homeFeed.setAdapter(postAdapter);
        if(postAdapter!=null){
            postAdapter.startListening();
            //shimmerLayoutPosts.stopShimmer();
            //shimmerLayoutPosts.setVisibility(View.GONE);
        }
    }

    private void loadMerged(){
        if(postAdapter!=null)
            postAdapter.stopListening();
        if(UserNetwork.getFollowing()==null){
            FirebaseUtil.getFirebaseFirestore().collection("followings").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult().contains("list"))
                            loadList((ArrayList<String>) task.getResult().get("list"));
                        else
                            loadList(null);
                    });
        }
        else
            loadList(UserNetwork.getFollowing());
    }

    private void loadList(ArrayList<String> ids){
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(userId);

        //check if following list has data
        if(ids != null && !ids.isEmpty()){
            userIds.addAll(ids);
        }
        int count = userIds.size();

        //create task and query for each followed id
        Query[] queries = new Query[count];
        Task[] tasks = new Task[count];

        for(int i = 0; i < count; i++){
            queries[i] = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds.get(i)).limit(10);
            tasks[i] = queries[i].get();
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            postList.clear();
            snapIds.clear();
            for(Object object: list){
                QuerySnapshot querySnapshot = (QuerySnapshot) object;
                if(querySnapshot !=null || !querySnapshot.isEmpty()){
                    for(DocumentSnapshot snapshot: querySnapshot.getDocuments()){
                        Post post = snapshot.toObject(Post.class);
                        if(post.getType()==6 && post.getStatus()!=2)
                            continue;
                        postList.add(post);
                        snapIds.add(new SnapId(snapshot.getId(), post.getTime()));
                    }
                }
            }
            if(postList.size()>1){
                Collections.sort(postList);
                Collections.sort(snapIds);
            }
            fAdapter.notifyDataSetChanged();
            //shimmerLayoutPosts.stopShimmer();
            //shimmerLayoutPosts.setVisibility(View.GONE);
        });

    }

    private void popUp(){
        String message = "<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Tips limit reached</strong></span></p>\n" +
                "<p>Take it easy, "+username+". You have reached your tips limit for today.</p>\n" +
                "<p>To prevent spam, each person can post tips only 4 times in a day.\n"+
                "But there is no limit to normal post. Enjoy!</p>";
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ExtendedHomeActivity.this, R.style.CustomMaterialAlertDialog);
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", (dialogInterface, i) -> {
                    //do nothing
                })
                .show();
    }

    //method checks if user has reached max post for the day
    private boolean hasReachedMax(){
        json = prefs.getString("profile", "");
        myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        if(myProfile ==null)
            return true;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentTime = sdf.format(new Date().getTime());
        String lastPostTime = sdf.format(new Date(myProfile.getC8_lsPostTime()));

        try {
            Date currentDate = sdf.parse(currentTime);
            Date lastPostDate = sdf.parse(lastPostTime);
            if(currentDate.after(lastPostDate))
                return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return myProfile.getC9_todayPostCount() >= 4;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
