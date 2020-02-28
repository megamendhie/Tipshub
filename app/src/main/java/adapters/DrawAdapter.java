package adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.R;

import java.util.ArrayList;

import models.Draw;

public class DrawAdapter extends RecyclerView.Adapter<DrawAdapter.DrawViewHolder> {
    private ArrayList<Draw> list = new ArrayList<>();
    private int colorCode = R.color.colorAccent;

    @NonNull
    @Override
    public DrawViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.draw_item, parent, false);
        return new DrawViewHolder(view);
    }

    public void setList(ArrayList<Draw> list, String code){
        this.list = list;
        this.colorCode = setColor(code);
        this.notifyDataSetChanged();
    }

    int setColor(String code){
        if(code!=null){
            switch (code.toLowerCase()){
                case "r":
                    return R.color.color_oxblood;
                case "br":
                    return R.color.brown;
                case "p":
                    return R.color.purple;
            }
        }
        return R.color.colorAccent;
    }

    @Override
    public void onBindViewHolder(@NonNull DrawViewHolder holder, int position) {
        Draw draw = list.get(position);
        holder.updateGame(draw);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DrawViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNum, txtMatch;
        private ImageView imgWon;
        Draw draw;

        DrawViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNum = itemView.findViewById(R.id.txtNum);
            txtMatch = itemView.findViewById(R.id.txtMatch);
            imgWon = itemView.findViewById(R.id.imgWon);
        }

        void updateGame(Draw drawModel){
            draw = drawModel;
            txtNum.setText(String.valueOf(draw.getNum()));
            txtMatch.setText(draw.getMatch());
            if(draw.isWon())
                imgWon.setVisibility(View.VISIBLE);
            else
                imgWon.setVisibility(View.INVISIBLE);
            GradientDrawable txtNumBackground = (GradientDrawable) txtNum.getBackground();
            txtNumBackground.setColor(itemView.getContext().getResources().getColor(colorCode));
        }

    }
}
