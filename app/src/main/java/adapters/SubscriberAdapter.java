package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.R;

import models.Subscription;
import services.GlideApp;

public class SubscriberAdapter extends FirestoreRecyclerAdapter<Subscription, SubscriptionAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Context context;
    private String userId;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private String[] status = {"", "PENDING", "PAID"};

    public SubscriberAdapter(Query query, String userID, Context context) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Post.class instructs the adapter to convert each DocumentSnapshot to a Post object
        */
        super(new FirestoreRecyclerOptions.Builder<Subscription>()
                .setQuery(query, Subscription.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.context = context;
        this.userId = userID;
        requestOptions.placeholder(R.drawable.dummy);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        int i = getItemCount();
        Log.i(TAG, "SubscriberAdapter: i =" + i);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull SubscriptionAdapter.PostHolder holder, final int position, @NonNull final Subscription model) {
        holder.mUsername.setText(model.getSubFrom());
        holder.mStartDate.setText(model.getDateStart());
        holder.mEndDate.setText(model.getDateEnd());
        holder.mAmount.setText(Html.fromHtml(model.getAmount()));
        holder.mStatus.setText(status[model.getStatus()]);
        holder.mPosition.setText(String.valueOf(position+1));
        GlideApp.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getSubFromId()))
                .into(holder.imgDp);

        holder.imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("userId", model.getSubFromId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public SubscriptionAdapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_container, parent, false);
        return new SubscriptionAdapter.PostHolder(view);
    }

}