package fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;
import com.sqube.tipshub.ExtendedHomeActivity;
import com.sqube.tipshub.FullViewActivity;
import com.sqube.tipshub.MainActivity;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import adapters.BankerTipsterAdapter;
import adapters.FilteredPostAdapter;
import adapters.PostAdapter;
import adapters.TipsAdapter;
import adapters.WebsiteAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import models.GameTip;
import models.Post;
import models.ProfileMedium;
import models.ProfileShort;
import models.SnapId;
import models.UserNetwork;
import models.Website;
import services.DailyNotificationWorker;
import utils.Calculations;
import utils.DatabaseHelper;
import utils.FirebaseUtil;
import utils.HttpConFunction;
import utils.Reusable;

import static android.app.Activity.RESULT_OK;
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
    private ArrayList<Post> trendingPostList = new ArrayList<>();
    private ArrayList<SnapId> trendingSnapIds= new ArrayList<>();
    private String userId, username;
    private PostAdapter postAdapter;
    private FilteredPostAdapter trendingAdapter;
    private FloatingActionMenu fabMenu;
    private RecyclerView homeFeed, tipsFeed, bankerTipstersFeed, siteFeed, trendingFeed;
    private Intent intent;
    //private SwipeRefreshLayout refresher;
    private JSONObject flagsJson;
    private ArrayList<GameTip> homepageTips = new ArrayList<>();
    private TipsAdapter tipsAdapter;
    private CardView crdTips, crdPosts;
    private TextView txtOpenFull;
    private TextView txtError;

    private String json;
    private ProfileMedium myProfile;
    private boolean subscriber;
    private Uri filePath = null;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private CircleImageView imgDp;

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
        homeFeed = rootView.findViewById(R.id.postList);
        tipsFeed = rootView.findViewById(R.id.tipsList);
        bankerTipstersFeed = rootView.findViewById(R.id.bankersList);
        siteFeed = rootView.findViewById(R.id.sportSitesList);
        trendingFeed = rootView.findViewById(R.id.trendingList);

        shimmerLayoutTips = rootView.findViewById(R.id.shimmerTips);
        shimmerLayoutPosts = rootView.findViewById(R.id.shimmerPosts);
        crdTips = rootView.findViewById(R.id.crdTips);
        crdPosts = rootView.findViewById(R.id.crdPosts);

        homeFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        tipsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        trendingFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        bankerTipstersFeed.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        siteFeed.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        //((DefaultItemAnimator) homeFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        //((DefaultItemAnimator) trendingFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        fabMenu = rootView.findViewById(R.id.fabMenu);
        FloatingActionButton fabNormal = rootView.findViewById(R.id.fabNormal);
        FloatingActionButton fabTip = rootView.findViewById(R.id.fabPost);
        //refresher = rootView.findViewById(R.id.refresher);
        //refresher.setColorSchemeResources(R.color.colorPrimary);

        TextView txtOpenFullPost = rootView.findViewById(R.id.txtOpenFullPost);
        txtOpenFullPost.setOnClickListener(view -> seeMore());
        txtOpenFull = rootView.findViewById(R.id.txtOpenFull);
        txtOpenFull.setOnClickListener(view -> getContext().startActivity(new Intent(getContext(), FullViewActivity.class)));

        if(homepageTips.isEmpty())
            shimmerLayoutTips.startShimmer();
        else{
            txtOpenFull.setVisibility(View.VISIBLE);
            shimmerLayoutTips.stopShimmer();
            shimmerLayoutTips.setVisibility(View.GONE);
            crdTips.setVisibility(View.VISIBLE);
        }
        shimmerLayoutPosts.startShimmer();

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

        flagsJson = HttpConFunction.getFlags(getContext().getResources().openRawResource(R.raw.flags));
        selectPostToLoad(savedInstanceState);
        loadTips();
        loadBankerTipsters();
        loadSportSites();
        loadTrendingPost();

        if(myProfile!=null && (myProfile.getA2_username().isEmpty()||myProfile.getB1_phone().isEmpty()))
                promptForUsername();
        return rootView;
    }

    private void seeMore(){
        Intent intent = new Intent(getContext(), ExtendedHomeActivity.class);
        intent.putExtra("fromEverybody", fromEverybody);
        startActivity(intent);
    }

    private void loadSportSites() {
        ArrayList<Website> siteList = new ArrayList<>();
        WebsiteAdapter websiteAdapter = new WebsiteAdapter(siteList);
        siteFeed.setAdapter(websiteAdapter);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("sportSites");
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren())
                    return;
                siteList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Website website = snapshot.getValue(Website.class);
                    siteList.add(website);
                }
                websiteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadBankerTipsters() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("profiles").orderBy("e6c_WGP",
                Query.Direction.DESCENDING).whereEqualTo("c1_banker", true).limit(10);
        FirestoreRecyclerOptions<ProfileShort> options = new FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort.class)
                .build();
        BankerTipsterAdapter bankerTipsterAdapter = new BankerTipsterAdapter(options);
        bankerTipstersFeed.setAdapter(bankerTipsterAdapter);
        bankerTipsterAdapter.startListening();
    }

    private void loadTrendingPost() {
        trendingAdapter = new FilteredPostAdapter(false, userId, getContext(), trendingPostList, trendingSnapIds);
        trendingFeed.setAdapter(trendingAdapter);
        FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("timeRelevance", Query.Direction.DESCENDING).limit(20).get()
                .addOnSuccessListener(result -> {
                    if(result==null|| result.isEmpty())
                        return;
                    trendingSnapIds.clear();
                    trendingPostList.clear();
                    for(DocumentSnapshot snapshot: result.getDocuments()){
                        Post post = snapshot.toObject(Post.class);
                        if(post.getType()==6 && post.getStatus()!=2)
                            continue;
                        trendingPostList.add(post);
                        trendingSnapIds.add(new SnapId(snapshot.getId(), post.getTime()));
                    }
                    trendingAdapter.notifyDataSetChanged();
                });
    }

    private void promptForUsername() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_signup2, null);
        builder.setView(dialogView);
        final androidx.appcompat.app.AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();

        //Initialize variables
        final boolean[] numberValid = {false};
        txtError = dialog.findViewById(R.id.txtError);
        final EditText edtUsername = dialog.findViewById(R.id.edtUsername);
        final EditText edtPhone = dialog.findViewById(R.id.editText_carrierNumber);
        final RadioGroup rdbGroup = dialog.findViewById(R.id.rdbGroupGender);
        final Button btnSave = dialog.findViewById(R.id.btnSave);
        imgDp = dialog.findViewById(R.id.imgDp); imgDp.setOnClickListener(view -> grabImage());
        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(edtPhone);

        if(myProfile.getB2_dpUrl().isEmpty())
            Glide.with(this).load(R.drawable.dummy).into(imgDp);
        else
            Glide.with(this).load(myProfile.getB2_dpUrl()).into(imgDp);

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> numberValid[0] =isValidNumber);

        btnSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String phone = ccp.getFullNumber();
            String country = ccp.getSelectedCountryName();
            String gender ="";
            switch (rdbGroup.getCheckedRadioButtonId()) {
                case R.id.rdbMale:
                    gender = "male";
                    break;
                case R.id.rdbFemale:
                    gender = "female";
                    break;
            }

            //verify fields meet requirement
            if(TextUtils.isEmpty(username)){
                edtUsername.setError("Enter username");
                txtError.setText("Enter username");
                txtError.setVisibility(View.VISIBLE);
                return;
            }
            if(username.length() < 3){
                edtUsername.setError("Username too short");
                txtError.setText("Username too short");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(TextUtils.isEmpty(phone)){
                txtError.setText("Enter phone number");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(!numberValid[0]){
                txtError.setText("Phone number is incorrect");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(TextUtils.isEmpty(gender)){
                txtError.setText("Select gender (M/F)");
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            txtError.setVisibility(View.GONE);

            String finalGender = gender;
            FirebaseUtil.getFirebaseFirestore().collection("profiles")
                    .whereEqualTo("a2_username", username).limit(1).get()
                    .addOnCompleteListener(task -> {
                        if(task.getResult() == null || !task.getResult().isEmpty()){
                            edtUsername.setError("Username already exist");
                            Toast.makeText(getContext(), "Username already exist. Try another one", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //Map new user datails, and ready to save to db
                        Map<String, String> url = new HashMap<>();
                        url.put("a2_username", username);
                        url.put("a4_gender", finalGender);
                        url.put("b0_country", country);
                        url.put("b1_phone", phone);

                        //set the new username to firebase auth user
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        FirebaseUtil.getFirebaseAuthentication().getCurrentUser().updateProfile(profileUpdate);

                        //save username, phone number, and gender to database
                        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).set(url, SetOptions.merge());
                        Reusable.updateAlgoliaIndex(myProfile.getA0_firstName(), myProfile.getA1_lastName(), username, userId, myProfile.getC2_score(), true); //add to Algolia index

                        dialog.cancel();
                    });
        });
    }

    private void grabImage(){
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(getActivity());
    }

    private void uploadImage(){
        txtError.setText("Uploading image...");
        txtError.setVisibility(View.VISIBLE);

        FirebaseUtil.getFirebaseStorage().getReference().child("profile_images").child(userId).putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId).update("b2_dpUrl", url);
                            txtError.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                            txtError.setText("Image uploaded" );
                            imgDp.setImageURI(filePath);
                        }))
                .addOnFailureListener(e -> {
                    txtError.setText("Could not upload image... Try again later");
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    txtError.setText((int) progress + "%" + " completed" );
                })
        ;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                uploadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void loadTips() {
        tipsAdapter = new TipsAdapter(homepageTips);
        tipsFeed.setAdapter(tipsAdapter);
        GetTips getTips = new GetTips();
        getTips.execute();
    }

    private void selectPostToLoad(Bundle savedInstanceState) {
        //refresher.setRefreshing(true);
        if(fromEverybody){
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
        trendingFeed.setAdapter(null);
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

    private void loadPostFbAdapter() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(20);
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
            ArrayList<Post> postList2 = new ArrayList<>();
            ArrayList<SnapId> snapIds2= new ArrayList<>();

            for(Object object: list){
                QuerySnapshot querySnapshot = (QuerySnapshot) object;
                if(querySnapshot !=null || !querySnapshot.isEmpty()){
                    for(DocumentSnapshot snapshot: querySnapshot.getDocuments()){
                        Post post = snapshot.toObject(Post.class);
                        if(post.getType()==6 && post.getStatus()!=2)
                            continue;
                        postList2.add(post);
                        snapIds2.add(new SnapId(snapshot.getId(), post.getTime()));
                    }
                }
            }
            if(postList2.size()>1){
                Collections.sort(postList2);
                Collections.sort(snapIds2);
            }

            FilteredPostAdapter adapter = new FilteredPostAdapter(true, userId, getContext(), postList, snapIds);
            homeFeed.setAdapter(adapter);
            int size = Math.min(20, postList2.size());
            for (int i=0; i< size; i++){
                postList.add(postList2.get(i));
                snapIds.add(snapIds2.get(i));
            }

            int i = postList.size();
            adapter.notifyDataSetChanged();
            postList2.clear(); snapIds2.clear();
            shimmerLayoutPosts.stopShimmer();
            shimmerLayoutPosts.setVisibility(View.GONE);
            crdPosts.setVisibility(View.VISIBLE);
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
            if(subscriber)
                Collections.sort(tips);
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
