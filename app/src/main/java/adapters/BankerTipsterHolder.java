package adapters;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;

public class BankerTipsterHolder extends RecyclerView.ViewHolder {
    CircleImageView imgDp;
    TextView txtUsername;
    private RequestOptions requestOptions = new RequestOptions();

    public BankerTipsterHolder(@NonNull View itemView) {
        super(itemView);
        imgDp = itemView.findViewById(R.id.imgDp);
        txtUsername = itemView.findViewById(R.id.txtUsername);
        requestOptions.placeholder(R.drawable.dummy);
    }

    public void setDisplay(ProfileShort profile){
        txtUsername.setText(profile.getA2_username());
        try {
            Glide.with(imgDp.getContext()).setDefaultRequestOptions(requestOptions)
                    .load(profile.getB2_dpUrl()).into(imgDp);
        }
        catch (Exception e){
            Log.w("{PeopleAdapter", "onBindViewHolder: " + e.getMessage());
        }

    }
}
