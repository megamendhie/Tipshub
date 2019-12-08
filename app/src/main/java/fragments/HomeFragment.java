package fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.FilteredPostAdapter;
import adapters.PostAdapter;
import models.Post;
import models.ProfileMedium;
import models.SnapId;
import models.UserNetwork;
import utils.FirebaseUtil;

public class HomeFragment extends Fragment{
    private String TAG = "HomeFrag", HOME_FEED_STATE = "homeFeedState";
    private ShimmerFrameLayout shimmerLayout;
    private Gson gson = new Gson();
    private SharedPreferences prefs;
    private boolean fromEverybody = true;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<SnapId> snapIds= new ArrayList<>();
    private String userId, username;
    private PostAdapter postAdapter;
    private FilteredPostAdapter fAdapter;
    private FloatingActionMenu fabMenu;
    private RecyclerView homeFeed;
    private Intent intent;
    private SwipeRefreshLayout refresher;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_home, container, false);
        intent = new Intent(getContext(), PostActivity.class);
        homeFeed = rootView.findViewById(R.id.testList);
        shimmerLayout = rootView.findViewById(R.id.shimmer);
        homeFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((DefaultItemAnimator) homeFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        fabMenu = rootView.findViewById(R.id.fabMenu);
        FloatingActionButton fabNormal = rootView.findViewById(R.id.fabNormal);
        FloatingActionButton fabTip = rootView.findViewById(R.id.fabPost);
        refresher = rootView.findViewById(R.id.refresher);
        refresher.setColorSchemeResources(R.color.colorPrimary);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        shimmerLayout.startShimmer();

        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                selectPostToLoad(savedInstanceState);
            }
        });

        fabTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(false);
                if(hasReachedMax()){
                    popUp();
                    return;
                }
                intent.putExtra("type", "tip");
                startActivity(intent);
            }
        });

        fabNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(false);
                intent.putExtra("type", "normal");
                startActivity(intent);
            }
        });

        //confirm if user is seeing everybody's post
        fromEverybody = prefs.getBoolean("fromEverybody", true);

        fAdapter = new FilteredPostAdapter(true, userId, getActivity(), getContext(), postList, snapIds);
        homeFeed.setAdapter(fAdapter);
        selectPostToLoad(savedInstanceState);
        Log.i(TAG, "onCreateView: ");
        return rootView;
    }

    private void selectPostToLoad(Bundle savedInstanceState) {
        refresher.setRefreshing(true);
        if(fromEverybody)
            loadPost();
        else{
            loadMerged();
        }
        if(savedInstanceState!=null){
            Parcelable homeFeedState = savedInstanceState.getParcelable(HOME_FEED_STATE);
            homeFeed.getLayoutManager().onRestoreInstanceState(homeFeedState);
        }
        else {
            LinearLayoutManager layoutManager = (LinearLayoutManager) homeFeed.getLayoutManager();
            layoutManager.smoothScrollToPosition(homeFeed, null, 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        homeFeed.setAdapter(null);
        Log.i(TAG, "onDestroyView: ");
    }

    private void popUp(){
        String message = "<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Tips limit reached</strong></span></p>\n" +
                "<p>Take it easy, "+username+". You have reached your tips limit for today.</p>\n" +
                "<p>To prevent spam, each person can post tips only 4 times in a day.\n"+
                "But there is no limit to normal post. Enjoy!</p>";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .show();
    }

    //method checks if user has reached max post for the day
    private boolean hasReachedMax(){
        String json = prefs.getString("profile", "");
        ProfileMedium myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
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

    private void loadPost() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        homeFeed.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
        }
        refresher.setRefreshing(false);
    }

    private void loadMerged(){
        if(postAdapter!=null)
            postAdapter.stopListening();
        if(UserNetwork.getFollowing()==null){
            FirebaseUtil.getFirebaseFirestore().collection("followings").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().contains("list"))
                        loadList((ArrayList<String>) task.getResult().get("list"));
                    else
                        loadList(null);
                }
            });
        }
        else
            loadList(UserNetwork.getFollowing());
        refresher.setRefreshing(false);
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

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> list) {
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
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Parcelable homeFeedState = homeFeed.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(HOME_FEED_STATE, homeFeedState);
        super.onSaveInstanceState(outState);
    }
}
