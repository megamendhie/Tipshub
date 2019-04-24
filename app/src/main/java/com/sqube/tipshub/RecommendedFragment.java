package com.sqube.tipshub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

import adapters.PeopleAdapter;
import adapters.PostAdapter;

public class RecommendedFragment extends Fragment {
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    String userId;
    PostAdapter postAdapter;
    FloatingActionButton fapTip, fabNormal;
    FloatingActionMenu fabMenu;
    RecyclerView peopleList, trendingList;
    private final String TAG = "RecFragment";

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
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        String[] testString = {"A", "B", "C", "E", "F", "G"};
        PeopleAdapter adapter = new PeopleAdapter(testString);
        peopleList.setAdapter(adapter);

        loadPost();
        return rootView;
    }

    private void loadPost() {
        long stopTime = new Date().getTime() - (48*60*60*1000);
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time").whereGreaterThanOrEqualTo("time", stopTime)
                .orderBy("relevance", Query.Direction.DESCENDING).limit(10);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        trendingList.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }

}
