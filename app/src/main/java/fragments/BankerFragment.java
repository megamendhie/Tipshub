package fragments;
/*
    This class is attached to MainActivity
 */


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adapters.BankerAdapter;
import adapters.FilteredBankerAdapter;
import models.Post;
import models.ProfileMedium;
import models.SnapId;
import models.UserNetwork;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankerFragment extends Fragment {
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Gson gson = new Gson();
    private String json;
    private ProfileMedium myProfile;
    private SharedPreferences prefs;
    private TextView txtNotice;
    String userId;
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        subscribedList = rootView.findViewById(R.id.subscribedList);
        latestList = rootView.findViewById(R.id.latestList);
        winningsList = rootView.findViewById(R.id.winningsList);
        fabPost = rootView.findViewById(R.id.fabPost);
        txtNotice = rootView.findViewById(R.id.txtNotice);
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
                json = prefs.getString("profile", "");
                myProfile = (json.equals(""))? null: gson.fromJson(json, ProfileMedium.class);
                if(myProfile==null){
                    Toast.makeText(getContext(), "My profile is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!myProfile.isC1_banker()){
                    popUp();
                    return;
                }
                intent.putExtra("type", "banker");
                startActivity(intent);
            }
        });

        loadLatest();
        loadWinning();
        loadSub();
        return rootView;
    }

    private void loadSub(){
        if(UserNetwork.getSubscribed()==null){
            database.collection("subscribed_to").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.getResult()==null|| !task.getResult().contains("list"))
                                loadList(null);
                            else
                                loadList((ArrayList<String>) task.getResult().get("list"));
                        }
                    });
        }
        else
            loadList(UserNetwork.getFollowing());
    }

    private void loadList(ArrayList<String> userIds){
        if(userIds==null||userIds.isEmpty())
            return;
        int count = userIds.size();

        //create task and query for each followed id
        Query[] queries = new Query[count];
        Task[] tasks = new Task[count];

        for(int i = 0; i < count; i++){
            queries[i] = database.collection("posts").orderBy("time", Query.Direction.DESCENDING)
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
                txtNotice.setVisibility(!posts.isEmpty()? View.GONE: View.VISIBLE);
                Collections.sort(posts);
                Collections.sort(snapIds);
                subscribedList.setAdapter(new FilteredBankerAdapter(userId, getActivity(), getContext(), posts, snapIds));
            }
        });

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

    private void popUp(){
        String message = "<p><span style=\"color: #F80051;\"><strong>Thanks for the interest</strong></span></p>\n" +
                "<p>Unfortunately, you don&rsquo;t have the approval to post banker tips yet. Banker tips are mainly for your subscribers.</p>\n" +
                "<p><span style=\"color: #F80051;\"><strong>Conditions for approval</strong></span></p>\n" +
                "<ul>\n" +
                "<li>You have posted at least 50 tips.</li>\n" +
                "<li>Won at least 70% of them.</li>\n" +
                "<li>Be very active on the app.</li>\n" +
                "</ul>";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .show();
    }
}
