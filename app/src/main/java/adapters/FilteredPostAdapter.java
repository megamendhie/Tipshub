package adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.sqube.tipshub.FlagActivity;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.SnapId;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

public class FilteredPostAdapter extends RecyclerView.Adapter<FilteredPostAdapter.PostHolder> {
    private final String TAG = "PostAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private ListenerRegistration listener;
    private boolean search;
    private Calculations calculations;
    private RequestOptions requestOptions = new RequestOptions();
    private ArrayList<Post> postList;
    private ArrayList<SnapId> snapIds;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public FilteredPostAdapter(boolean search, String userID, Activity activity, Context context, ArrayList<Post> postList,
                               ArrayList<SnapId> snapIds) {
        Log.i(TAG, "PostAdapter: created");
        this.search = search;
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.postList = postList;
        this.snapIds = snapIds;
        this.calculations = new Calculations(context);
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        CollectionReference collectionReference = FirebaseUtil.getFirebaseFirestore().collection("posts");
        long time = new Date().getTime();

        if(search)
            listener = collectionReference.orderBy("time").startAt(new Date().getTime())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots==null)
                        return;
                    for(DocumentChange change: queryDocumentSnapshots.getDocumentChanges()){
                        if(change.getType()== DocumentChange.Type.ADDED){
                            Log.i(TAG, "onEvent: added again "+ time);
                            Post post = change.getDocument().toObject(Post.class);
                            if(!post.getUserId().equals(userId))
                                if(UserNetwork.getFollowing()==null|| !UserNetwork.getFollowing().contains(post.getUserId()))
                                    return;
                            postList.add(0, post);
                            snapIds.add(0, new SnapId(change.getDocument().getId(), post.getTime()) );
                            FilteredPostAdapter.this.notifyDataSetChanged();
                        }
                    }
                }
            });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if(search)
            listener.remove();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.snapIds.size();
    }

    private void displayChildContent(final Post model, final PostHolder holder) {
        final LinearLayout lnrChildCode = holder.lnrChildCode;
        final TextView childPost= holder.childPost;
        final TextView childUsername = holder.childUsername;
        final TextView childCode = holder.childCode, childType = holder.childType;
        final CircleImageView childDp = holder.childDp;
        final ImageView imgChildCode = holder.imgChildCode;

        //holder.imgChildStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getChildBookingCode()!=null && !model.getChildBookingCode().isEmpty()){
            childCode.setText(String.format(Locale.ENGLISH, "%s @%s",
                    model.getBookingCode(), code[(model.getChildBookie()-1)]));
            childCode.setVisibility(View.VISIBLE);
            imgChildCode.setVisibility(View.VISIBLE);
            lnrChildCode.setVisibility(View.VISIBLE);
        }
        else{
            lnrChildCode.setVisibility(View.GONE);
            childCode.setVisibility(View.GONE);
            imgChildCode.setVisibility(View.GONE);
        }
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
                .load(FirebaseUtil.getStorageReference().child(model.getChildUserId()))
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
        if(model.getUserId().equals(userId)&& timeDifference > 144000000)
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        Log.i(TAG, "onBindViewHolder: executed");
        final String postId = snapIds.get(position).getId();
        Post model = postList.get(holder.getAdapterPosition());

        FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Log.i(TAG, "onEvent: pos= " + position);
                        if(documentSnapshot==null||!documentSnapshot.exists()){
                            if(postList.size()>position){
                                postList.remove(position);
                                snapIds.remove(position);
                                FilteredPostAdapter.this.notifyDataSetChanged();
                            }
                        }
                        else {
                            Post model = documentSnapshot.toObject(Post.class);
                            holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);

                            holder.imgLikes.setColorFilter(model.getLikes().contains(userId)?
                                    context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

                            holder.imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                                    context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

                            holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));

                            holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));

                            holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));
                            postList.set(position, model);
                        }
                    }
                });

        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);

        holder.imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));

        holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));

        holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        holder.mUsername.setText(model.getUsername());
        holder.crdChildPost.setVisibility(model.isHasChild()? View.VISIBLE: View.GONE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            holder.mCode.setText(String.format(Locale.ENGLISH, "%s @%s",
                    model.getBookingCode(), code[(model.getRecommendedBookie()-1)]));
            holder.mCode.setVisibility(View.VISIBLE);
            holder.imgCode.setVisibility(View.VISIBLE);
            holder.lnrCode.setVisibility(View.VISIBLE);
        }
        else{
            holder.lnrCode.setVisibility(View.GONE);
            holder.mCode.setVisibility(View.GONE);
            holder.imgCode.setVisibility(View.GONE);
        }
        if(model.getType()==0){
            holder.mType.setVisibility(View.GONE);
        }
        else{
            holder.mType.setVisibility(View.VISIBLE);
            holder.mType.setText(type[model.getType()-1]);
        }

        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(FirebaseUtil.getStorageReference().child(model.getUserId()))
                .into(holder.imgDp);

        //listen to dp click and open user profile
        holder.imgDp.setOnClickListener(new View.OnClickListener() {
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
        holder.mUsername.setOnClickListener(new View.OnClickListener() {
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

        holder.mpost.setText(model.getContent());
        Reusable.applyLinkfy(context, model.getContent(), holder.mpost);
        holder.mTime.setText(Reusable.getTime(model.getTime()));
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
                List<String> l = model.getLikes();
                if(model.getDislikes().contains(userId)){
                    holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                    holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");


                    //get list of userIds that disliked
                    List<String> dl = model.getDislikes();
                    dl.remove(userId);
                    l.add(userId);
                    model.setDislikes(dl);
                    model.setLikes(l);
                    model.setLikesCount(model.getLikesCount()+1);
                    model.setDislikesCount(model.getDislikesCount()-1);
                }
                else{
                    //get list of userIds that liked
                    if(model.getLikes().contains(userId)){
                        holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        holder.mLikes.setText(model.getLikesCount()-1>0?String.valueOf(model.getLikesCount()-1):"");

                        l.remove(userId);
                        model.setLikesCount(model.getLikesCount()-1);
                    }
                    else{
                        holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));

                        l.add(userId);
                        model.setLikesCount(model.getLikesCount()+1);
                    }
                    model.setLikes(l);
                }
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onLike(postId, userId, model.getUserId(), substring);
            }
        });

        holder.imgDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> dl = model.getDislikes();
                if(model.getLikes().contains(userId)){
                    holder.imgLikes.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                    holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGold));
                    holder.mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
                    holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));


                    //get list of userIds that liked
                    List<String> l = model.getLikes();
                    l.remove(userId);
                    dl.add(userId);
                    model.setLikes(l);
                    model.setDislikes(dl);
                    model.setLikesCount(model.getLikesCount()-1);
                    model.setDislikesCount(model.getDislikesCount()+1);

                }
                else{
                    //get list of userIds that disliked
                    if(model.getDislikes().contains(userId)){
                        holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGrey));
                        holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");

                        dl.remove(userId);
                        model.setDislikesCount(model.getDislikesCount()-1);
                    }
                    else{
                        holder.imgDislike.setColorFilter(context.getResources().getColor(R.color.likeGold));
                        holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));

                        dl.add(userId);
                        model.setDislikesCount(model.getDislikesCount()+1);
                    }
                    model.setDislikes(dl);
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

        holder.lnrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(model.isHasChild()){
            displayChildContent(model, holder);
        }
    }


    public class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp, childDp;
        LinearLayout lnrCode, lnrContainer,  lnrChildCode, lnrChildContainer;
        CardView crdChildPost;
        TextView mpost, childPost;
        TextView mUsername, childUsername;
        TextView mTime;
        TextView mLikes, mDislikes, mComment, mCode, mType, childCode, childType;
        ImageView imgOverflow;
        ImageView imgLikes, imgDislike, imgComment, imgRepost, imgStatus, imgCode, imgChildStatus, imgChildCode;
        public PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            childDp = itemView.findViewById(R.id.childDp);
            crdChildPost = itemView.findViewById(R.id.crdChildPost);
            lnrCode = itemView.findViewById(R.id.lnrCode);
            lnrContainer = itemView.findViewById(R.id.container_post);
            lnrChildCode = itemView.findViewById(R.id.lnrChildCode);
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
            imgCode = itemView.findViewById(R.id.imgCode);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
            imgChildCode = itemView.findViewById(R.id.imgChildCode);
            imgChildStatus = itemView.findViewById(R.id.imgChildStatus);
        }
    }

}