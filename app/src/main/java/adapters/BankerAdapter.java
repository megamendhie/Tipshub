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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FlagActivity;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;
import com.sqube.tipshub.SubscriptionActivity;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

public class BankerAdapter extends FirestoreRecyclerAdapter<Post, BankerAdapter.PostHolder>{
    private final String TAG = "BankerAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private Calculations calculations;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public BankerAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build());

        Log.i(TAG, "BankerAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.calculations = new Calculations(context);
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        storageReference = FirebaseUtil.getFirebaseStorage().getReference().child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull final PostHolder holder, final int position, @NonNull final Post model) {
        Log.i(TAG, "onBindViewHolder: executed");
        boolean makeVisible = false, makePublic = false;
        final LinearLayout lnrContainer = holder.lnrContainer;
        final CircleImageView imgDp = holder.imgDp;
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
        final ImageView imgComment = holder.imgComment;
        final ImageView imgRepost = holder.imgRepost;
        final ImageView imgOverflow = holder.imgOverflow;
        final String postId = getSnapshots().getSnapshot(position).getId();

        mUsername.setText(model.getUsername());
        imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
            mCode.setVisibility(View.VISIBLE);
        }
        else
            mCode.setVisibility(View.GONE);

        if(model.getType()==0){
            mType.setVisibility(View.GONE);
        }
        else{
            mType.setVisibility(View.VISIBLE);
            mType.setText(type[model.getType()-1]);
        }
        if(model.getUserId().equals(userId))
            makeVisible = true;
        else if(UserNetwork.getSubscribed()!=null && UserNetwork.getSubscribed().contains(this.userId))
            makeVisible = true;
        if(model.getStatus()==2 || (new Date().getTime() - model.getTime()) >(18*60*60*1000))
            makePublic = true;
        if(makeVisible|| makePublic){
            holder.lnrSub.setVisibility(View.GONE);
        }
        else{
            mpost.setMaxLines(6);
            holder.mSub.setText("Subscribe to "+ model.getUsername());

        }
        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getUserId()))
                .signature(new ObjectKey(model.getUserId()+"_"+Reusable.getSignature()))
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
        mpost.setText(model.getContent());
        Reusable.applyLinkfy(context, model.getContent(), holder.mpost);
        mTime.setText(Reusable.getTime(model.getTime()));
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        final boolean finalMakePublic = makePublic;
        final boolean finalMakeVisible = makeVisible;
        imgRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!finalMakePublic) {
                    Snackbar.make(mComment, "You can't repost yet", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, RepostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });

        mpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display full post with comments if visibility or public is set true
                if(!finalMakePublic && !finalMakeVisible) {
                    Intent intent = new Intent(context, SubscriptionActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    context.startActivity(intent);
                    return;
                }
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });

        lnrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display full post with comments if visibility or public is set true
                if(!finalMakePublic && !finalMakeVisible) {
                    Intent intent = new Intent(context, SubscriptionActivity.class);
                    intent.putExtra("userId", model.getUserId());
                    context.startActivity(intent);
                    return;
                }
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });

        imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display full post with comments if visibility or public is set true
                if(!finalMakePublic && !finalMakeVisible) {
                    Snackbar.make(mComment, "Access denied", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });

        imgLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.getDislikes().contains(userId);
                if(model.getDislikes().contains(userId)){
                    imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    imgDislikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    mLikesCount.setText(String.valueOf(model.getLikesCount()+1));
                    mDislikesCount.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");
                }
                else{
                    if(model.getLikes().contains(userId)){
                        imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        mLikesCount.setText(model.getLikesCount()-1>0?String.valueOf(model.getLikesCount()-1):"");
                    }
                    else{
                        imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        mLikesCount.setText(String.valueOf(model.getLikesCount()+1));
                    }
                }
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onLike(postId, userId, model.getUserId(), substring);
            }
        });

        imgDislikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getLikes().contains(userId)){
                    imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    imgDislikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    mLikesCount.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
                    mDislikesCount.setText(String.valueOf(model.getDislikesCount()+1));
                }
                else{
                    if(model.getDislikes().contains(userId)){
                        imgDislikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        mDislikesCount.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");
                    }
                    else{
                        imgDislikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        mDislikesCount.setText(String.valueOf(model.getDislikesCount()+1));
                    }
                }
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onDislike( postId, userId, model.getUserId(), substring);
            }
        });

        imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), imgOverflow, finalMakePublic);
            }
        });
    }

    private void displayOverflow(final Post model, String userID, final String postId, int status, int type, ImageView imgOverflow,
                                 final boolean makePublic) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView;
        if(userID.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();

        Button btnSubmit, btnDelete, btnShare, btnFollow;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnShare = dialog.findViewById(R.id.btnShare);
        btnFollow = dialog.findViewById(R.id.btnFollow);

        long timeDifference = new Date().getTime() - model.getTime();
        if(model.getUserId().equals(userId)&& model.getType()>0 && timeDifference > 9000000)
            btnDelete.setEnabled(false);
        if(model.getUserId().equals(userId) && model.getType()==0)
            btnSubmit.setVisibility(View.GONE);
        else if(model.getUserId().equals(userId)&& timeDifference > 144000000)
            btnSubmit.setVisibility(View.GONE);
        else {
            if (model.getUserId().equals(userId) && model.getStatus() == 2 && timeDifference <= 9000000)
                btnSubmit.setText("CANCEL WON");
            if (model.getUserId().equals(userId) && model.getStatus() == 2 && timeDifference > 9000000)
                btnSubmit.setVisibility(View.GONE);
        }

        if(!makePublic){
            btnShare.setVisibility(View.GONE);
        }

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userID)? "UNFOLLOW": "FOLLOW");


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getType()>0)
                    popUp();
                dialog.cancel();
            }
            private void popUp(){
                String message = "<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Your tips have delivered?</strong></span></p>\n" +
                        "<p>By clicking 'YES', you confirm that your prediction has delivered.</p>\n" +
                        "<p>Your account may be suspended or terminated if that's not true.</p>";
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setMessage(Html.fromHtml(message))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                calculations.onPostWon(imgOverflow, postId, userId, type);
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
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnDelete.getText().toString().toLowerCase().equals("flag")){
                    Intent intent = new Intent(context, FlagActivity.class);
                    intent.putExtra("postId", postId);
                    intent.putExtra("reportedUsername", model.getUsername());
                    intent.putExtra("reportedUserId", userID);
                    context.startActivity(intent);
                    dialog.cancel();
                }
                else{
                    if(model.getType()>0)
                        calculations.onDeletePost(imgOverflow, postId, userId,status==2, type);
                    else {
                        FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId).delete();
                        Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show();
                    }
                }
                dialog.cancel();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reusable.shareTips(activity, model.getUsername(), model.getContent());
                dialog.cancel();
            }
        });

        btnFollow.setOnClickListener(v -> {
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, userID);
            }
            else{
                calculations.unfollowMember(imgOverflow, userId, userID);
            }
            dialog.cancel();
        });
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view_banker, parent, false);
        return new PostHolder(view);
    }

    static class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        RelativeLayout lnrSub;
        LinearLayout lnrContainer;
        TextView mpost, mUsername, mTime;
        TextView mLikes, mDislikes, mComment, mCode, mType, mSub;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgComment, imgRepost, imgStatus;
        PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            lnrSub = itemView.findViewById(R.id.lnrSub);
            lnrContainer = itemView.findViewById(R.id.container_post);

            mpost = itemView.findViewById(R.id.txtPost);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mTime = itemView.findViewById(R.id.txtTime);
            mSub = itemView.findViewById(R.id.txtSub);

            mLikes = itemView.findViewById(R.id.txtLike);
            mDislikes = itemView.findViewById(R.id.txtDislike);
            mComment = itemView.findViewById(R.id.txtComment);
            mCode = itemView.findViewById(R.id.txtCode);
            mType = itemView.findViewById(R.id.txtPostType);

            imgLikes = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgRepost = itemView.findViewById(R.id.imgRepost);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
        }
    }
}