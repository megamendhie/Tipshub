package com.sqube.tipshub;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import adapters.NewsAdapter;
import adapters.PeopleAdapter;
import adapters.PostAdapter;
import utils.CacheHelper;
import utils.NewsFunction;

public class RecommendedFragment extends Fragment {
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    String userId;
    PostAdapter postAdapter;
    FloatingActionButton fapTip, fabNormal;
    FloatingActionMenu fabMenu;
    RecyclerView peopleList, trendingList, newsList;
    private final String TAG = "RecFragment";
    NewsAdapter adapter;

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

        newsList = rootView.findViewById(R.id.newsList);
        newsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        String[] testString = {"A", "B", "C", "E", "F", "G"};
        PeopleAdapter adapter = new PeopleAdapter(testString);
        peopleList.setAdapter(adapter);

        loadPost();
        loadNews();
        return rootView;
    }


    private void loadPost() {
        long stopTime = new Date().getTime() - (48*60*60*1000);
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .orderBy("relevance", Query.Direction.DESCENDING).limit(10);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        trendingList.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }

    public void loadNews(){
        DownloadNews newsTask = new DownloadNews();
        boolean networkAvailabile = ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo() != null? true: false;

        if(!networkAvailabile)
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        newsTask.execute();
    }

    class DownloadNews extends AsyncTask<String, Void, String> {
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
            xml = NewsFunction.excuteGet("https://newsapi.org/v2/everything?domains=espnfc.com&language=en&pageSize=10&apiKey="+myAPI_Key, urlParameters);
            return  xml;
        }
        @Override
        protected void onPostExecute(String xml) {
            if(xml==null){
                return;
            }

            if(xml.length()>10){ // Just checking if not empty
                dataList.clear();

                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArray = jsonResponse.optJSONArray("articles");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_AUTHOR, jsonObject.optString(KEY_AUTHOR));
                        map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE));
                        map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION));
                        map.put(KEY_URL, jsonObject.optString(KEY_URL).toString());
                        map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE));
                        long date = jsonObject.optLong(KEY_PUBLISHEDAT);
                        String newsDate = DateFormat.format("dd MMM", date).toString();
                        //map.put(KEY_PUBLISHEDAT, newsDate);
                        map.put(KEY_PUBLISHEDAT, jsonObject.optString(KEY_PUBLISHEDAT).toString());

                        dataList.add(map);
                    }

                    //Delete previous cache and cache new info for later use
                    File cache = new File(getContext() + "/TipshubNews.srl");
                    cache.delete();
                    CacheHelper.save(getContext(), "TipshubNews", xml);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                }

                adapter = new NewsAdapter(getActivity(), dataList);
                newsList.setLayoutManager(new LinearLayoutManager(getContext()));
                newsList.setAdapter(adapter);

            }else{
                Toast.makeText(getContext(), "No news found", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
