package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.LoginActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Comment;
import models.Post;
import models.SnapId;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

import static utils.Reusable.getPlaceholderImage;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.CommentHolder>{
    private final String TAG = "CommentAdapter";
    private Activity activity;
    private Context context;
    private String userId;
    private String mainPostId;
    private Calculations calculations;
    private StorageReference storageReference;
    private ArrayList<SnapId> repliedList = new ArrayList<>();
    private boolean anchorSnackbar;

    public ArrayList<SnapId> getRepliedList() {
        return repliedList;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void resetRepliesList(){
        repliedList.clear();
    }

    public CommentAdapter(String mainPostId, Query query, String userID, Activity activity, Context context, boolean anchorSnackbar) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Comment.class instructs the adapter to convert each DocumentSnapshot to a Comment object
        */
        super(new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build());
        this.anchorSnackbar = anchorSnackbar;

        Log.i(TAG, "CommentAdapter: created");
        this.activity = activity;
        this.context = context;
        calculations = new Calculations(context);
        this.setUserId(userID);
        this.mainPostId = mainPostId;
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
        final ImageView imgReply = holder.imgReply;
        final ImageView imgOverflow = holder.imgOverflow;
        final String postId = getSnapshots().getSnapshot(position).getId();

        if(model.isFlag())
            holder.lnrContainer.setBackgroundColor(context.getResources().getColor(R.color.comment_flagged));
        else
            holder.lnrContainer.setBackgroundColor(context.getResources().getColor(R.color.comment_bg));

        imgReply.setVisibility(model.getUserId().equals(userId)? View.GONE: View.VISIBLE);

        //set username and comment content
        mUsername.setText(model.getUsername());
        mComment.setText(model.getContent());
        Reusable.applyLinkfy(context, model.getContent(), mComment);
        mTime.setText(Reusable.getTime(model.getTime()));
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        GlideApp.with(context)
                .load(storageReference.child(model.getUserId()))
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(model.getUserId().charAt(0)))
                .signature(new ObjectKey(model.getUserId()+"_"+Reusable.getSignature()))
                .into(imgDp);

        //listen to dp click and open user profile
        imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getUserId().equals(userId)){
                    imgDp.getContext().startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    imgDp.getContext().startActivity(intent);
                }
            }
        });

        //listen to username click and open user profile
        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getUserId().equals(userId)){
                    mUsername.getContext().startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    mUsername.getContext().startActivity(intent);
                }
            }
        });

        imgLikes.setOnClickListener(v -> {
            Log.i(TAG, "onClick: Key is " + postId);
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(imgLikes);
                return;
            }
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
        });

        imgReply.setOnClickListener(v -> {
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(imgReply);
                return;
            }
            EditText edtComment = activity.findViewById(R.id.edtComment);
            String comment = edtComment.getText().toString();
            if(comment.isEmpty())
                edtComment.setText(String.format("@%s ", model.getUsername()));
            else
                edtComment.setText(String.format("%s @%s ", comment.trim(), model.getUsername()));
            edtComment.setSelection(edtComment.getText().length());
            repliedList.add(new SnapId(model.getUserId(), model.getUsername()));
        });

        imgDislikes.setOnClickListener(v -> {
            Log.i(TAG, "onClick: Key is " + postId);
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(imgDislikes);
                return;
            }

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
        });

        imgOverflow.setOnClickListener(v -> displayOverflow(model, model.getUserId(), postId, holder.imgOverflow));
    }

    private void loginPrompt(View view) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(view.getRootView().getContext(),
                R.style.CustomMaterialAlertDialog);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .setPositiveButton("Login", (dialogInterface, i) -> view.getContext().startActivity(new Intent(view.getContext(), LoginActivity.class)))
                .show();
    }

    private void displayOverflow(Comment model, String commentUserId, String postId, ImageView imgOverflow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(imgOverflow.getRootView().getContext());
        LayoutInflater inflater = LayoutInflater.from(imgOverflow.getRootView().getContext());
        View dialogView;
        Reusable reusable = new Reusable();
        if(commentUserId.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine_comment, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member_comment, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button btnSubmit, btnDelete, btnFollow, btnShare;
        btnSubmit = dialog.findViewById(R.id.btnSubmit); btnSubmit.setVisibility(View.GONE);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        if(!commentUserId.equals(this.userId))
            btnDelete.setVisibility(View.GONE);
        btnFollow = dialog.findViewById(R.id.btnFollow);
        btnShare = dialog.findViewById(R.id.btnShare);

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(commentUserId)? "UNFOLLOW": "FOLLOW");

        btnShare.setOnClickListener(v -> {
            reusable.shareComment(btnShare.getContext(), model.getUsername(), model.getContent());
            dialog.cancel();
        });

        btnFollow.setOnClickListener(v -> {
            if(!Reusable.getNetworkAvailability(context)){
                Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                dialog.cancel();
                return;
            }
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(btnFollow);
                return;
            }
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, commentUserId, anchorSnackbar);
            }
            else
                unfollowPrompt(imgOverflow, commentUserId, model.getUsername());
            dialog.cancel();
        });

        btnDelete.setOnClickListener(v -> {
            deleteComment(postId, imgOverflow);
            dialog.cancel();
        });
    }

    private void unfollowPrompt(ImageView imgOverflow, String userID, String username){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog);
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No", (dialogInterface, i) -> {
                    //do nothing
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> calculations.unfollowMember(imgOverflow, userId, userID, anchorSnackbar))
                .show();
    }

    private void deleteComment(String postId, View imgOverflow){
        final DocumentReference postPath =  FirebaseUtil.getFirebaseFirestore().collection("posts").document(mainPostId);
        FirebaseUtil.getFirebaseFirestore().runTransaction((Transaction.Function<Void>) transaction -> {
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
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseUtil.getFirebaseFirestore().collection("comments").document(postId).delete()
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

    class CommentHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mComment;
        TextView mUsername;
        TextView mTime;
        TextView mLikes, mDislikes;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgReply;
        CommentHolder(View itemView) {
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
            imgReply = itemView.findViewById(R.id.imgReply);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
        }
    }
}