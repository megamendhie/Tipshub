package fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.R;

import adapters.PostAdapter;
import utils.FirebaseUtil;

public class PostFragment extends Fragment {
    private String userId, myId;
    private RecyclerView recyclerView;

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
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        myId = user.getUid();
        userId = getArguments().getString("userId");
        loadPost();
        return rootView;
    }

    private void loadPost() {
        String TAG = "PostFragment";
        Log.i(TAG, "loadPost: ");
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", userId);
        PostAdapter postAdapter = new PostAdapter(query, myId, getActivity(), getContext());
        recyclerView.setAdapter(postAdapter);
        if(postAdapter !=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }
}
