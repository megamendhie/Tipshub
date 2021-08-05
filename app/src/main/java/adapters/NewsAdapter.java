package adapters;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sqube.tipshub.NewsStoryActivity;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.HashMap;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ListNewsViewHolder>{
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public NewsAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
    }

    @NonNull
    @Override
    public ListNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView =  null;
        if(viewType==0)
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_news, parent, false);
        else
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_news_small, parent, false);

        return new ListNewsViewHolder(convertView);
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 4);
    }

    @Override
    public void onBindViewHolder(@NonNull ListNewsViewHolder holder, int position) {
        HashMap<String, String> news = data.get(position);
        try{
            holder.description.setText(news.get("description"));
            holder.title.setText(news.get("title"));
            holder.time.setText(news.get("publishedAt"));

            if(news.get("urlToImage").length() > 5)
                Glide.with(activity)
                        .load(news.get("urlToImage"))
                        .into(holder.galleryImage);

            holder.crdContainer.setOnClickListener(v -> {
                Intent i = new Intent(activity.getApplicationContext(), NewsStoryActivity.class);
                i.putExtra("url", news.get("url"));
                activity.startActivity(i);
                activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            });
        }catch(Exception e) {}

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ListNewsViewHolder extends RecyclerView.ViewHolder {
        CardView crdContainer;
        ImageView galleryImage;
        TextView description, title, time;
        ListNewsViewHolder(View itemView) {
            super(itemView);
            crdContainer = itemView.findViewById(R.id.crdContainer);
            galleryImage = itemView.findViewById(R.id.galleryImage);
            description = itemView.findViewById(R.id.txtDescription);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }
    }
}

