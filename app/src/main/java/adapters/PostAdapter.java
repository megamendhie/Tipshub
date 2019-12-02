package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FlagActivity;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;

import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private Calculations calculations;
    private final int NORMAL_POST=1, BANKER_POST = 0;
    private RequestOptions requestOptions = new RequestOptions();

    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public PostAdapter(Query query, String userID, Activity activity, Context context) {
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
        this.calculations = new Calculations(context);
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getType()==6 && getItem(position).getStatus()!=2){
            return BANKER_POST;
        }
        return NORMAL_POST;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Post model) {
        if(model.getType()==6 && getItem(position).getStatus()!=2){
            return;
        }
        Log.i(TAG, "onBindViewHolder: executed");
        final LinearLayout lnrChildContainer = holder.lnrChildContainer;
        final String postId = getSnapshots().getSnapshot(position).getId();

        holder.mUsername.setText(model.getUsername());
        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        holder.crdChildPost.setVisibility(model.isHasChild()? View.VISIBLE: View.GONE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            holder.mCode.setText(String.format(Locale.ENGLISH, "%s @%s",
                    model.getBookingCode(), code[(model.getRecommendedBookie()-1)]));
            holder.mCode.setVisibility(View.VISIBLE);
        }
        else
            holder.mCode.setVisibility(View.GONE);

        if(model.getType()==0){
            holder.mType.setVisibility(View.GONE);
        }
        else{
            holder.mType.setVisibility(View.VISIBLE);
            holder.mType.setText(type[model.getType()-1]);
        }

        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getUserId()))
                .signature(new ObjectKey(model.getUserId()+"_"+Reusable.getSignature()))
                .into(holder.imgDp);
        //listen to dp click and open user profile
        holder.imgDp.setOnClickListener(v -> {
            if(model.getUserId().equals(userId)){
                context.startActivity(new Intent(context, MyProfileActivity.class));
            }
            else{
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("userId", model.getUserId());
                context.startActivity(intent);
            }
        });
        //listen to username click and open user profile
        holder.mUsername.setOnClickListener(v -> {
            if(model.getUserId().equals(userId)){
                context.startActivity(new Intent(context, MyProfileActivity.class));
            }
            else{
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("userId", model.getUserId());
                context.startActivity(intent);
            }
        });
        holder.mpost.setText(model.getContent());
        Reusable.applyLinkfy(context, model.getContent(), holder.mpost);
        holder.mTime.setText(Reusable.getTime(model.getTime()));
        holder.imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        holder.imgRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RepostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });

        holder.lnrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });
        holder.mpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });
        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });
        holder.imgLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                calculations.onLike(postId, userId, model.getUserId(), substring);
            }
        });

        holder.imgDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                calculations.onDislike( postId, userId, model.getUserId(), substring);
            }
        });
        holder.imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), holder.imgOverflow);
            }
        });

        if(model.isHasChild()){
            displayChildContent(model, holder);
        }
    }

    private void displayChildContent(final Post model, final PostHolder holder) {
        final TextView childPost= holder.childPost;
        final TextView childUsername = holder.childUsername;
        final TextView childCode = holder.childCode, childType = holder.childType;
        final CircleImageView childDp = holder.childDp;

        //holder.imgChildStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getChildBookingCode()!=null && !model.getChildBookingCode().isEmpty()){
            childCode.setText(String.format(Locale.ENGLISH, "%s @%s",
                    model.getChildBookingCode(), code[(model.getChildBookie()-1)]));
            childCode.setVisibility(View.VISIBLE);
        }
        else
            childCode.setVisibility(View.GONE);

        if(model.getChildType()==0){
            childType.setVisibility(View.GONE);
        }
        else{
            childType.setVisibility(View.VISIBLE);
            childType.setText(type[model.getChildType()-1]);
        }

        childUsername.setText(model.getChildUsername());
        childPost.setText(model.getChildContent());
        Reusable.applyLinkfy(context, model.getChildContent(), childPost);
        FirebaseUtil.getFirebaseFirestore().collection("posts").document(model.getChildLink()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                holder.imgChildStatus.setVisibility(documentSnapshot.toObject(Post.class).getStatus()==1? View.INVISIBLE: View.VISIBLE);
            }
        });


        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getChildUserId()))
                .signature(new ObjectKey(model.getChildUserId()+"_"+Reusable.getSignature()))
                .into(childDp);

        //listen to dp click and open user profile
        childDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getChildUserId().equals(userId)){
                    context.startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getChildUserId());
                    context.startActivity(intent);
                }
            }
        });
        //listen to username click and open user profile
        childUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getChildUserId().equals(userId)){
                    context.startActivity(new Intent(context, MyProfileActivity.class));
                }
                else{
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra("userId", model.getChildUserId());
                    context.startActivity(intent);
                }
            }
        });
    }

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
    private void displayOverflow(final Post model, String userID, final String postId, int status, int type, ImageView imgOverflow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView;
        if(userID.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                    Log.i(TAG, "onClick: "+ model.getType());
                    if(model.getType()>0)
                        calculations.onDeletePost(imgOverflow, postId, userId,status==2, type);
                    else {
                        FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId).delete();
                        Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show();
                    }
                    dialog.cancel();
                }
            }
        });

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

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userID)? "UNFOLLOW": "FOLLOW");

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
        View view = null;
        if(viewType==BANKER_POST)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_empty, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp, childDp;
        LinearLayout lnrContainer, lnrChildContainer;
        CardView crdChildPost;
        TextView mpost, childPost;
        TextView mUsername, childUsername;
        TextView mTime;
        TextView mLikes, mDislikes, mComment, mCode, mType, childCode, childType;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgComment, imgRepost, imgStatus, imgChildStatus;

        PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            childDp = itemView.findViewById(R.id.childDp);
            crdChildPost = itemView.findViewById(R.id.crdChildPost);
            lnrContainer = itemView.findViewById(R.id.container_post);
            lnrChildContainer = itemView.findViewById(R.id.container_child_post);

            mpost = itemView.findViewById(R.id.txtPost);
            childPost = itemView.findViewById(R.id.txtChildPost);
            mUsername = itemView.findViewById(R.id.txtUsername);
            childUsername = itemView.findViewById(R.id.txtChildUsername);
            mTime = itemView.findViewById(R.id.txtTime);

            mLikes = itemView.findViewById(R.id.txtLike);
            mDislikes = itemView.findViewById(R.id.txtDislike);
            mComment = itemView.findViewById(R.id.txtComment);
            mCode = itemView.findViewById(R.id.txtCode);
            mType = itemView.findViewById(R.id.txtPostType);
            childCode = itemView.findViewById(R.id.txtChildCode);
            childType = itemView.findViewById(R.id.txtChildType);

            imgLikes = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgRepost = itemView.findViewById(R.id.imgRepost);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
            imgChildStatus = itemView.findViewById(R.id.imgChildStatus);
        }
    }

}