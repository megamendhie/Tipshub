package com.sqube.tipshub;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

import adapters.PostAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankerFragment extends Fragment {
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    String userId;
    PostAdapter subAdapter, latestAdapter, winAdapter;
    com.github.clans.fab.FloatingActionButton fapTip, fabNormal;
    FloatingActionMenu fabMenu;
    RecyclerView subscribedList, latestList, winningsList;
    private final String TAG = "RecFragment";

    public BankerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_banker, container, false);
        subscribedList = rootView.findViewById(R.id.subscribedList);
        latestList = rootView.findViewById(R.id.latestList);
        winningsList = rootView.findViewById(R.id.winningsList);

        subscribedList.setLayoutManager(new LinearLayoutManager(getActivity()));
        latestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        winningsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        long stopTime = new Date().getTime() - (48*60*60*1000);
        query = database.collection("posts").orderBy("time").whereGreaterThanOrEqualTo("time", stopTime)
                .orderBy("type").whereEqualTo("type", 0);
        loadSub();
        loadLatest();
        loadWinning();
        return rootView;
    }

    private void loadSub() {
    }

    private void loadLatest() {
        Log.i(TAG, "loadPost: ");
        latestAdapter = new PostAdapter(query.orderBy("relevance", Query.Direction.DESCENDING).limit(10),
                userId, getActivity(), getContext());
        latestList.setAdapter(latestAdapter);
        if(latestAdapter!=null)
            latestAdapter.startListening();
    }

    private void loadWinning() {
        Log.i(TAG, "loadPost: ");
        winAdapter = new PostAdapter(query.orderBy("status").whereEqualTo("status", 2), userId, getActivity(), getContext());
        latestList.setAdapter(winAdapter);
        if(winAdapter!=null)
            winAdapter.startListening();
    }

}
