package adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sqube.tipshub.LoginActivity;
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

import static utils.Reusable.getPlaceholderImage;

public class PeopleRecAdapter extends RecyclerView.Adapter<PeopleRecAdapter.PeopleHolder> {
    private final String TAG = "PplAdapter";
    private Context context;
    private String userId;
    private ArrayList<String> list;

    public PeopleRecAdapter(Context context, String userId,  ArrayList<String> list){
        Log.i(TAG, "PeopleRecAdapter: called");
        this.context = context;
        this.userId = userId;
        this.list = list;
    }

    @NonNull
    @Override
    public PeopleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new PeopleHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull PeopleHolder holder, int i) {
        String ref = list.get(i);
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(ref).get()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()||task.isCanceled() || !task.getResult().exists()){
                        list.remove(i);
                        PeopleRecAdapter.this.notifyDataSetChanged();
                        return;
                    }
                    Log.i(TAG, "onComplete: ");
                    ProfileShort model = task.getResult().toObject(ProfileShort.class);
                    holder.mUsername.setText(model.getA2_username());

                    String tips = model.getE0a_NOG()>1? "tips": "tip";
                    holder.mPost.setText(String.format(Locale.getDefault(),"%d  %s  • ", model.getE0a_NOG(), tips));
                    holder.mAccuracy.setText(String.format(Locale.getDefault(),"%.1f%%", (double) model.getE0c_WGP()));
                    holder.btnFollow.setText(UserNetwork.getFollowing()==null||!UserNetwork.getFollowing().contains(ref)? "FOLLOW": "FOLLOWING");

                    Glide.with(holder.imgDp.getContext()).load(model.getB2_dpUrl())
                            .placeholder(R.drawable.dummy)
                            .error(getPlaceholderImage(ref.charAt(0)))
                            .into(holder.imgDp);

                    holder.lnrContainer.setOnClickListener(v -> {
                        if(ref.equals(userId)){
                            holder.lnrContainer.getContext().startActivity(new Intent(context, MyProfileActivity.class));
                        }
                        else{
                            Intent intent = new Intent(context, MemberProfileActivity.class);
                            intent.putExtra("userId", ref);
                            holder.lnrContainer.getContext().startActivity(intent);
                        }
                    });

                    holder.btnFollow.setOnClickListener(v -> {
                        if(!Reusable.getNetworkAvailability(context)){
                            Snackbar.make(holder.btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(R.id.bottom_navigation).show();
                            return;
                        }

                        if (userId.equals(Calculations.GUEST)) {
                            loginPrompt(holder.btnFollow);
                            return;
                        }
                        if(holder.btnFollow.getText().toString().toUpperCase().equals("FOLLOW")){
                            Calculations calculations= new Calculations(context);
                            calculations.followMember(holder.imgDp, userId, ref, true);
                            holder.btnFollow.setText("FOLLOWING");
                        }
                        else
                            unfollowPrompt(holder.btnFollow, ref, model.getA2_username());
                    });
                });
    }

    private void loginPrompt(View view) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(view.getRootView().getContext(), R.style.CustomMaterialAlertDialog);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .setPositiveButton("Login", (dialogInterface, i) -> view.getContext().startActivity(new Intent(view.getContext(), LoginActivity.class)))
                .show();
    }

    private void unfollowPrompt(TextView btnFollow, String userID, String username){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog);
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No", (dialogInterface, i) -> {
                    //do nothing
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Calculations calculations= new Calculations(context);
                    calculations.unfollowMember(btnFollow, userId, userID, true);
                    btnFollow.setText("FOLLOW");
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PeopleHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDp;
        LinearLayout lnrContainer;
        TextView mUsername, mPost, mAccuracy;
        TextView btnFollow;
        PeopleHolder(View itemView) {
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