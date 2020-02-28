package adapters;


import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import views.DislikeButton;
import views.LikeButton;

class BankerPostHolder extends RecyclerView.ViewHolder {
    CircleImageView imgDp;
    RelativeLayout lnrSub;
    LinearLayout lnrContainer;
    TextView mpost, mUsername, mTime;
    TextView mLikes, mDislikes, mComment, mCode, mType, mSub;
    ImageView imgOverflow;
    ImageView imgComment, imgRepost, imgStatus;
    LikeButton imgLikes;
    DislikeButton imgDislike;

    BankerPostHolder(View itemView) {
        super(itemView);
        imgDp = itemView.findViewById(R.id.imgDp);
        lnrSub = itemView.findViewById(R.id.lnrSub);
        lnrContainer = itemView.findViewById(R.id.container_post);

        mpost = itemView.findViewById(R.id.txtPost);
        mUsername = itemView.findViewById(R.id.txtUsername);
        mTime = itemView.findViewById(R.id.txtTime);
        mSub = itemView.findViewById(R.id.txtSub);

        mLikes = itemView.findViewById(R.id.txtLike);
        mDislikes = itemView.findViewById(R.id.txtDislike);
        mComment = itemView.findViewById(R.id.txtComment);
        mCode = itemView.findViewById(R.id.txtCode);
        mType = itemView.findViewById(R.id.txtPostType);

        imgLikes = itemView.findViewById(R.id.imgLike);
        imgDislike = itemView.findViewById(R.id.imgDislike);
        imgComment = itemView.findViewById(R.id.imgComment);
        imgRepost = itemView.findViewById(R.id.imgRepost);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        imgOverflow = itemView.findViewById(R.id.imgOverflow);
    }
}
