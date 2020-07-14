package adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

import models.ProfileShort;
import utils.FirebaseUtil;

public class BankerTipsterAdapter extends FirestoreRecyclerAdapter<ProfileShort, BankerTipsterHolder> {
    private String myUserId;

    public BankerTipsterAdapter(@NonNull FirestoreRecyclerOptions<ProfileShort> options) {
        super(options);

        if(FirebaseUtil.getFirebaseAuthentication().getCurrentUser()!=null)
            myUserId = FirebaseUtil.getFirebaseAuthentication().getCurrentUser().getUid();
    }

    @Override
    protected void onBindViewHolder(@NonNull BankerTipsterHolder holder, int position, @NonNull ProfileShort model) {
        String userId = getSnapshots().getSnapshot(position).getId();
        holder.setDisplay(model);
        holder.imgDp.setOnClickListener(v -> {
            if(myUserId.equals(userId)){
                holder.imgDp.getContext().startActivity(new Intent(holder.imgDp.getContext(), MyProfileActivity.class));
            }
            else{
                Intent intent = new Intent(holder.imgDp.getContext(), MemberProfileActivity.class);
                intent.putExtra("userId", userId);
                holder.imgDp.getContext().startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public BankerTipsterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banker_tipster, parent, false);
        return new BankerTipsterHolder(view);
    }
}
