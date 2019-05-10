package com.sqube.tipshub;

import android.content.Intent;
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

import adapters.PostAdapter;

public class PostFragment extends Fragment {
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private final String TAG = "PostFragment";
    String userId, myId, myUsername;
    PostAdapter postAdapter;
    FloatingActionButton fapTip, fabNormal;
    FloatingActionMenu fabMenu;
    RecyclerView recyclerView;
    Intent intent;

    public PostFragment() {
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
        View rootView  = inflater.inflate(R.layout.fragment_post, container, false);
        recyclerView = rootView.findViewById(R.id.postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        myId = user.getUid();
        myUsername = user.getDisplayName();
        userId = getArguments().getString("userId");
        loadPost();
        return rootView;

    }
    private void loadPost() {
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", userId);
        postAdapter = new PostAdapter(query, myId, getActivity(), getContext());
        recyclerView.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }
}
