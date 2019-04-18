package adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sqube.tipshub.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.PostHolder> {
    String[] dataset;
    public CommentAdapter(String[] dataset){
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        TextView mpost;
        TextView mUsername;
        TextView mTime;
        public PostHolder(View itemView) {
            super(itemView);
            mpost = itemView.findViewById(R.id.txtPost);
            mUsername = itemView.findViewById(R.id.txtUsername);
            mTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
