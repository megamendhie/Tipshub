package adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.HashMap;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ListNewsViewHolder>{
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    HashMap<String, String> news = new HashMap<String, String>();
    private final String KEY_AUTHOR = "author";
    private final String KEY_TITLE = "title";
    private final String KEY_DESCRIPTION = "description";
    private final String KEY_URL = "url";
    private final String KEY_URLTOIMAGE = "urlToImage";
    private final String KEY_PUBLISHEDAT = "publishedAt";

    public NewsAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
    }

    @NonNull
    @Override
    public ListNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("News started", "onCreateViewHolder: started");
        View convertView =  LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.news_container, parent, false);
        return new ListNewsViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(@NonNull ListNewsViewHolder holder, int position) {
        news = data.get(position);
        try{
            holder.author.setText(news.get(KEY_AUTHOR));
            holder.title.setText(news.get(KEY_TITLE));
            holder.time.setText(news.get(KEY_PUBLISHEDAT));

            if(news.get(KEY_URLTOIMAGE).toString().length() < 5)
            {
                //holder.galleryImage.setVisibility(View.GONE);
            }else{
                Glide.with(activity)
                        .load(news.get(KEY_URLTOIMAGE))
                        .into(holder.galleryImage);
            }
            holder.crdContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Testing", "onClick: " + news.get(KEY_TITLE )+ " " + news.get(KEY_URLTOIMAGE));
                    //Intent i = new Intent(activity.getApplicationContext(), NewsStoryActivity.class);
                    //i.putExtra("url", data.get(+position).get(MainActivity.KEY_URL));
                    //activity.startActivity(i);
                }
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
        TextView author, title, time;
        public ListNewsViewHolder(View itemView) {
            super(itemView);
            crdContainer = itemView.findViewById(R.id.crdContainer);
            galleryImage = itemView.findViewById(R.id.galleryImage);
            author = itemView.findViewById(R.id.author);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }
    }
}

