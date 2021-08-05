package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.R;

import models.Subscription;
import services.GlideApp;
import utils.Reusable;

import static utils.Reusable.getPlaceholderImage;

public class SubscriberAdapter extends FirestoreRecyclerAdapter<Subscription, SubscriptionAdapter.PostHolder>{
    private final String TAG = "PostAdaper";
    private Context context;
    private StorageReference storageReference;
    private String[] status = {"", "pending", "PAID"};

    public SubscriberAdapter(Query query, Context context) {
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
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        int i = getItemCount();
        Log.i(TAG, "SubscriberAdapter: i =" + i);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull SubscriptionAdapter.PostHolder holder, final int position, @NonNull final Subscription model) {
        holder.mUsername.setText(model.getSubFrom());
        holder.mStartDate.setText(Reusable.getNewDate(model.getDateStart()));
        holder.mEndDate.setText(Reusable.getNewDate(model.getDateEnd()));
        holder.mAmount.setText(Html.fromHtml(model.getTipsterAmount()));
        if(model.getStatus() < status.length)
            holder.mStatus.setText(status[model.getStatus()]);

        GlideApp.with(context).load(storageReference.child(model.getSubFromId()))
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(model.getSubFromId().charAt(0)))
                .into(holder.imgDp);

        holder.mUsername.setOnClickListener(v -> {
            Intent intent = new Intent(context, MemberProfileActivity.class);
            intent.putExtra("userId", model.getSubFromId());
            context.startActivity(intent);
        });
        holder.imgDp.setOnClickListener(v -> {
            Intent intent = new Intent(context, MemberProfileActivity.class);
            intent.putExtra("userId", model.getSubFromId());
            context.startActivity(intent);
        });
    }


    @Override
    public SubscriptionAdapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionAdapter.PostHolder(view);
    }

}