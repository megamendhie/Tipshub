package adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sqube.tipshub.FullViewActivity;
import com.sqube.tipshub.R;
import com.sqube.tipshub.VipSubActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import models.GameTip;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipsHolder> {
    private ArrayList<GameTip> tips;
    private boolean subscriber;

    public TipsAdapter(ArrayList<GameTip> tips, boolean subscriber){
        this.tips = tips;
        this.subscriber = subscriber;
    }

    @NonNull
    @Override
    public TipsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_tip_view, parent, false);
        return new TipsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsHolder holder, int position) {
        GameTip gameTip = tips.get(position);
        holder.bindItems(gameTip);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    class TipsHolder extends RecyclerView.ViewHolder{
        TextView txtRegion, txtLeague, txtTime, txtHomeTeam, txtAwayTeam, txtResult, txtPrediction, txtProbabity;
        ImageView imgStatus;
        TipsHolder(@NonNull View itemView) {
            super(itemView);

            txtAwayTeam =itemView.findViewById(R.id.txtAwayTeam);
            txtHomeTeam =itemView.findViewById(R.id.txtHomeTeam);
            txtLeague =itemView.findViewById(R.id.txtLeague);
            txtPrediction = itemView.findViewById(R.id.txtPrediction);
            txtProbabity = itemView.findViewById(R.id.txtProbability);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtRegion = itemView.findViewById(R.id.txtRegion);
            txtResult = itemView.findViewById(R.id.txtResult);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }

        private void showPrompt(){
            AlertDialog.Builder builder = new AlertDialog.Builder(txtPrediction.getRootView().getContext(),
                    R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setMessage("Tip probability shows only for subscribers.\n\n")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    })
                    .setPositiveButton("See price", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            txtPrediction.getRootView().getContext()
                                    .startActivity(new Intent(txtPrediction.getRootView().getContext(), VipSubActivity.class));
                        }
                    })
                    .show();
        }

        private void bindItems(GameTip tip){
            txtLeague.setText(tip.getLeague());
            String region = tip.getRegion() + "  -";
            txtRegion.setText(region);
            txtPrediction.setText(tip.getPrediction());
            txtHomeTeam.setText(tip.getHomeTeam());
            txtAwayTeam.setText(tip.getAwayTeam());
            txtResult.setText(tip.getResult().isEmpty()? "vs": tip.getResult());
            txtProbabity.setText(subscriber?String.format(Locale.getDefault(),"P: %.2f%%", 100*tip.getProbability()): "see probability");
            txtProbabity.setOnClickListener(view -> {
                if(!subscriber)
                    showPrompt();
            });
            txtTime.setText(getFormattedTime(tip.getTime()));
            if(tip.getStatus().equals("lost"))
                imgStatus.setVisibility(View.INVISIBLE);
            else{
                imgStatus.setImageResource(tip.getStatus().equals("pending")?
                        R.drawable.ic_hourglass_empty_color_24dp: R.drawable.ic_check_circle_green_24dp);
            }
            imgStatus.setVisibility(tip.getStatus().equals("lost")? View.INVISIBLE: View.VISIBLE);
        }

        private String getFormattedTime(String time){
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            oldFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat newFormatter = new SimpleDateFormat("dd MMM - hh:mma", Locale.ENGLISH);
            newFormatter.setTimeZone(TimeZone.getDefault());
            Date date = null;
            try {
                date = oldFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date==null)
                return "";
            else {
                String dateTime = newFormatter.format(date);
                dateTime = dateTime.replace("PM", "pm");
                dateTime = dateTime.replace("AM", "am");
                return  dateTime;
            }
        }
    }
}
