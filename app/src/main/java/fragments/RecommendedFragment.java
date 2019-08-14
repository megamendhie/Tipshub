package fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import adapters.FilteredPostAdapter;
import adapters.NewsAdapter;
import adapters.PeopleRecAdapter;
import models.Post;
import models.SnapId;
import models.UserNetwork;
import utils.CacheHelper;
import utils.FirebaseUtil;
import utils.NewsFunction;
import utils.Reusable;

public class RecommendedFragment extends Fragment {
    private String userId;
    Timer timer = new Timer();
    private RecyclerView peopleList, trendingList, newsList;
    private final String TAG = "RecFragment";
    private boolean alreadyLoaded;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<SnapId> snapIds= new ArrayList<>();
    FilteredPostAdapter fAdapter;

    public final String myAPI_Key = "417444c0502047d69c1c2a9dcc1672cd";
    public final String KEY_AUTHOR = "author";
    public final String KEY_TITLE = "title";
    public final String KEY_DESCRIPTION = "description";
    public final String KEY_URL = "url";
    public final String KEY_URLTOIMAGE = "urlToImage";
    public final String KEY_PUBLISHEDAT = "publishedAt";
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

    public RecommendedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_recommended, container, false);
        peopleList = rootView.findViewById(R.id.peopleList);
        peopleList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        trendingList = rootView.findViewById(R.id.trendingList);
        trendingList.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((DefaultItemAnimator) trendingList.getItemAnimator()).setSupportsChangeAnimations(false);

        newsList = rootView.findViewById(R.id.newsList);
        newsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        fAdapter = new FilteredPostAdapter(false, userId, getActivity(), getContext(), postList, snapIds);
        trendingList.setAdapter(fAdapter);
        alreadyLoaded = false;
        loadNews();
        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        trendingList.setAdapter(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(alreadyLoaded)
            return;
        loadPeople();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadPost();
            }
        }, 0, 300000);
        alreadyLoaded=true;
    }

    private void loadPeople() {
        CollectionReference recReference = FirebaseUtil.getFirebaseFirestore().collection("recommended").document(userId)
        .collection("rec");

        recReference.orderBy("count", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() == null || task.getResult().isEmpty())
                    loadPeopleFromProfile();
                if (task.getResult().getDocuments().size() > 6) {
                    ArrayList<String> list = new ArrayList<>();
                    for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                        list.add(snapshot.getId());
                    }
                    Collections.shuffle(list);
                    peopleList.setAdapter(new PeopleRecAdapter(getActivity(), getContext(), userId, list));
                } else
                    loadPeopleFromProfile();
            }
        });
    }

    private void loadPeopleFromProfile(){
        FirebaseUtil.getFirebaseFirestore().collection("profiles").orderBy("c2_score",
                Query.Direction.DESCENDING).limit(30).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult()!=null && !task.getResult().isEmpty()){
                    ArrayList<String> list = new ArrayList<>();
                    int c=0;
                    for (DocumentSnapshot snapshot: task.getResult().getDocuments()){
                        String ref = snapshot.getId();
                        if(ref.equals(userId) || UserNetwork.getFollowing().contains(ref))
                            continue;
                        if(c>=12)
                            return;
                        list.add(ref);
                        c++;
                    }
                    Collections.shuffle(list);
                    peopleList.setAdapter( new PeopleRecAdapter(getActivity(), getContext(), userId, list));
                }
            }
        });

    }

    private void loadPost() {
        Log.i(TAG, "loadPost: ");

        FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("timeRelevance", Query.Direction.DESCENDING).limit(15).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot result) {
                        if(result==null|| result.isEmpty())
                            return;
                        snapIds.clear();
                        postList.clear();
                        for(DocumentSnapshot snapshot: result.getDocuments()){
                            Post post = snapshot.toObject(Post.class);
                            if(post.getType()==6 && post.getStatus()!=2)
                                continue;
                            postList.add(post);
                            snapIds.add(new SnapId(snapshot.getId(), post.getTime()));
                        }
                        fAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void loadNews(){
        DownloadNews newsTask = new DownloadNews();
        if(!Reusable.getNetworkAvailability(getActivity()))
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        newsTask.execute();
    }

    private class DownloadNews extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String xml = CacheHelper.retrieve(getContext(),"TipshubNews");
            if(xml==null||xml.equals("")){}
            else{
                onPostExecute(xml);
            }

        }
        protected String doInBackground(String... args) {
            String xml = "";

            String urlParameters = "";
            xml = NewsFunction.excuteGet("https://newsapi.org/v2/everything?domains=espnfc.com&language=en&pageSize=15&apiKey="+myAPI_Key, urlParameters);
            return  xml;
        }
        @Override
        protected void onPostExecute(String xml) {
            if(xml==null||xml.isEmpty()){
                return;
            }

            if(xml.length()>10){ // Just checking if not empty
                dataList.clear();

                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArray = jsonResponse.optJSONArray("articles");
                    SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    oldFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    SimpleDateFormat newFormatter = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
                    newFormatter.setTimeZone(TimeZone.getDefault());
                    Date dt;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_AUTHOR, jsonObject.optString(KEY_AUTHOR));
                        map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE));
                        map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION));
                        map.put(KEY_URL, jsonObject.optString(KEY_URL).toString());
                        map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE));
                        //long date = jsonObject.optLong(KEY_PUBLISHEDAT);
                        dt = oldFormat.parse(jsonObject.optString(KEY_PUBLISHEDAT));
                        String newsDate = newFormatter.format(dt);
                        map.put(KEY_PUBLISHEDAT, newsDate);
                        //map.put(KEY_PUBLISHEDAT, jsonObject.optString(KEY_PUBLISHEDAT).toString());

                        dataList.add(map);
                    }

                    //Delete previous cache and cache new info for later use
                    File cache = new File(getContext() + "/TipshubNews.srl");
                    cache.delete();
                    CacheHelper.save(getContext(), "TipshubNews", xml);
                } catch (JSONException | ParseException e) {
                    Log.i(TAG, "onPostExecute: "+ e.getMessage());
                    Toast.makeText(getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                }

                NewsAdapter adapter = new NewsAdapter(getActivity(), dataList);
                newsList.setLayoutManager(new LinearLayoutManager(getContext()));
                newsList.setAdapter(adapter);

            }else{
                Toast.makeText(getContext(), "No news found", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
