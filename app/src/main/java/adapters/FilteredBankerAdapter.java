package adapters;

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
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
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
import java.util.Locale;

import models.Post;
import models.SnapId;
import models.UserNetwork;
import services.GlideApp;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

import static utils.Reusable.getPlaceholderImage;
import static views.DislikeButton.DISLIKED;
import static views.DislikeButton.NOT_DISLIKED;
import static views.LikeButton.LIKED;
import static views.LikeButton.NOT_LIKED;

public class FilteredBankerAdapter extends RecyclerView.Adapter<BankerPostHolder> {
    private final String TAG = "PostAdapter";
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private Calculations calculations;
    private ArrayList<Post> postList;
    private ArrayList<SnapId> snapIds;

    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public FilteredBankerAdapter(String userID, Context context, ArrayList<Post> postList, ArrayList<SnapId> snapIds) {
        Log.i(TAG, "PostAdapter: created");
        this.context = context;
        this.userId = userID;
        this.postList = postList;
        this.snapIds = snapIds;
        this.calculations = new Calculations(context);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(imgOverflow.getRootView().getContext());
        LayoutInflater inflater = LayoutInflater.from(imgOverflow.getRootView().getContext());
        View dialogView;
        if(userID.equals(this.userId))
            dialogView = inflater.inflate(R.layout.dialog_mine, null);
        else
            dialogView = inflater.inflate(R.layout.dialog_member, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button btnSubmit, btnDelete, btnRepost, btnFollow;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnRepost = dialog.findViewById(R.id.btnRepost);
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
            btnRepost.setVisibility(View.GONE);
        }

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userID)? "UNFOLLOW": "FOLLOW");

        btnDelete.setOnClickListener(v -> {
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
        });

        btnRepost.setOnClickListener(v -> {
            Intent intent = new Intent(context, RepostActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("model", model);
            btnRepost.getContext().startActivity(intent);
            dialog.cancel();
        });

        btnFollow.setOnClickListener(v -> {
            if(!Reusable.getNetworkAvailability(context)){
                Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                dialog.cancel();
                return;
            }
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, userID);
            }
            else
                unfollowPrompt(imgOverflow, userID, model.getUsername());
            dialog.cancel();
        });
    }

    private void unfollowPrompt(ImageView imgOverflow, String userID, String username){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        calculations.unfollowMember(imgOverflow, userId, userID);
                    }
                })
                .show();
    }

    @Override
    public BankerPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new BankerPostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankerPostHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        Post model = postList.get(position);
        final String postId = snapIds.get(position).getId();

        holder.mUsername.setText(model.getUsername());
        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
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
                .load(storageReference.child(model.getUserId()))
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(model.getUserId().charAt(0)))
                .signature(new ObjectKey(model.getUserId()+"_"+Reusable.getSignature()))
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
        holder.imgLikes.setState(model.getLikes().contains(userId)? LIKED: NOT_LIKED);

        holder.imgDislike.setState(model.getDislikes().contains(userId)? DISLIKED: NOT_DISLIKED);

        holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        final boolean finalMakePublic = (model.getStatus()==2 || (new Date().getTime() - model.getTime()) >(18*60*60*1000));

        holder.imgShare.setOnClickListener(v -> {
            if(!finalMakePublic) {
                Snackbar.make(holder.mComment, holder.imgShare.getContext().getResources().getString(R.string.str_cannot_share_post), Snackbar.LENGTH_SHORT).show();
                return;
            }
            Reusable.shareTips(holder.imgShare.getContext(), model.getUsername(), model.getContent());
        });

        holder.lnrContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        holder.imgComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        holder.imgLikes.setOnClickListener(v -> {
            if(!Reusable.getNetworkAvailability(context))
                return;
            List<String> l = model.getLikes();
            if(holder.imgDislike.getState()==DISLIKED){
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
                if(holder.imgLikes.getState()==LIKED){
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
        });

        holder.imgDislike.setOnClickListener(v -> {
            if(!Reusable.getNetworkAvailability(context))
                return;
            List<String> dl = model.getDislikes();
            if(holder.imgLikes.getState()==LIKED){
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
                if(holder.imgDislike.getState()==DISLIKED){
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
        });

        holder.imgOverflow.setOnClickListener(v -> displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), holder.imgOverflow, finalMakePublic));
    }

}