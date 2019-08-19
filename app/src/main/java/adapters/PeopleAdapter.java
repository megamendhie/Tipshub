package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;
import models.UserNetwork;
import utils.Calculations;
import utils.FirebaseUtil;
import utils.Reusable;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PostHolder> {
    private final String TAG = "PplAdaper";
    private Activity activity;
    private Context context;
    private String userId;
    private ArrayList<String> list;
    private StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();

    public PeopleAdapter(){}

    public PeopleAdapter(Activity activity, Context context, String userId, ArrayList<String> list){
        this.activity =activity;
        this.context = context;
        this.userId = userId;
        this.list = list;
        requestOptions.placeholder(R.drawable.dummy);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view_land, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int i) {
        String ref = list.get(i);
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(ref).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful() || !task.getResult().exists()){
                    list.remove(i);
                    PeopleAdapter.this.notifyDataSetChanged();
                    return;
                }
                ProfileShort model = task.getResult().toObject(ProfileShort.class);
                holder.mUsername.setText(model.getA2_username());
                String tips = model.getE0a_NOG()>1? "tips": "tip";
                holder.mPost.setText(String.format(Locale.getDefault(),"%d  %s  • ", model.getE0a_NOG(), tips));
                holder.mAccuracy.setText(String.format(Locale.getDefault(),"%.1f%%", (double) model.getE0c_WGP()));
                holder.btnFollow.setText(UserNetwork.getFollowing()==null||!UserNetwork.getFollowing().contains(ref)? "FOLLOW": "FOLLOWING");
                if(ref.equals(userId))
                    holder.btnFollow.setVisibility(View.GONE);

                //load image
                Glide.with(activity)
                        .setDefaultRequestOptions(requestOptions)
                        .load(model.getB2_dpUrl())
                        .into(holder.imgDp);

                holder.lnrContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ref.equals(userId)){
                            context.startActivity(new Intent(context, MyProfileActivity.class));
                        }
                        else{
                            Intent intent = new Intent(context, MemberProfileActivity.class);
                            intent.putExtra("userId", ref);
                            context.startActivity(intent);
                        }
                    }
                });

                holder.btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(UserNetwork.getFollowing()==null){
                            Snackbar.make(holder.btnFollow, "Can't follow this person now", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        Calculations calculations= new Calculations(context);
                        switch (holder.btnFollow.getText().toString().toLowerCase()){
                            case "follow":
                                calculations.followMember(holder.imgDp, userId, ref);
                                if(Reusable.getNetworkAvailability(activity)) {
                                    holder.btnFollow.setText("FOLLOWING");
                                }
                                break;
                            case "following":
                                calculations.unfollowMember(holder.imgDp, userId, ref);
                                if(Reusable.getNetworkAvailability(activity)) {
                                    holder.btnFollow.setText("FOLLOW");
                                }
                                break;
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mUsername, mPost, mAccuracy;
        Button btnFollow;
        public PostHolder(View itemView) {
            super(itemView);
            imgDp = itemView.findViewById(R.id.imgDp);
            lnrContainer = itemView.findViewById(R.id.lnrContainer);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mPost = itemView.findViewById(R.id.txtPost);
            mAccuracy = itemView.findViewById(R.id.txtAccuracy);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}