package fragments;
/*
    This class is attached to MainActivity
 */


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.BankerAdapter;
import adapters.BankerTipsterAdapter;
import adapters.FilteredBankerAdapter;
import models.Post;
import models.ProfileMedium;
import models.ProfileShort;
import models.SnapId;
import models.UserNetwork;
import utils.FirebaseUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class BankerFragment extends Fragment {
    private Gson gson = new Gson();
    private String json;
    private ProfileMedium myProfile;
    private SharedPreferences prefs;
    private TextView txtNotice;
    private String userId;
    private RecyclerView subscribedList, latestList, winningsList, bankersList;
    private final String TAG = "BankerFragment";
    private Intent intent;

    private View rootView;
    public BankerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment only if its null
        if(rootView==null)
            rootView = inflater.inflate(R.layout.fragment_banker, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        subscribedList = rootView.findViewById(R.id.subscribedList);
        latestList = rootView.findViewById(R.id.latestList);
        winningsList = rootView.findViewById(R.id.winningsList);
        bankersList = rootView.findViewById(R.id.bankersList);
        FloatingActionButton fabPost = rootView.findViewById(R.id.fabPost);
        txtNotice = rootView.findViewById(R.id.txtNotice);

        ((DefaultItemAnimator) subscribedList.getItemAnimator()).setSupportsChangeAnimations(false);
        ((DefaultItemAnimator) winningsList.getItemAnimator()).setSupportsChangeAnimations(false);
        ((DefaultItemAnimator) latestList.getItemAnimator()).setSupportsChangeAnimations(false);

        subscribedList.setLayoutManager(new LinearLayoutManager(getActivity()));
        latestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        winningsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        bankersList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        intent = new Intent(getContext(), PostActivity.class);
        FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();

        fabPost.setOnClickListener(v -> {
            json = prefs.getString("profile", "");
            myProfile = (json.equals(""))? null: gson.fromJson(json, ProfileMedium.class);
            if(myProfile==null){
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!myProfile.isC1_banker()){
                popUp();
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            String currentTime = sdf.format(new Date().getTime());
            String lastBankerTime = sdf.format(new Date(myProfile.getD3_bankerPostTime()));

            try {
                Date currentDate = sdf.parse(currentTime);
                Date lastPostDate = sdf.parse(lastBankerTime);
                if(currentDate.equals(lastPostDate)){
                    popUp2();
                    return;
                }
                intent.putExtra("type", "banker");
                startActivity(intent);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        loadBankerTipsters();
        loadLatest();
        loadWinning();
        loadSub();
        return rootView;
    }

    private void loadSub(){
        if(UserNetwork.getSubscribed()==null){
            FirebaseUtil.getFirebaseFirestore().collection("subscribed_to").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult().contains("list"))
                                loadList((ArrayList<String>) task.getResult().get("list"));
                            else
                                loadList(null);
                        }
                    });
        }
        else
            loadList(UserNetwork.getSubscribed());
    }

    private void loadList(ArrayList<String> userIds){
        if(userIds==null||userIds.isEmpty()){
            txtNotice.setText("You haven't subscribed to anyone yet");
            return;
        }
        int count = userIds.size();

        Log.i(TAG, "loadList: " + userIds);
        //create task and query for each followed id
        Query[] queries = new Query[count];
        Task[] tasks = new Task[count];

        for(int i = 0; i < count; i++){
            queries[i] = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds.get(i)).whereEqualTo("type", 6).limit(2);
            tasks[i] = queries[i].get();
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> list) {
                ArrayList<Post> posts = new ArrayList<>();
                ArrayList<SnapId> snapIds= new ArrayList<>();
                for(Object object: list){
                    QuerySnapshot querySnapshot = (QuerySnapshot) object;
                    if(querySnapshot !=null || !querySnapshot.isEmpty()){
                        for(DocumentSnapshot snapshot: querySnapshot.getDocuments()){
                            Post post = snapshot.toObject(Post.class);
                            posts.add(post);
                            snapIds.add(new SnapId(snapshot.getId(), post.getTime()));
                        }
                    }
                }
                if(posts.isEmpty()){
                    txtNotice.setText("No tips at the moment");
                    txtNotice.setVisibility(View.VISIBLE);
                }
                else
                    txtNotice.setVisibility(View.GONE);
                Collections.sort(posts);
                Collections.sort(snapIds);
                subscribedList.setAdapter(new FilteredBankerAdapter(userId, getContext(), posts, snapIds));
            }
        });

    }

    private void loadBankerTipsters() {
        Query query = FirebaseUtil.getFirebaseFirestore().collection("profiles").orderBy("e6c_WGP")
                .whereEqualTo("c1_banker", true).limit(10);
        FirestoreRecyclerOptions<ProfileShort> options = new FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort.class)
                .build();
        BankerTipsterAdapter bankerTipsterAdapter = new BankerTipsterAdapter(options);
        bankersList.setAdapter(bankerTipsterAdapter);
        bankerTipsterAdapter.startListening();
    }

    private void loadLatest() {
        Log.i(TAG, "loadPost: ");
        BankerAdapter latestAdapter = new BankerAdapter(FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).limit(8), userId, getContext(), true);
        latestList.setAdapter(latestAdapter);
        if(latestAdapter !=null)
            latestAdapter.startListening();
    }

    private void loadWinning() {
        Log.i(TAG, "loadWinning: ");
        BankerAdapter winAdapter = new BankerAdapter(FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).whereEqualTo("status", 2).limit(8), userId, getContext(), true);
        winningsList.setAdapter(winAdapter);
        if(winAdapter !=null)
            winAdapter.startListening();
    }

    private void popUp(){
        String message = "<p><span style=\"color: #F80051;\"><strong>Thanks for the interest</strong></span></p>\n" +
                "<p>Unfortunately, you don&rsquo;t have the approval to post banker tips yet. Banker tips are mainly for your subscribers.</p>\n" +
                "<p><span style=\"color: #F80051;\"><strong>Conditions for approval</strong></span></p>\n" +
                "<ul>\n" +
                "<li>You have posted at least 50 tips.</li>\n" +
                "<li>Won at least 70% of them.</li>\n" +
                "<li>Be very active on the app.</li>\n" +
                "</ul>";
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.CustomMaterialAlertDialog);
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", (dialogInterface, i) -> {
                    //do nothing
                })
                .show();
    }

    private void popUp2(){
        String message = "<p><span style=\"color: #F80051;\"><strong>You already predicted today</strong></span></p>\n" +
                "<p>Sorry you cannot post any banker tip again for today. Give your subscribers your very surest tip for each day.</p>";
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.CustomMaterialAlertDialog);
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", (dialogInterface, i) -> {
                    //do nothing
                })
                .show();
    }
}
