package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Notification;
import services.GlideApp;
import utils.Calculations;
import utils.Reusable;

public class NotificationAdapter extends FirestoreRecyclerAdapter<Notification, NotificationAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();

    public NotificationAdapter(Query query, String userID, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.context = context;
        this.userId = userID;
        Calculations calculations = new Calculations(context);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        requestOptions.placeholder(R.drawable.dummy);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Notification model) {
        Log.i(TAG, "onBindViewHolder: executed");
        final String notificationId = getSnapshots().getSnapshot(position).getId();

        final LinearLayout lnrContainer = holder.lnrContainer;
        final ImageView imgType = holder.imgType;
        final CircleImageView imgDp = holder.imgDp;
        final TextView mTitle= holder.mTitle;
        final TextView mMessage = holder.mMessage;
        final TextView mTime = holder.mTime;
        final String intentUrl = model.getIntentUrl();

        mTime.setText(Reusable.getTime(model.getTime()));
        mTitle.setText(model.getTitle());
        mMessage.setText(model.getMessage());

        switch (model.getAction()) {
            case "liked":
                imgType.setImageResource(R.drawable.ic_thumb_up_color_24dp);
                break;
            case "disliked":
                imgType.setImageResource(R.drawable.ic_thumb_down_color_24dp);
                break;
            case "subEnd":
            case "subscribed":
                imgType.setImageResource(R.drawable.ic_favorite_color_24dp);
                break;
        }

        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getSentFrom()))
                .into(imgDp);

        lnrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                switch (model.getType()){
                    case "comment":
                    case "post":
                        intent = new Intent(context, FullPostActivity.class);
                        intent.putExtra("postId", intentUrl);
                        context.startActivity(intent);
                        break;
                    case "following":
                    case "subscription":
                        if(model.getSentFrom().equals(userId)){
                            context.startActivity(new Intent(context, MyProfileActivity.class));
                        }
                        else{
                            intent = new Intent(context, MemberProfileActivity.class);
                            intent.putExtra("userId", model.getSentFrom());
                            context.startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view1, parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder {
        ImageView imgType;
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mTitle, mMessage, mTime;
        PostHolder(View itemView) {
            super(itemView);
            imgType = itemView.findViewById(R.id.imgType);
            imgDp = itemView.findViewById(R.id.imgDp);
            lnrContainer = itemView.findViewById(R.id.container);
            mMessage = itemView.findViewById(R.id.txtMessage);
            mTitle = itemView.findViewById(R.id.txtTitle);
            mTime = itemView.findViewById(R.id.txtTime);
        }
    }
}