package fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import java.util.Date;

import adapters.BankerAdapter;
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
    PostAdapter subAdapter;
    BankerAdapter latestAdapter, winAdapter;
    FloatingActionButton fabPost;
    RecyclerView subscribedList, latestList, winningsList;
    private final String TAG = "RecFragment";
    Intent intent;

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
        fabPost = rootView.findViewById(R.id.fabPost);
        ((DefaultItemAnimator) subscribedList.getItemAnimator()).setSupportsChangeAnimations(false);
        ((DefaultItemAnimator) winningsList.getItemAnimator()).setSupportsChangeAnimations(false);
        ((DefaultItemAnimator) latestList.getItemAnimator()).setSupportsChangeAnimations(false);

        subscribedList.setLayoutManager(new LinearLayoutManager(getActivity()));
        latestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        winningsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("type", "banker");
                startActivity(intent);
            }
        });
        long stopTime = new Date().getTime() - (48*60*60*1000);
        //loadSub();
        loadLatest();
        loadWinning();
        return rootView;
    }

    private void loadSub() {
        /*
        Log.i(TAG, "loadPost: ");
        winAdapter = new PostAdapter(query.limit(8), userId, getActivity(), getContext());
        latestList.setAdapter(winAdapter);
        if(winAdapter!=null)
            winAdapter.startListening();
            */
    }

    private void loadLatest() {
        Log.i(TAG, "loadPost: ");
        latestAdapter = new BankerAdapter(database.collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).limit(8), userId, getActivity(), getContext());
        latestList.setAdapter(latestAdapter);
        if(latestAdapter!=null)
            latestAdapter.startListening();
    }

    private void loadWinning() {
        Log.i(TAG, "loadWinning: ");
        winAdapter = new BankerAdapter(database.collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).whereEqualTo("status", 2).limit(8), userId, getActivity(), getContext());
        winningsList.setAdapter(winAdapter);
        if(winAdapter!=null)
            winAdapter.startListening();
    }

}
