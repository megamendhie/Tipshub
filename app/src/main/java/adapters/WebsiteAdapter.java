package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.R;

import java.util.ArrayList;

import models.Website;

public class WebsiteAdapter extends RecyclerView.Adapter<WebsiteHolder> {
    ArrayList<Website> siteList;

    public WebsiteAdapter(ArrayList<Website> siteList){
        this.siteList = siteList;
    }

    @NonNull
    @Override
    public WebsiteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sport_site, parent, false);
        return new WebsiteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebsiteHolder holder, int position) {
        holder.setDisplay(siteList.get(position));
    }

    @Override
    public int getItemCount() {
        if(siteList==null|| siteList.isEmpty())
            return 0;
        else
            return siteList.size();
    }
}
