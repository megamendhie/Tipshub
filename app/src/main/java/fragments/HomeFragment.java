package fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.sqube.tipshub.FullViewActivity;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.FilteredPostAdapter;
import adapters.PosidonAdapter;
import adapters.PostAdapter;
import adapters.TipsAdapter;
import models.GameTip;
import models.Post;
import models.ProfileMedium;
import models.SnapId;
import models.UserNetwork;
import utils.CacheHelper;
import utils.Calculations;
import utils.DatabaseHelper;
import utils.FirebaseUtil;
import utils.HttpConFunction;

import static utils.Calculations.CLASSIC;
import static utils.Calculations.targetUrl;

public class HomeFragment extends Fragment{
    private String TAG = "HomeFrag", HOME_FEED_STATE = "homeFeedState";
    private ShimmerFrameLayout shimmerLayoutTips;
    private ShimmerFrameLayout shimmerLayoutPosts;
    private Gson gson = new Gson();
    private SharedPreferences prefs;
    private boolean fromEverybody = true;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<SnapId> snapIds= new ArrayList<>();
    private String userId, username;
    private PostAdapter postAdapter;
    private FilteredPostAdapter fAdapter;
    private PosidonAdapter posidonAdapter;
    private FloatingActionMenu fabMenu;
    private RecyclerView homeFeed, tipsFeed;
    private Intent intent;
    //private SwipeRefreshLayout refresher;
    private JSONObject flagsJson;
    private ArrayList<GameTip> homepageTips = new ArrayList<>();
    private TipsAdapter tipsAdapter;
    private CardView crdTips, crdPosts;
    private TextView txtOpenFull;

    private String json;
    private ProfileMedium myProfile;
    private boolean subscriber;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        json = prefs.getString("profile", "");
        myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        subscriber = myProfile != null && myProfile.isD4_vipSubscriber();

        dbHelper = new DatabaseHelper(getContext());
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_home, container, false);
        intent = new Intent(getContext(), PostActivity.class);
        homeFeed = rootView.findViewById(R.id.testList);
        tipsFeed = rootView.findViewById(R.id.tipsList);
        shimmerLayoutTips = rootView.findViewById(R.id.shimmerTips);
        shimmerLayoutPosts = rootView.findViewById(R.id.shimmerPosts);
        crdTips = rootView.findViewById(R.id.crdTips);
        crdPosts = rootView.findViewById(R.id.crdPosts);
        homeFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        tipsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        ((DefaultItemAnimator) homeFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        fabMenu = rootView.findViewById(R.id.fabMenu);
        FloatingActionButton fabNormal = rootView.findViewById(R.id.fabNormal);
        FloatingActionButton fabTip = rootView.findViewById(R.id.fabPost);
        //refresher = rootView.findViewById(R.id.refresher);
        //refresher.setColorSchemeResources(R.color.colorPrimary);

        txtOpenFull = rootView.findViewById(R.id.txtOpenFull);
        txtOpenFull.setOnClickListener(view ->
                getContext().startActivity(new Intent(getContext(), FullViewActivity.class)));
        if(homepageTips.isEmpty())
            shimmerLayoutTips.startShimmer();
        else{
            txtOpenFull.setVisibility(View.VISIBLE);
            shimmerLayoutTips.stopShimmer();
            shimmerLayoutTips.setVisibility(View.GONE);
            crdTips.setVisibility(View.VISIBLE);
        }
        shimmerLayoutPosts.startShimmer();
        /*
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                selectPostToLoad(savedInstanceState);
            }
        });
        */

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

        //confirm if user is seeing everybody's post
        fromEverybody = prefs.getBoolean("fromEverybody", true);

        fAdapter = new FilteredPostAdapter(true, userId, getContext(), postList, snapIds);
        posidonAdapter = new PosidonAdapter(true, userId, getContext(), postList, snapIds);
        homeFeed.setAdapter(fAdapter);
        flagsJson = HttpConFunction.getFlags(getContext().getResources().openRawResource(R.raw.flags));
        selectPostToLoad(savedInstanceState);
        loadTips();
        Log.i(TAG, "onCreateView: ");
        return rootView;
    }

    private void loadTips() {
        tipsAdapter = new TipsAdapter(homepageTips, subscriber);
        tipsFeed.setAdapter(tipsAdapter);
        GetTips getTips = new GetTips();
        getTips.execute();
    }

    private void selectPostToLoad(Bundle savedInstanceState) {
        //refresher.setRefreshing(true);
        if(fromEverybody){
            homeFeed.setAdapter(posidonAdapter);
            //loadPost();
            loadPostFbAdapter();
        }
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

    private void loadPost() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING);

        query.limit(200).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.isSuccessful())
                    return;

                postList.clear();
                snapIds.clear();
                QuerySnapshot snapshots = task.getResult();
                for(DocumentSnapshot snapshot: snapshots.getDocuments()){
                    Post post = snapshot.toObject(Post.class);
                    if(post.getType()==6 && post.getStatus()!=2)
                        continue;
                    postList.add(post);
                    snapIds.add(new SnapId(snapshot.getId(), post.getTime()));
                }
                posidonAdapter.notifyDataSetChanged();
                shimmerLayoutPosts.stopShimmer();
                shimmerLayoutPosts.setVisibility(View.GONE);
                crdPosts.setVisibility(View.VISIBLE);
            }
        });

        //refresher.setRefreshing(false);
    }

    private void loadPostFbAdapter() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(40);
        FirestoreRecyclerOptions<Post> response = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        postAdapter = new PostAdapter(response, userId, getContext());
        homeFeed.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
            shimmerLayoutPosts.stopShimmer();
            shimmerLayoutPosts.setVisibility(View.GONE);
            crdPosts.setVisibility(View.VISIBLE);
        }
        //refresher.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        json = prefs.getString("profile", "");
        myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        boolean subscriber = myProfile != null && myProfile.isD4_vipSubscriber();
        if(subscriber && !this.subscriber ){
            this.subscriber = true;
            loadTips();
        }
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
        //refresher.setRefreshing(false);
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
                shimmerLayoutPosts.stopShimmer();
                shimmerLayoutPosts.setVisibility(View.GONE);
                crdPosts.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Parcelable homeFeedState = homeFeed.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(HOME_FEED_STATE, homeFeedState);
        super.onSaveInstanceState(outState);
    }

    private class GetTips extends AsyncTask<String, Void, ArrayList<GameTip>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String xml = dbHelper.getTip(db, CLASSIC);
            if(xml!=null && !xml.isEmpty())
                onPostExecute(getTips(xml));
        }

        @Override
        protected ArrayList<GameTip> doInBackground(String... strings) {
            final Date today = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String todayDate = sdf.format(today.getTime());

            HttpConFunction httpConnection = new HttpConFunction();
            String s = httpConnection.executeGet(targetUrl+ "iso_date="+todayDate, "HOME");

            if(s!=null && s.length() >= 10)
                dbHelper.updateTip(db, CLASSIC, s);
            return getTips(s);
        }

        private ArrayList<GameTip> getTips(String s) {
            ArrayList<GameTip> tips = new ArrayList<>();
            if(s==null)
                return tips;

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray data = jsonObject.getJSONArray("data");

                for(int i=0; i < data.length(); i++){
                    JSONObject tipJSON = data.getJSONObject(i);
                    GameTip gameTip = new GameTip();
                    gameTip.set_id(tipJSON.optString("id"));
                    gameTip.setAwayTeam(tipJSON.optString("away_team"));
                    gameTip.setHomeTeam(tipJSON.optString("home_team"));
                    String region = tipJSON.optString("competition_cluster");
                    gameTip.setRegion(flagsJson==null? region : flagsJson.optString(region.trim())+" "+region);
                    gameTip.setLeague(tipJSON.optString("competition_name"));
                    gameTip.setPrediction(tipJSON.optString("prediction"));
                    gameTip.setTime(tipJSON.optString("start_date"));
                    gameTip.setResult(tipJSON.optString("result"));
                    gameTip.setStatus(tipJSON.optString("status"));

                    if(tipJSON.has("probabilities")){
                        JSONObject probabilities = tipJSON.optJSONObject("probabilities");
                        gameTip.setProbability(probabilities.optDouble(gameTip.getPrediction()));
                        Log.i(TAG, "getTips: "+ probabilities.optDouble(gameTip.getPrediction()));
                    }
                    else
                        Log.i(TAG, "getTips: null");

                    JSONObject oddJSON = tipJSON.getJSONObject("odds");
                    if(oddJSON != null){
                        gameTip.setOdd(oddJSON.optDouble(gameTip.getPrediction()));
                    }
                    tips.add(gameTip);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return tips;
        }

        @Override
        protected void onPostExecute(ArrayList<GameTip> tips) {
            Log.i("GETTIPS", "onPostExecute: "+ tips);
            if(tips.isEmpty())
                return;
            homepageTips.clear();
            Calculations.setFreeGameTips(tips); //save all the tips
            int k = 0;
            for(GameTip tip: tips){
                homepageTips.add(tip);
                k++;
                if (k>=3) break;
            }
            tipsAdapter.notifyDataSetChanged();
            txtOpenFull.setVisibility(View.VISIBLE);
            shimmerLayoutTips.stopShimmer();
            shimmerLayoutTips.setVisibility(View.GONE);
            crdTips.setVisibility(View.VISIBLE);

        }
    }
}
