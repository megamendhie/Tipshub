package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Comment;
import models.UserNetwork;
import utils.Calculations;
import utils.Reusable;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.CommentHolder>{
    private final String TAG = "CommentAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private FirebaseFirestore database;

    public CommentAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Comment.class instructs the adapter to convert each DocumentSnapshot to a Comment object
        */
        super(new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build());

        Log.i(TAG, "CommentAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull CommentHolder holder, final int position, @NonNull final Comment model) {
        Log.i(TAG, "onBindViewHolder: executed");
        final TextView mComment = holder.mComment;
        final TextView mUsername = holder.mUsername;
        final TextView mTime = holder.mTime;
        final TextView mLikesCount = holder.mLikes;
        final TextView mDislikesCount = holder.mDislikes;
        final CircleImageView imgDp = holder.imgDp;
        final ImageView imgLikes = holder.imgLikes;
        final ImageView imgDislikes = holder.imgDislike;
        final ImageView imgShare = holder.imgShare;
        final ImageView imgOverflow = holder.imgOverflow;
        final String postId = getSnapshots().getSnapshot(position).getId();
        final DocumentReference commentRef = getSnapshots().getSnapshot(position).getReference();

        //set username and comment content
        mUsername.setText(model.getUsername());
        mComment.setText(model.getContent());
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        //listen to dp click and open user profile
        imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getUserId().equals(userId)){
                    context.startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    context.startActivity(intent);
                }
            }
        });

        //listen to username click and open user profile
        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getUserId().equals(userId)){
                    context.startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    context.startActivity(intent);
                }
            }
        });

        imgLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onCommentLike(commentRef, userId, model.getUserId());
            }
        });

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reusable reusable = new Reusable();
                reusable.shareComment(activity, model.getUsername(), model.getContent());
            }
        });

        imgDislikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onCommentDislike(commentRef, userId);
            }
        });
        imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model.getUserId());
            }
        });
    }

    private void displayOverflow(String userId) {
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

        Button btnSubmit, btnDelete, btnFollow, btnSubscribe, btnObject;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnFollow = dialog.findViewById(R.id.btnFollow);
        btnSubscribe = dialog.findViewById(R.id.btnSubscribe);
        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(this.userId)? "UNFOLLOW": "FOLLOW");
    }

    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new CommentHolder(view);
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        TextView mComment;
        TextView mUsername;
        TextView mTime;
        TextView mLikes, mDislikes;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgShare;
        public CommentHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            mComment = itemView.findViewById(R.id.txtPost);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mTime = itemView.findViewById(R.id.txtTime);
            mLikes = itemView.findViewById(R.id.txtLike);
            mDislikes = itemView.findViewById(R.id.txtDislike);

            imgLikes = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgShare = itemView.findViewById(R.id.imgShare);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
        }
    }
}