package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

public class PeopleAdapter extends FirestoreRecyclerAdapter<ProfileShort, PeopleAdapter.PostHolder>{
    private final String TAG = "PplAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;

    public PeopleAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final ProfileShort model) {
        Log.i(TAG, "onBindViewHolder: executed");
        holder.mBio.setText(model.getA5_bio());
        holder.mUsername.setText(model.getA2_username());
        holder.mPost.setText(model.getE0a_NOG()+ " tips");
        holder.mAccuracy.setText(String.format("||  Accuracy: %.1f", (double) model.getE0c_WGP())+"%");
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new PostHolder(view);
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mUsername, mPost, mAccuracy, mBio;
        Button btnFollow;
        public PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            lnrContainer = itemView.findViewById(R.id.lnrContainer);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mPost = itemView.findViewById(R.id.txtPost);
            mAccuracy = itemView.findViewById(R.id.txtAccuracy);
            mBio = itemView.findViewById(R.id.txtBio);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}