package com.sqube.tipshub;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import adapters.TipsAdapter;
import models.GameTip;
import models.ProfileMedium;
import utils.Calculations;
import utils.DatabaseHelper;
import utils.HttpConFunction;

import static utils.Calculations.BTTS;
import static utils.Calculations.CLASSIC;
import static utils.Calculations.OVER;
import static utils.Calculations.targetUrl;

public class FullViewActivity extends AppCompatActivity {

    RecyclerView listClassic, listVIP, listOver, listBts;
    private JSONObject flagsJson;
    private Gson gson = new Gson();

    private ArrayList<GameTip> classicTips = new ArrayList<>();
    private ArrayList<GameTip> sortedClassicTips = new ArrayList<>();
    private ArrayList<GameTip> vipTips = new ArrayList<>();
    private ArrayList<GameTip> overTips = new ArrayList<>();
    private ArrayList<GameTip> bttTips = new ArrayList<>();

    private TipsAdapter classicAdapter;
    private TipsAdapter vipAdapter;
    private TipsAdapter overAdapter;
    private TipsAdapter bttsAdapter;

    private ShimmerFrameLayout shimmerClassicTips, shimmerVipTips, shimmerOverTips, shimmerBtsTips;

    private final String classic = "classic";
    private final String over = "over";
    private final String btts = "btts";

    private SharedPreferences prefs;

    private RelativeLayout lnrVip, lnrOver, lnrBts;
    private boolean subscriber;
    private int cutOff = 7;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    GetTips getClassicTips, getOverTips, getBttsTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        listClassic = findViewById(R.id.listClassic);
        listVIP = findViewById(R.id.listVIP);
        listOver = findViewById(R.id.listOver);
        listBts = findViewById(R.id.listBts);

        lnrVip = findViewById(R.id.lnrVip);
        lnrOver = findViewById(R.id.lnrOver);
        lnrBts = findViewById(R.id.lnrBts);

        listClassic.setLayoutManager(new LinearLayoutManager(this));
        listVIP.setLayoutManager(new LinearLayoutManager(this));
        listOver.setLayoutManager(new LinearLayoutManager(this));
        listBts.setLayoutManager(new LinearLayoutManager(this));

        shimmerClassicTips = findViewById(R.id.shimmerClassicTips); shimmerClassicTips.startShimmer();
        shimmerVipTips = findViewById(R.id.shimmerVipTips); shimmerVipTips.startShimmer();
        shimmerOverTips = findViewById(R.id.shimmerOverTips); shimmerOverTips.startShimmer();
        shimmerBtsTips = findViewById(R.id.shimmerBtsTips); shimmerBtsTips.startShimmer();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = prefs.getString("profile", "");
        ProfileMedium myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        subscriber = myProfile != null && myProfile.isD4_vipSubscriber();

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        setLayoutVisibility();

        flagsJson = HttpConFunction.getFlags(this.getResources().openRawResource(R.raw.flags));
        getClassicTips = new GetTips(classic);
        getOverTips = new GetTips(over);
        getBttsTips = new GetTips(btts);
        loadTips(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String json = prefs.getString("profile", "");
        ProfileMedium myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        boolean subscriber = myProfile != null && myProfile.isD4_vipSubscriber();
        if(subscriber && !this.subscriber ){
            this.subscriber = true;
            setLayoutVisibility();
            loadTips(false);
        }
    }

    private void loadTips(boolean refreshed) {
        classicAdapter = new TipsAdapter(classicTips, subscriber);
        vipAdapter = new TipsAdapter(vipTips, subscriber);
        overAdapter = new TipsAdapter(overTips, subscriber);
        bttsAdapter = new TipsAdapter(bttTips, subscriber);

        listClassic.setAdapter(classicAdapter);
        listVIP.setAdapter(vipAdapter);
        listOver.setAdapter(overAdapter);
        listBts.setAdapter(bttsAdapter);

        if(refreshed) {
            getClassicTips.execute();
        }
        else{
            classicTips.clear();
            if(!Calculations.getFreeGameTips().isEmpty()) {
                for(GameTip tip: Calculations.getFreeGameTips()){
                    classicTips.add(tip);
                    if(classicTips.size() >= cutOff)
                        break;
                }
                classicAdapter.notifyDataSetChanged();
                shimmerClassicTips.stopShimmer();
                shimmerClassicTips.setVisibility(View.GONE);
            }
            if(subscriber)
                setVip(classicTips);
        }

        if(subscriber){
            getOverTips.execute();
            getBttsTips.execute();
        }
    }

    private void setLayoutVisibility() {
        if(subscriber){
            lnrVip.setVisibility(View.GONE);
            lnrOver.setVisibility(View.GONE);
            lnrBts.setVisibility(View.GONE);
            shimmerVipTips.setVisibility(View.VISIBLE);
            shimmerOverTips.setVisibility(View.VISIBLE);
            shimmerBtsTips.setVisibility(View.VISIBLE);
            listVIP.setVisibility(View.VISIBLE);
            listOver.setVisibility(View.VISIBLE);
            listBts.setVisibility(View.VISIBLE);
            cutOff = 12;
        }
        else{
            lnrVip.setVisibility(View.VISIBLE);
            lnrOver.setVisibility(View.VISIBLE);
            lnrBts.setVisibility(View.VISIBLE);
            shimmerVipTips.setVisibility(View.GONE);
            shimmerOverTips.setVisibility(View.GONE);
            shimmerBtsTips.setVisibility(View.GONE);
            listVIP.setVisibility(View.GONE);
            listOver.setVisibility(View.GONE);
            listBts.setVisibility(View.GONE);
            cutOff = 7;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void setVip(ArrayList<GameTip> tips) {
        sortedClassicTips.addAll(tips);
        Collections.sort(sortedClassicTips);
        vipTips.clear();
        for(GameTip tip: sortedClassicTips){
            vipTips.add(tip);
            if (vipTips.size()>=4)
                break;
        }
        vipAdapter.notifyDataSetChanged();
        shimmerVipTips.stopShimmer();
        shimmerVipTips.setVisibility(View.GONE);
    }

    private class GetTips extends AsyncTask<String, Void, ArrayList<GameTip>>{
        private String market;

        private GetTips(String market){
            this.market = market;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String xml = null;

            switch (market){
                case classic:
                    xml = dbHelper.getTip(db, CLASSIC); break;
                case over:
                    xml = dbHelper.getTip(db, OVER); break;
                case btts:
                    xml = dbHelper.getTip(db, BTTS); break;
            }

            if(xml!=null && !xml.isEmpty())
                onPostExecute(getTips(xml));
        }

        @Override
        protected ArrayList<GameTip> doInBackground(String... strings) {
            HttpConFunction httpConnection = new HttpConFunction();
            String s = null;

            final Date today = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String todayDate = sdf.format(today.getTime());

            switch (market){
                case classic:
                    s = httpConnection.executeGet( targetUrl+ "iso_date="+todayDate, "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, CLASSIC, s);
                    break;
                case over:
                    s = httpConnection.executeGet(targetUrl + "iso_date="+todayDate+"&market=over_25", "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, OVER, s);
                    break;
                case btts:
                    s = httpConnection.executeGet(targetUrl + "iso_date="+todayDate+"&market=btts", "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, BTTS, s);
                    break;
            }
            return getTips(s);
        }

        private ArrayList<GameTip> getTips(String s) {
            ArrayList<GameTip> tips = new ArrayList<>();
            if(s==null)
                return tips;

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray data = jsonObject.getJSONArray("data");

                for(int i=0; i < data.length(); i++){
                    JSONObject tipJSON = data.getJSONObject(i);
                    GameTip gameTip = new GameTip();
                    gameTip.set_id(tipJSON.optString("id"));
                    gameTip.setAwayTeam(tipJSON.optString("away_team"));
                    gameTip.setHomeTeam(tipJSON.optString("home_team"));
                    String region = tipJSON.optString("competition_cluster");
                    gameTip.setRegion(flagsJson==null? region : flagsJson.optString(region.trim())+" "+region);
                    gameTip.setLeague(tipJSON.optString("competition_name"));
                    gameTip.setPrediction(tipJSON.optString("prediction"));
                    gameTip.setTime(tipJSON.optString("start_date"));
                    gameTip.setResult(tipJSON.optString("result"));
                    gameTip.setStatus(tipJSON.optString("status"));

                    if(tipJSON.has("probabilities")){
                        JSONObject probabilities = tipJSON.optJSONObject("probabilities");
                        gameTip.setProbability(probabilities.optDouble(gameTip.getPrediction()));
                    }

                    JSONObject oddJSON = tipJSON.getJSONObject("odds");
                    if(oddJSON != null){
                        gameTip.setOdd(oddJSON.optDouble(gameTip.getPrediction()));
                    }
                    tips.add(gameTip);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return tips;
        }

        @Override
        protected void onPostExecute(ArrayList<GameTip> tips) {
            Log.i("GETTIPS", "onPostExecute: "+ tips);
            if(tips.isEmpty())
                return;

            switch (market){
                case classic:
                    classicTips.clear();
                    for(GameTip tip: tips){
                        classicTips.add(tip);
                        if (classicTips.size()>=12)
                            break;
                    }
                    classicAdapter.notifyDataSetChanged();
                    shimmerClassicTips.stopShimmer();
                    shimmerClassicTips.setVisibility(View.GONE);
                    setVip(tips);
                    break;
                case over:
                    overTips.clear();
                    Collections.sort(tips);
                    for(GameTip tip: tips){
                        overTips.add(tip);
                        if (overTips.size()>=4)
                            break;
                    }
                    overAdapter.notifyDataSetChanged();
                    shimmerOverTips.stopShimmer();
                    shimmerOverTips.setVisibility(View.GONE);
                    break;
                case btts:
                    bttTips.clear();
                    Collections.sort(tips);
                    for(GameTip tip: tips){
                        bttTips.add(tip);
                        if (bttTips.size()>=5)
                            break;
                    }
                    bttsAdapter.notifyDataSetChanged();
                    shimmerBtsTips.stopShimmer();
                    shimmerBtsTips.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void openSub(View view){
        startActivity(new Intent(FullViewActivity.this, VipSubActivity.class));
    }
}
