package adapters;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.FullPostActivity;
import com.sqube.tipshub.R;

import de.hdodenhof.circleimageview.CircleImageView;
import views.DislikeButton;
import views.LikeButton;

class PostHolder extends RecyclerView.ViewHolder {
    private String postId;
    CircleImageView imgDp, childDp;
    LinearLayout lnrChildContainer;
    CardView crdChildPost;
    TextView mpost, childPost;
    TextView mUsername, childUsername;
    TextView mTime;
    TextView mLikes, mDislikes, mComment, mCode, mType, childCode, childType;
    ImageView imgComment, imgShare, imgStatus, imgChildStatus, imgOverflow;
    LikeButton imgLikes;
    DislikeButton imgDislike;

    PostHolder(View itemView) {
        super(itemView);
        imgDp = itemView.findViewById(R.id.imgDp);
        childDp = itemView.findViewById(R.id.childDp);
        crdChildPost = itemView.findViewById(R.id.crdChildPost);
        lnrChildContainer = itemView.findViewById(R.id.container_child_post);

        mpost = itemView.findViewById(R.id.txtPost);
        childPost = itemView.findViewById(R.id.txtChildPost);
        mUsername = itemView.findViewById(R.id.txtUsername);
        childUsername = itemView.findViewById(R.id.txtChildUsername);
        mTime = itemView.findViewById(R.id.txtTime);

        mLikes = itemView.findViewById(R.id.txtLike);
        mDislikes = itemView.findViewById(R.id.txtDislike);
        mComment = itemView.findViewById(R.id.txtComment);
        mCode = itemView.findViewById(R.id.txtCode);
        mType = itemView.findViewById(R.id.txtPostType);
        childCode = itemView.findViewById(R.id.txtChildCode);
        childType = itemView.findViewById(R.id.txtChildType);

        imgLikes = itemView.findViewById(R.id.imgLike);
        imgDislike = itemView.findViewById(R.id.imgDislike);
        imgComment = itemView.findViewById(R.id.imgComment);
        imgShare = itemView.findViewById(R.id.imgShare);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        imgOverflow = itemView.findViewById(R.id.imgOverflow);
        imgChildStatus = itemView.findViewById(R.id.imgChildStatus);

        itemView.setOnClickListener(view -> {
            Intent intent = new Intent(itemView.getContext(), FullPostActivity.class);
            intent.putExtra("postId", postId);
            itemView.getContext().startActivity(intent);
        });
    }

    public void setPostId(String postId){
        this.postId = postId;
    }
}
