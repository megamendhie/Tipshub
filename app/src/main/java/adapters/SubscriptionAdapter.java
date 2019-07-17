package adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Subscription;
import services.GlideApp;

public class SubscriptionAdapter extends FirestoreRecyclerAdapter<Subscription, SubscriptionAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private FirebaseFirestore database;
    private String[] status = {"", "PENDING", "PAID"};

    public SubscriptionAdapter(Query query, String userID, Activity activity, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Subscription>()
                .setQuery(query, Subscription.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.activity = activity;
        this.context = context;
        this.userId = userID;
        this.database = FirebaseFirestore.getInstance();
        requestOptions.placeholder(R.drawable.dummy);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        int i = getItemCount();
        Log.i(TAG, "SubscriptionAdapter: i =" + i);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, final int position, @NonNull final Subscription model) {
        holder.mUsername.setText(model.getSubTo());
        holder.mStartDate.setText(model.getDateStart());
        holder.mEndDate.setText(model.getDateEnd());
        holder.mAmount.setText(Html.fromHtml(model.getAmount()));
        holder.mStatus.setText(status[model.getStatus()]);
        holder.mPosition.setText(String.valueOf(position+1));
        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getSubToId()))
                .into(holder.imgDp);

        holder.imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("userId", model.getSubToId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_container, parent, false);
        return new PostHolder(view);
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        TextView mUsername, mStartDate, mEndDate, mAmount, mStatus, mPosition;
        public PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mAmount = itemView.findViewById(R.id.txtAmount);
            mStartDate = itemView.findViewById(R.id.txtStarDate);
            mEndDate = itemView.findViewById(R.id.txtEndDate);
            mStatus = itemView.findViewById(R.id.txtStatus);
            mPosition = itemView.findViewById(R.id.txtPosition);
        }
    }
}