package com.sqube.tipshub;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import utils.AboutUtil;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            actionBar.setTitle("About");
        }
        RecyclerView listAbout = findViewById(R.id.listAbout);

        boolean showCongratsImage = getIntent().getBooleanExtra("showCongratsImage", false);
        ImageView imgCongrats = findViewById(R.id.imgCongrats);
        imgCongrats.setVisibility(showCongratsImage? View.VISIBLE: View.GONE);
        listAbout.setLayoutManager(new LinearLayoutManager(this));
        listAbout.setAdapter(new Adapt());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public class Adapt extends RecyclerView.Adapter<Holder>{
        ArrayList<Map<String, String>> aboutList = AboutUtil.getAboutList();

        Adapt(){}

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.about_item, viewGroup, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.bindText(aboutList.get(i));

        }

        @Override
        public int getItemCount() {
            return aboutList.size();
        }
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView txtHeading, txtBody;
        Holder(@NonNull View itemView) {
            super(itemView);
            txtHeading = itemView.findViewById(R.id.txtHeading);
            txtBody = itemView.findViewById(R.id.txtBody);
        }

        private void bindText(Map<String, String> model){
            txtHeading.setText(model.get("heading"));
            txtBody.setText(Html.fromHtml(model.get("body")));
        }
    }
}
