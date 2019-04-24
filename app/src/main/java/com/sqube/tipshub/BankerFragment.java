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
    PostAdapter postAdapter;
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
        latestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        loadSub();
        loadLatest();
        loadWinning();
        return rootView;
    }

    private void loadSub() {
    }

    private void loadLatest() {
        long stopTime = new Date().getTime() - (48*60*60*1000);
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time").whereGreaterThanOrEqualTo("time", stopTime)
                .orderBy("type").whereEqualTo("type", 0).limit(10);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        latestList.setAdapter(postAdapter);
        if(postAdapter!=null)
            postAdapter.startListening();
    }

    private void loadWinning() {
    }

}
