package adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sqube.tipshub.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.ListNewsViewHolder>{
    private Activity activity;
    private ArrayList<Map<String, Object>> performanceList;
    Map<String, Object> row = new HashMap<>();
    String TAG = "PerformanceAdapter";

    public PerformanceAdapter(Activity a, ArrayList<Map<String, Object>> list) {
        activity = a;
        performanceList = list;
        Log.i(TAG, "PerformanceAdapter: " + performanceList);
    }

    @NonNull
    @Override
    public ListNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("News started", "onCreateViewHolder: started");
        View convertView =  LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.tips_performance_view, parent, false);
        return new ListNewsViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(@NonNull ListNewsViewHolder holder, int position) {
        row = performanceList.get(position);
        try{
            holder.txtType.setText(getType((int)row.get("type")));
            holder.txtNOG.setText((long)row.get("NOG")>1 ? row.get("NOG") + " tips" : row.get("NOG") + " tip");
            holder.txtWG.setText(row.get("WG")+ " won");
            long i = (long) row.get("WGP");
            holder.txtWGP.setText(String.format(Locale.getDefault(),"%.1f%%", (double) i));
        }catch(Exception e) {
            e.getStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        Log.i(TAG, "getItemCount: " + performanceList.size());
        return performanceList.size();
    }

    private String getType(int i) {
        switch (i){
            case 1:
                return "3-5 odd";
            case 2:
                return "6-10 odd";
            case 3:
                return "11-50 odd";
            case 4:
                return "50+ odd";
            case 5:
                return "Draws";
            case 6:
                return "Banker";
        }
        return "";
    }

    class ListNewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtType, txtNOG, txtWG, txtWGP;
        public ListNewsViewHolder(View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txtType);
            txtNOG = itemView.findViewById(R.id.txtNOG);
            txtWG = itemView.findViewById(R.id.txtWG);
            txtWGP = itemView.findViewById(R.id.txtWGP);
        }
    }
}

