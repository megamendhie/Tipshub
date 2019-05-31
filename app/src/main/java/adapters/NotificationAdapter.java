package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Notification;
import utils.Calculations;
import utils.Reusable;

public class NotificationAdapter extends FirestoreRecyclerAdapter<Notification, NotificationAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    Reusable reusable = new Reusable();
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    Calculations calculations;
    final int NORMAL_POST=1, BANKER_POST = 0;

    private FirebaseFirestore database;
    private String[] code = {"1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365"};
    private String[] type = {"3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip"};

    public NotificationAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.calculations = new Calculations(context);
        this.database = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images");
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

        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getTime()));
        mTitle.setText(model.getTitle());
        mMessage.setText(model.getMessage());

        lnrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (model.getType()){
                    case "comment":
                    case "post":
                        Intent intent = new Intent(context, FullPostActivity.class);
                        intent.putExtra("postId", intentUrl);
                        context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view1, parent, false);
        return new PostHolder(view);
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        ImageView imgType;
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mTitle, mMessage, mTime;
        public PostHolder(View itemView) {
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