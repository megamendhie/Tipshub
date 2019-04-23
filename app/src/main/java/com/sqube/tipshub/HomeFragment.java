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

public class HomeFragment extends Fragment{
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private final String TAG = "HomeFrag";
    String userId;
    PostAdapter postAdapter;
    FloatingActionButton fapTip, fabNormal;
    FloatingActionMenu fabMenu;
    RecyclerView testList;
    Intent intent;
    public HomeFragment() {
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
        View rootView=inflater.inflate(R.layout.fragment_home, container, false);
        intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
        testList = rootView.findViewById(R.id.testList);
        testList.setLayoutManager(new LinearLayoutManager(getActivity()));
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        fabMenu = rootView.findViewById(R.id.fabMenu);
        fabNormal = rootView.findViewById(R.id.fabNormal);
        fapTip = rootView.findViewById(R.id.fabPost);
        fapTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(false);
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
        loadPost();
        return rootView;
    }

    private void loadPost() {
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time", Query.Direction.DESCENDING);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        testList.setAdapter(postAdapter);
        if(postAdapter!=null)
            postAdapter.startListening();
    }

}
