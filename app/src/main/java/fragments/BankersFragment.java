package fragments;
/*
    This fragment is attached to Profile activity
 */

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.R;

import adapters.BankerAdapter;
import utils.Calculations;
import utils.FirebaseUtil;

public class BankersFragment extends Fragment {
    private String userId, myId;
    private BankerAdapter postAdapter;
    private RecyclerView recyclerView;
    private FirebaseUser user;

    public BankersFragment() {
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
        View rootView=inflater.inflate(R.layout.fragment_bankers, container, false);
        recyclerView = rootView.findViewById(R.id.postList);
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

    private void loadPost() {
        String TAG = "PostFragment";
        Log.i(TAG, "loadPost: ");
        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("userId", userId).whereEqualTo("type", 6);
        postAdapter = new BankerAdapter(query, myId, getContext());
        recyclerView.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
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

}
