package adapters;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

import static utils.Reusable.getPlaceholderImage;

public class BankerTipsterHolder extends RecyclerView.ViewHolder {
    CircleImageView imgDp;
    private TextView txtUsername;

    BankerTipsterHolder(@NonNull View itemView) {
        super(itemView);
        imgDp = itemView.findViewById(R.id.imgDp);
        txtUsername = itemView.findViewById(R.id.txtUsername);
    }

    public void setDisplay(ProfileShort profile){
        txtUsername.setText(profile.getA2_username());
        try {
            Glide.with(imgDp.getContext())
                    .load(profile.getB2_dpUrl())
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(profile.getA_userId().charAt(0))).into(imgDp);
        }
        catch (Exception e){
            Log.w("{PeopleAdapter", "onBindViewHolder: " + e.getMessage());
        }

    }
}
