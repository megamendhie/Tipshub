package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.R;

import javax.annotation.Nullable;

import models.Post;
import utils.Calculations;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private FirebaseFirestore database;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};


    public PostAdapter(Query query, String userID, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.context = context;
        this.userId = userID;
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull Post model) {
        Log.i(TAG, "onBindViewHolder: executed");
        final LinearLayout lnrCode = holder.lnrCode;
        final TextView mpost = holder.mpost;
        final TextView mUsername = holder.mUsername;
        final TextView mTime = holder.mTime;
        final TextView mComment = holder.mComment;
        final TextView mLikesCount = holder.mLikes;
        final TextView mDislikesCount = holder.mDislikes;
        final TextView mCode = holder.mCode;
        final TextView mType = holder.mType;
        final ImageView imgLikes = holder.imgLikes;
        final ImageView imgDislikes = holder.imgDislike;
        final ImageView imgStatus = holder.imgStatus;
        final ImageView imgCode = holder.imgCode;
        final ImageView imgComment = holder.imgComment;
        final ImageView imgShare = holder.imgShare;
        final String postId = getSnapshots().getSnapshot(position).getId();
        final boolean[] liked = new boolean[1];
        final boolean[] disliked = new boolean[1];

        imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
            mCode.setVisibility(View.VISIBLE);
            imgCode.setVisibility(View.VISIBLE);
            lnrCode.setVisibility(View.VISIBLE);
        }
        else{
            lnrCode.setVisibility(View.GONE);
            mCode.setVisibility(View.GONE);
            imgCode.setVisibility(View.GONE);
        }
        if(model.getType()==0){
            mType.setVisibility(View.GONE);
        }
        else{
            mType.setVisibility(View.VISIBLE);
            mType.setText(type[model.getType()-1]);
        }
        mUsername.setText(model.getUsername());
        mpost.setText(model.getContent());
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        database.collection("likes").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onSuccess: userID = " + userId);
                if(documentSnapshot.contains(userId)){
                    liked[0] = true;
                    imgLikes.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                }
                else{
                    liked[0] = false;
                    imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                }
                return;
            }
        });

        database.collection("dislikes").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.contains(userId)){
                    disliked[0] = true;
                    imgDislikes.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                }
                else{
                    disliked[0] = false;
                    imgDislikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                }
                return;
            }
        });

        database.collection("likes").document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

            }
        });

        mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        imgLikes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations();
                if(disliked[0]){
                    calculations.Like(true, postId, userId);
                }
                else{
                    if(liked[0]){
                        calculations.Unlike(postId, userId);
                    }
                    else{
                        calculations.Like(false, postId, userId);
                    }
                }
                return false;
            }
        });
        imgDislikes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations();
                if(liked[0]){
                    calculations.Dislike(true, postId, userId);
                }
                else{
                    if(disliked[0]){
                        calculations.Undislike(postId, userId);
                    }
                    else{
                        calculations.Dislike(false, postId, userId);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new PostHolder(view);
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        LinearLayout lnrCode;
        TextView mpost;
        TextView mUsername;
        TextView mTime;
        TextView mLikes, mDislikes, mComment, mCode, mType;
        ImageView imgLikes, imgDislike, imgComment, imgShare, imgStatus, imgCode;
        public PostHolder(View itemView) {
            super(itemView);
            lnrCode = itemView.findViewById(R.id.lnrCode);
            mpost = itemView.findViewById(R.id.txtPost);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mTime = itemView.findViewById(R.id.txtTime);

            mLikes = itemView.findViewById(R.id.txtLike);
            mDislikes = itemView.findViewById(R.id.txtDislike);
            mComment = itemView.findViewById(R.id.txtComment);
            mCode = itemView.findViewById(R.id.txtCode);
            mType = itemView.findViewById(R.id.txtPostType);

            imgLikes = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgShare = itemView.findViewById(R.id.imgShare);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgCode = itemView.findViewById(R.id.imgCode);

        }
    }
}