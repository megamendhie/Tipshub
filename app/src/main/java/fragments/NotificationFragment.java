package fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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


    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_notification, container, false);
        RecyclerView notificationList = rootView.findViewById(R.id.postList);
        notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        SwipeRefreshLayout refresher = rootView.findViewById(R.id.refresher);
        refresher.setColorSchemeResources(R.color.colorPrimary);

        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        String userId = user.getUid();
        Query query = FirebaseUtil.getFirebaseFirestore().collection("notifications")
                .orderBy("time", Query.Direction.DESCENDING).whereEqualTo("sendTo", userId).limit(40);

        NotificationAdapter notificationAdapter = new NotificationAdapter(query, userId, getContext());
        notificationList.setAdapter(notificationAdapter);
        notificationAdapter.startListening();

        refresher.setOnRefreshListener(() -> {
            refresher.setRefreshing(true);
            if(notificationAdapter !=null) {
                notificationAdapter.stopListening();
                notificationAdapter.startListening();
            }
            refresher.setRefreshing(false);
        });
        return rootView;
    }
}