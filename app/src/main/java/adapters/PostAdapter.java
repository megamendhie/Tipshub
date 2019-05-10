package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.LoginActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Post;
import models.UserNetwork;
import utils.Calculations;
import utils.Reusable;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    Reusable reusable = new Reusable();
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private FirebaseFirestore database;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws"};


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
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Post model) {
        Log.i(TAG, "onBindViewHolder: executed");
        final LinearLayout lnrCode = holder.lnrCode;
        final LinearLayout lnrContainer = holder.lnrContainer;
        final LinearLayout lnrChildContainer = holder.lnrChildContainer;
        final CardView crdChildPost = holder.crdChildPost;
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
        final ImageView imgCode = holder.imgCode;
        final ImageView imgComment = holder.imgComment;
        final ImageView imgShare = holder.imgShare;
        final ImageView imgOverflow = holder.imgOverflow;
        final String postId = getSnapshots().getSnapshot(position).getId();

        mUsername.setText(model.getUsername());
        imgStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        crdChildPost.setVisibility(model.isHasChild()? View.VISIBLE: View.GONE);
        if(model.getBookingCode()!=null && !model.getBookingCode().isEmpty()){
            mCode.setText(model.getBookingCode() + " @" + code[(model.getRecommendedBookie()-1)]);
            mCode.setVisibility(View.VISIBLE);
            imgCode.setVisibility(View.VISIBLE);
            lnrCode.setVisibility(View.VISIBLE);
        }
        else{
            lnrCode.setVisibility(View.GONE);
            mCode.setVisibility(View.GONE);
            imgCode.setVisibility(View.GONE);
        }
        if(model.getType()==0){
            mType.setVisibility(View.GONE);
        }
        else{
            mType.setVisibility(View.VISIBLE);
            mType.setText(type[model.getType()-1]);
        }
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
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        imgLikes.setColorFilter(model.getLikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        imgDislikes.setColorFilter(model.getDislikes().contains(userId)?
                context.getResources().getColor(R.color.colorPrimary): context.getResources().getColor(R.color.likeGrey));

        mComment.setText(model.getCommentsCount()==0? "":String.valueOf(model.getCommentsCount()));
        mLikesCount.setText(model.getLikesCount()==0? "":String.valueOf(model.getLikesCount()));
        mDislikesCount.setText(model.getDislikesCount()==0? "":String.valueOf(model.getDislikesCount()));

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reusable.shareTips(activity, model.getUsername(), model.getContent());
            }
        });

        lnrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });
        imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullPostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });
        imgLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onLike(postId, userId, model.getUserId());
            }
        });

        imgDislikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Key is " + postId);
                Calculations calculations = new Calculations(context);
                calculations.onDislike( postId, userId);
            }
        });
        imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOverflow(model, model.getUserId(), postId, model.getStatus(), model.getType(), imgOverflow);
            }
        });
        lnrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(model.isHasChild()){
            displayChildContent(model, holder);
        }
    }

    private void displayChildContent(final Post model, final PostHolder holder) {
        final LinearLayout lnrChildCode = holder.lnrChildCode;
        final TextView childPost= holder.childPost;
        final TextView childUsername = holder.childUsername;
        final TextView childCode = holder.childCode, childType = holder.childType;
        final CircleImageView childDp = holder.childDp;
        final ImageView imgChildStatus = holder.imgChildStatus, imgChildCode = holder.imgChildCode;

        imgChildStatus.setVisibility(model.getStatus()==1? View.GONE: View.VISIBLE);
        if(model.getChildBookingCode()!=null && !model.getChildBookingCode().isEmpty()){
            childCode.setText(model.getChildBookingCode() + " @" + code[(model.getChildBookie()-1)]);
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
        database.collection("posts").document(model.getChildLink()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                imgChildStatus.setVisibility(documentSnapshot.toObject(Post.class).getStatus()==1? View.GONE: View.VISIBLE);
            }
        });

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

    private void displayOverflow(final Post model, String userId, final String postId, int status, int type, ImageView imgOverflow) {
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

        Button btnSubmit, btnDelete, btnShare, btnRepost, btnFollow, btnSubscribe, btnObject;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnDelete = dialog.findViewById(R.id.btnDelete);
        btnShare = dialog.findViewById(R.id.btnShare);
        btnRepost = dialog.findViewById(R.id.btnRepost);
        btnFollow = dialog.findViewById(R.id.btnFollow);
        btnSubscribe = dialog.findViewById(R.id.btnSubscribe);

        if(UserNetwork.getFollowing()==null)
            btnFollow.setVisibility(View.GONE);
        else
            btnFollow.setText(UserNetwork.getFollowing().contains(this.userId)? "UNFOLLOW": "FOLLOW");
        btnRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RepostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("model", model);
                context.startActivity(intent);
                dialog.cancel();
            }
        });
    }

    private void popUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You must login first")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, LoginActivity.class));
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

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new PostHolder(view);
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
        ImageView imgLikes, imgDislike, imgComment, imgShare, imgStatus, imgCode, imgChildStatus, imgChildCode;
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
            imgShare = itemView.findViewById(R.id.imgShare);
            imgCode = itemView.findViewById(R.id.imgCode);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgOverflow = itemView.findViewById(R.id.imgOverflow);
            imgChildCode = itemView.findViewById(R.id.imgChildCode);
            imgChildStatus = itemView.findViewById(R.id.imgChildStatus);
        }
    }
}