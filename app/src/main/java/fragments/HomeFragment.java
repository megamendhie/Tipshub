package fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sqube.tipshub.PostActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adapters.FilteredPostAdapter;
import adapters.PostAdapter;
import models.Post;
import models.SnapId;

public class HomeFragment extends Fragment{
    private FirebaseFirestore database;
    private Query query;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private final String TAG = "HomeFrag";
    String userId;
    PostAdapter postAdapter;
    FloatingActionButton fabTip, fabNormal;
    FloatingActionMenu fabMenu;
    public RecyclerView homeFeed;
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
        homeFeed = rootView.findViewById(R.id.testList);
        homeFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((DefaultItemAnimator) homeFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        fabMenu = rootView.findViewById(R.id.fabMenu);
        fabNormal = rootView.findViewById(R.id.fabNormal);
        fabTip = rootView.findViewById(R.id.fabPost);
        fabTip.setOnClickListener(new View.OnClickListener() {
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
        loadMerged();
        return rootView;
    }

    private void loadPost() {
        Log.i(TAG, "loadPost: ");
        query = database.collection("posts").orderBy("time", Query.Direction.DESCENDING);
        postAdapter = new PostAdapter(query, userId, getActivity(), getContext());
        homeFeed.setAdapter(postAdapter);
        if(postAdapter!=null){
            Log.i(TAG, "loadPost: started listening");
            postAdapter.startListening();
        }
    }

    private void loadMerged(){
        String[] userIds = {"9netjQqxyATkN6SbHmaDAYDhzT43", "c3cMX8YsnCUOSATLznuolZBmI0x1"};
        int count = userIds.length;

        Query[] queries = new Query[count];
        Task[] tasks = new Task[count];

        for(int i = 0; i < userIds.length; i++){
            queries[i] = database.collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds[i]).limit(3);
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
                Collections.sort(posts);
                Collections.sort(snapIds);
                homeFeed.setAdapter(new FilteredPostAdapter(userId, getActivity(), getContext(), posts, snapIds));
            }
        });


    }
}
