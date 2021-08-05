package fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.R;

import adapters.PostAdapter;
import models.Post;
import utils.Calculations;
import utils.FirebaseUtil;

public class PostFragment extends Fragment {
    private String userId, myId;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private PostAdapter postAdapter;

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
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            myId= Calculations.GUEST;
        else
            myId = user.getUid();
        userId = getArguments().getString("userId");
        loadPost();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        if(user==null)
            myId= Calculations.GUEST;
        else
            myId = user.getUid();
        if(postAdapter!=null)
            postAdapter.setUserId(myId);
    }

    private void loadPost() {
        String TAG = "PostFragment";
        Log.i(TAG, "loadPost: ");
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", userId);


        FirestoreRecyclerOptions<Post> response = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        postAdapter = new PostAdapter(response, myId, getContext(), false);
        recyclerView.setAdapter(postAdapter);
        if(postAdapter !=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }
}
