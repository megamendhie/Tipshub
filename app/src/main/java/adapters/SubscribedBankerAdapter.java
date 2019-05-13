package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.LoginActivity;
import com.sqube.tipshub.R;

import models.Post;
import models.UserNetwork;
import utils.Calculations;

public class SubscribedBankerAdapter extends FirestoreRecyclerAdapter<Post, SubscribedBankerAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private FirebaseFirestore database;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};

    public SubscribedBankerAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Post model) {
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
        final ImageView imgOverflow = holder.imgOverflow;
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
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        mpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });
        imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });
        imgLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onLike(postId, userId, model.getUserId());
            }
        });

        imgDislikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onDislike( postId, userId);
            }
        });
        imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model.getUserId(), model.getStatus(), model.getType(), imgOverflow);
            }
        });
    }

    private void displayOverflow(String userId, int status, int type, ImageView imgOverflow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView;
        if(userId.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();

        Button btnSubmit, btnDelete, btnShare, btnFollow, btnSubscribe, btnObject;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnShare = dialog.findViewById(R.id.btnShare);
        btnFollow = dialog.findViewById(R.id.btnFollow);
        btnSubscribe = dialog.findViewById(R.id.btnSubscribe);

        btnFollow.setText(UserNetwork.getFollowing().contains(this.userId)? "yes": "no");
    }

    private void popUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You must login first")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, LoginActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .show();
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
        ImageView imgOverflow;
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
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
        }
    }
}