package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.SubscriptionActivity;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Comment;
import models.Post;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.Reusable;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.CommentHolder>{
    private final String TAG = "CommentAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private String mainPostId;
    private Calculations calculations;
    private RequestOptions requestOptions = new RequestOptions();
    private StorageReference storageReference;
    private FirebaseFirestore database;

    public CommentAdapter(String mainPostId, Query query, String userID, Activity activity, Context context) {
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
        calculations = new Calculations(context);
        this.userId = userID;
        this.mainPostId = mainPostId;
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
        requestOptions.placeholder(R.drawable.dummy);
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

        if(model.isFlag())
            holder.lnrContainer.setBackgroundColor(context.getResources().getColor(R.color.comment_flagged));
        else
            holder.lnrContainer.setBackgroundColor(context.getResources().getColor(R.color.comment_bg));


        //set username and comment content
        mUsername.setText(model.getUsername());
        mComment.setText(model.getContent());
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getUserId()))
                .into(imgDp);

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
                if(model.getDislikes().contains(userId)){
                    holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                    holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");
                }
                else{
                    if(model.getLikes().contains(userId)){
                        holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        holder.mLikes.setText(model.getLikesCount()-1>0?String.valueOf(model.getLikesCount()-1):"");
                    }
                    else{
                        holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                    }
                }
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onCommentLike(userId, model.getUserId(), postId, mainPostId, substring);
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

                if(model.getLikes().contains(userId)){
                    holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    holder.mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
                    holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
                }
                else{
                    if(model.getDislikes().contains(userId)){
                        holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");
                    }
                    else{
                        holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
                    }
                }
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onCommentDislike(userId, model.getUserId(), postId, mainPostId, substring);
            }
        });
        imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model, model.getUserId(), postId, holder.imgOverflow);
            }
        });
    }

    private void displayOverflow(Comment model, String commentUserId, String postId, ImageView imgOverflow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView;
        Reusable reusable = new Reusable();
        if(commentUserId.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();

        Button btnSubmit, btnDelete, btnFollow, btnSubscribe, btnShare;
        btnSubmit = dialog.findViewById(R.id.btnSubmit); btnSubmit.setVisibility(View.GONE);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        if(!commentUserId.equals(this.userId))
            btnDelete.setVisibility(View.GONE);
        btnFollow = dialog.findViewById(R.id.btnFollow);
        btnShare = dialog.findViewById(R.id.btnShare);
        btnSubscribe = dialog.findViewById(R.id.btnSubscribe);
        btnSubscribe.setVisibility(UserNetwork.getSubscribed()==null||!UserNetwork.getSubscribed().contains(userId)?
                View.VISIBLE: View.GONE);

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(commentUserId)? "UNFOLLOW": "FOLLOW");

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reusable.shareTips(activity, model.getUsername(), model.getContent());
                dialog.cancel();
            }
        });
        btnFollow.setOnClickListener(v -> {
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, commentUserId);
            }
            else{
                calculations.unfollowMember(imgOverflow, userId, commentUserId);
            }
            dialog.cancel();
        });

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SubscriptionActivity.class);
                intent.putExtra("userId", commentUserId);
                context.startActivity(intent);
                dialog.cancel();
            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment(postId, imgOverflow);
            }
        });
    }

    private void deleteComment(String postId, View imgOverflow){
        final DocumentReference postPath =  database.collection("posts").document(mainPostId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    Log.i(TAG, "apply: snapshot is empty");
                    return null;
                }

                Map<String, Object> updates = new HashMap<>();
                final long commentCount = snapshot.toObject(Post.class).getCommentsCount() -1; //Retrieve commentCount stat
                updates.put("commentsCount", Math.max(0,commentCount));
                transaction.update(postPath, updates);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                database.collection("comments").document(postId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(imgOverflow, "Comment deleted", Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(imgOverflow, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(imgOverflow, "Something went wrong", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new CommentHolder(view);
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mComment;
        TextView mUsername;
        TextView mTime;
        TextView mLikes, mDislikes;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgShare;
        public CommentHolder(View itemView) {
            super(itemView);
            lnrContainer = itemView.findViewById(R.id.container_post);
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