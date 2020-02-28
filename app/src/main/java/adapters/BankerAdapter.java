package adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.sqube.tipshub.LoginActivity;
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

import static views.DislikeButton.DISLIKED;
import static views.DislikeButton.NOT_DISLIKED;
import static views.LikeButton.LIKED;
import static views.LikeButton.NOT_LIKED;

public class BankerAdapter extends FirestoreRecyclerAdapter<Post, BankerPostHolder>{
    private final String TAG = "BankerAdaper";
    private Context context;
    private String userId;
    private Calculations calculations;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public BankerAdapter(Query query, String userID, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build());

        Log.i(TAG, "BankerAdapter: created");
        this.context = context;
        this.setUserId(userID);
        this.calculations = new Calculations(context);
        requestOptions.placeholder(R.drawable.ic_person_outline_black_24dp);
        storageReference = FirebaseUtil.getFirebaseStorage().getReference().child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull final BankerPostHolder holder, final int position, @NonNull final Post model) {
        Log.i(TAG, "onBindViewHolder: executed");
        boolean makeVisible = false, makePublic = false;
        final String postId = getSnapshots().getSnapshot(position).getId();

        holder.mUsername.setText(model.getUsername());
        holder.imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            holder.mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
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
            holder.mpost.setMaxLines(6);
            holder.mSub.setText("Subscribe to "+ model.getUsername());

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
        holder.imgLikes.setState(model.getLikes().contains(userId)? LIKED: NOT_LIKED);
        holder.imgDislike.setState(model.getDislikes().contains(userId)? DISLIKED: NOT_DISLIKED);

        holder.mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        holder.mLikes.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        holder.mDislikes.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        final boolean finalMakePublic = makePublic;
        final boolean finalMakeVisible = makeVisible;
        holder.imgRepost.setOnClickListener(v -> {
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(holder.imgRepost);
                return;
            }

            if(!finalMakePublic) {
                Snackbar.make(holder.mComment, "You can't repost yet", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, RepostActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("model", model);
            context.startActivity(intent);
        });

        holder.mpost.setOnClickListener(v -> {
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
        });

        holder.lnrContainer.setOnClickListener(v -> {
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
        });

        holder.imgComment.setOnClickListener(v -> {
            //display full post with comments if visibility or public is set true
            if(!finalMakePublic && !finalMakeVisible) {
                Snackbar.make(holder.mComment, "Access denied", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        holder.imgLikes.setOnClickListener(v -> {
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(holder.imgLikes);
                return;
            }

            model.getDislikes().contains(userId);
            if(model.getDislikes().contains(userId)){
                holder.imgLikes.setState(LIKED);
                holder.imgDislike.setState(NOT_LIKED);
                holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");
            }
            else{
                if(model.getLikes().contains(userId)){
                    holder.imgLikes.setState(NOT_LIKED);
                    holder.mLikes.setText(model.getLikesCount()-1>0?String.valueOf(model.getLikesCount()-1):"");
                }
                else{
                    holder.imgLikes.setState(LIKED);
                    holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                }
            }
            String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
            calculations.onLike(postId, userId, model.getUserId(), substring);
        });

        holder.imgDislike.setOnClickListener(v -> {
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(holder.imgDislike);
                return;
            }
            if(model.getLikes().contains(userId)){
                holder.imgLikes.setState(NOT_LIKED);
                holder.imgDislike.setState(DISLIKED);
                holder.mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
                holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
            }
            else{
                if(model.getDislikes().contains(userId)){
                    holder.imgDislike.setState(NOT_DISLIKED);
                    holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1): "");
                }
                else{
                    holder.imgDislike.setState(DISLIKED);
                    holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
                }
            }
            String substring = model.getContent().substring(0, Math.min(model.getContent().length(), 90));
            calculations.onDislike( postId, userId, model.getUserId(), substring);
        });

        holder.imgOverflow.setOnClickListener(v -> displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), holder.imgOverflow, finalMakePublic));
    }

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
                Reusable.shareTips(btnShare.getRootView().getContext(), model.getUsername(), model.getContent());
                dialog.cancel();
            }
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
                calculations.followMember(imgOverflow, userId, userID);
            }
            else
                unfollowPrompt(imgOverflow, userID, model.getUsername());
            dialog.cancel();
        });
    }

    private void loginPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext(),
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .setPositiveButton("Login", (dialogInterface, i) -> view.getRootView().getContext().startActivity(new Intent(view.getRootView().getContext(), LoginActivity.class)))
                .show();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view_banker, parent, false);
        return new BankerPostHolder(view);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}