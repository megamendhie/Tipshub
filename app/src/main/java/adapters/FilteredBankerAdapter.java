package adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FlagActivity;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.SnapId;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.Reusable;

public class FilteredBankerAdapter extends RecyclerView.Adapter<FilteredBankerAdapter.PostHolder> {
    private final String TAG = "PostAdaper";
    Reusable reusable = new Reusable();
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    Calculations calculations;
    private RequestOptions requestOptions = new RequestOptions();
    ArrayList<Post> postList;
    private ArrayList<SnapId> snapIds;

    private FirebaseFirestore database;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public FilteredBankerAdapter(String userID, Activity activity, Context context, ArrayList<Post> postList, ArrayList<SnapId> snapIds) {
        Log.i(TAG, "PostAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.postList = postList;
        this.snapIds = snapIds;
        this.calculations = new Calculations(context);
        this.database = FirebaseFirestore.getInstance();
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
    }

    @Override
    public int getItemCount() {
        return snapIds.size();
    }

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
    private void displayOverflow(final Post model, String userID, final String postId, int status, int type, ImageView imgOverflow,
                                 final boolean makePublic) {
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
        btnSubscribe.setVisibility(View.GONE);

        if(!makePublic){
            btnShare.setVisibility(View.GONE);
        }

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userID)? "UNFOLLOW": "FOLLOW");

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
                        database.collection("posts").document(postId).delete();
                        Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show();
                    }
                }
                dialog.cancel();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reusable.shareTips(activity, model.getUsername(), model.getContent());
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
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        Post model = postList.get(position);
        Log.i(TAG, model.getUsername()+" "+ model.getContent());
        Log.i(TAG, "onBindViewHolder: executed");
        final LinearLayout lnrChildContainer = holder.lnrChildContainer;
        final String postId = snapIds.get(position).getId();

        holder.mUsername.setText(model.getUsername());
        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        holder.crdChildPost.setVisibility(model.isHasChild()? View.VISIBLE: View.GONE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            holder.mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
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
                .load(storageReference.child(model.getUserId()))
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

        holder.mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));

        holder.imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.imgDislike.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.likeGold): context.getResources().getColor(R.color.likeGrey));

        holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        final boolean finalMakePublic = (model.getStatus()==2 || (new Date().getTime() - model.getTime()) >(16*60*60*1000))? true: false;
        holder.imgRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!finalMakePublic) {
                    Snackbar.make(holder.mComment, "You can't repost yet", Snackbar.LENGTH_SHORT).show();
                    return;
                }
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
                if(!Reusable.getNetworkAvailability(context))
                    return;
                List<String> l = model.getLikes();
                if(model.getDislikes().contains(userId)){
                    //get list of userIds that disliked
                    List<String> dl = model.getDislikes();
                    dl.remove(userId);
                    l.add(userId);
                    postList.get(position).setDislikes(dl);
                    postList.get(position).setLikes(l);
                    postList.get(position).setLikesCount(model.getLikesCount()+1);
                    postList.get(position).setDislikesCount(model.getDislikesCount()-1);
                }
                else{
                    //get list of userIds that liked
                    if(model.getLikes().contains(userId)){
                        l.remove(userId);
                        postList.get(position).setLikesCount(model.getLikesCount()-1);
                    }
                    else{
                        l.add(userId);
                        postList.get(position).setLikesCount(model.getLikesCount()+1);
                    }
                    postList.get(position).setLikes(l);
                }
                FilteredBankerAdapter.this.notifyDataSetChanged();
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onLike(postId, userId, model.getUserId(), substring);
            }
        });

        holder.imgDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Reusable.getNetworkAvailability(context))
                    return;
                List<String> dl = model.getDislikes();
                if(model.getLikes().contains(userId)){
                    //get list of userIds that liked
                    List<String> l = model.getLikes();
                    l.remove(userId);
                    dl.add(userId);
                    postList.get(position).setLikes(l);
                    postList.get(position).setDislikes(dl);
                    postList.get(position).setLikesCount(model.getLikesCount()-1);
                    postList.get(position).setDislikesCount(model.getDislikesCount()+1);
                }
                else{
                    //get list of userIds that disliked
                    if(model.getDislikes().contains(userId)){
                        dl.remove(userId);
                        postList.get(position).setDislikesCount(model.getDislikesCount()-1);
                    }
                    else{
                        dl.add(userId);
                        postList.get(position).setDislikesCount(model.getDislikesCount()+1);
                    }
                    postList.get(position).setDislikes(dl);
                }
                FilteredBankerAdapter.this.notifyDataSetChanged();
                String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
                calculations.onDislike( postId, userId, model.getUserId(), substring);
            }
        });

        holder.imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), holder.imgOverflow, finalMakePublic);
            }
        });

        holder.lnrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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