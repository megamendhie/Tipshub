package adapters;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;
import models.Website;

public class WebsiteHolder extends RecyclerView.ViewHolder {
    private ImageView imgIcon;
    private TextView txtName;

    WebsiteHolder(@NonNull View itemView) {
        super(itemView);
        imgIcon = itemView.findViewById(R.id.imgIcon);
        txtName = itemView.findViewById(R.id.txtName);
    }

    public void setDisplay(Website website){
        txtName.setText(website.getName());
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(website.getIcon());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.i("PeopleAdapter", "onComplete: "+ uri.toString());
            try {
                Glide.with(imgIcon.getContext()).load(uri.toString()).fitCenter().into(imgIcon);
            }
            catch (Exception e){
                Log.w("{PeopleAdapter", "imgIcon GlideApp: " + e.getMessage());
            }
        });
        itemView.setOnClickListener(view -> {
            itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    website.getLink())));
        });
    }
}
