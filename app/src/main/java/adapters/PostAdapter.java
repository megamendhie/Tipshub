package adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FlagActivity;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.LoginActivity;
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

import static utils.Reusable.getPlaceholderImage;
import static views.DislikeButton.DISLIKED;
import static views.DislikeButton.NOT_DISLIKED;
import static views.LikeButton.LIKED;
import static views.LikeButton.NOT_LIKED;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostHolder>{
    private final String TAG = "PostAdapter";
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private Calculations calculations;
    private final int NORMAL_POST=1, BANKER_POST = 0;
    private boolean anchorSnackbar;

    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public PostAdapter(FirestoreRecyclerOptions<Post> response, String userID, Context context, boolean anchorSnackbar) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(response);
        this.anchorSnackbar = anchorSnackbar;

        Log.i(TAG, "PostAdapter: created");
        this.context = context;
        this.setUserId(userID);
        this.calculations = new Calculations(context);
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
        final String postId = getSnapshots().getSnapshot(position).getId();
        holder.setPostId(postId);

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

        GlideApp.with(context).load(storageReference.child(model.getUserId()))
                .placeholder(getPlaceholderImage(model.getUserId().charAt(0)))
                .error(getPlaceholderImage(model.getUserId().charAt(0)))
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

        holder.imgShare.setOnClickListener(v -> {
            Reusable.shareTips( holder.imgShare.getContext(), model.getUsername(), model.getContent());
        });

        holder.mpost.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        holder.imgComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        holder.imgLikes.setOnClickListener(view -> {
            if (userId.equals(Calculations.GUEST)) {
                loginPrompt(holder.imgLikes);
                return;
            }
            if(holder.imgDislike.getState()==DISLIKED){
                holder.imgLikes.setState(LIKED);
                holder.imgDislike.setState(NOT_DISLIKED);
                holder.mLikes.setText(String.valueOf(model.getLikesCount()+1));
                holder.mDislikes.setText(model.getDislikesCount()-1>0? String.valueOf(model.getDislikesCount()-1):"");
            }
            else{
                if(holder.imgLikes.getState()==LIKED){
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
            if(holder.imgLikes.getState()==LIKED){
                holder.imgLikes.setState(NOT_LIKED);
                holder.imgDislike.setState(DISLIKED);
                holder.mLikes.setText(model.getLikesCount()-1>0? String.valueOf(model.getLikesCount()-1):"");
                holder.mDislikes.setText(String.valueOf(model.getDislikesCount()+1));
            }
            else{
                if(holder.imgDislike.getState()==DISLIKED){
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

        holder.imgOverflow.setOnClickListener(v -> {
            displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), holder.imgOverflow);});

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
                .addOnSuccessListener(documentSnapshot -> {
                    if(!documentSnapshot.exists())
                        return;
                    holder.imgChildStatus.setVisibility(documentSnapshot.toObject(Post.class).getStatus()==1?
                            View.INVISIBLE: View.VISIBLE);
                });

        GlideApp.with(context).load(storageReference.child(model.getChildUserId()))
                .placeholder(getPlaceholderImage(model.getChildUserId().charAt(0)))
                .error(getPlaceholderImage(model.getChildUserId().charAt(0)))
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

    private void loginPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext(),
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        view.getContext().startActivity(new Intent(view.getContext(), LoginActivity.class));
                    }
                })
                .show();
    }

    /*
        Displays overflow containing options like follow, subscribe, disagree, etc.
     */
    private void displayOverflow(final Post model, String userID, final String postId, int status, int type, ImageView imgOverflow) {
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
                Log.i(TAG, "onClick: "+ model.getType());
                if(model.getType()>0)
                    calculations.onDeletePost(imgOverflow, postId, userId,status==2, type, anchorSnackbar);
                else {
                    FirebaseUtil.getFirebaseFirestore().collection("posts").document(postId).delete();
                    Snackbar.make(imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show();
                }
                dialog.cancel();
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
                        .setPositiveButton("Yes", (dialogInterface, i) -> calculations.onPostWon(imgOverflow, postId, userId, type, anchorSnackbar))
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            //do nothing
                        })
                        .show();
            }
        });

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(userID)? "UNFOLLOW": "FOLLOW");

        btnRepost.setOnClickListener(v -> {
            if (userId.equals(Calculations.GUEST)) {
                dialog.cancel();
                loginPrompt(btnRepost);
                return;
            }
            Intent intent = new Intent(context, RepostActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("model", model);
            context.startActivity(intent);
            dialog.cancel();
        });

        btnFollow.setOnClickListener(v -> {
            if(!Reusable.getNetworkAvailability(context)){
                if(anchorSnackbar)
                    Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                            .setAnchorView(R.id.bottom_navigation).show();
                else
                    Snackbar.make(btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                dialog.cancel();
                return;
            }
            if(btnFollow.getText().equals("FOLLOW")){
                calculations.followMember(imgOverflow, userId, userID, anchorSnackbar);
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
                .setNegativeButton("No", (dialogInterface, i) -> {
                    //do nothing
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> calculations.unfollowMember(imgOverflow, userId, userID, anchorSnackbar))
                .show();
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

}