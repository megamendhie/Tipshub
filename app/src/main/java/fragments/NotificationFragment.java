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

import adapters.NotificationAdapter;
import utils.FirebaseUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {
    private String userId;
    private RecyclerView notificationList;


    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_notification, container, false);
        notificationList = rootView.findViewById(R.id.testList);
        notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();
        loadPost();
        return rootView;
    }

    private void loadPost() {
        String TAG = "NotificationFrag";
        Log.i(TAG, "loadPost: ");
        Query query = FirebaseUtil.getFirebaseFirestore().collection("notifications").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId);
        NotificationAdapter notificationAdapter = new NotificationAdapter(query, userId, getContext());
        notificationList.setAdapter(notificationAdapter);
        if(notificationAdapter !=null){
            Log.i(TAG, "loadPost: started listening");
            notificationAdapter.startListening();
        }
    }

}
