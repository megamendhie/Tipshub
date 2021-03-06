package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

import static utils.Reusable.getPlaceholderImage;

public class NotificationAdapter extends FirestoreRecyclerAdapter<Notification, NotificationAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Context context;
    private String userId;
    private StorageReference storageReference;

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
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Notification model) {
        Log.i(TAG, "onBindViewHolder: executed");

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

        imgType.setVisibility(View.VISIBLE);
        switch (model.getAction()) {
            case "liked":
                Glide.with(context).load(R.drawable.ic_thumb_up_color_24dp).into(imgType);
                break;
            case "disliked":
                Glide.with(context).load(R.drawable.ic_thumb_down_color_24dp).into(imgType);
                break;
            case "reposted":
                Glide.with(context).load(R.drawable.ic_retweet_color).into(imgType);
                break;
            case "subEnd":
            case "subscribed":
                Glide.with(context).load(R.drawable.ic_favorite_color_24dp).into(imgType);
                break;
            default:
                imgType.setVisibility(View.INVISIBLE);
                break;
        }

        if(model.getSentFrom().equals(Calculations.TIPSHUB))
            GlideApp.with(context).load(R.drawable.icn_mid).into(imgDp);
        else
            GlideApp.with(context).load(storageReference.child(model.getSentFrom()))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(model.getSentFrom().charAt(0)))
                .signature(new ObjectKey(model.getSentFrom()+"_"+Reusable.getSignature()))
                .into(imgDp);

        lnrContainer.setOnClickListener(v -> {
            if(model.getIntentUrl().equals(Calculations.TIPSHUB))
                return;
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
        });

        imgDp.setOnClickListener(v -> {
            if(model.getSentFrom().equals(Calculations.TIPSHUB))
                return;
            if(model.getSentFrom().equals(userId)){
                context.startActivity(new Intent(context, MyProfileActivity.class));
            }
            else{
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("userId", model.getSentFrom());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view, parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder {
        private ImageView imgType;
        private CircleImageView imgDp;
        private LinearLayout lnrContainer;
        private TextView mTitle, mMessage, mTime;
        private PostHolder(View itemView) {
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