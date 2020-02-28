package com.sqube.tipshub;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import adapters.DrawAdapter;
import adapters.TipsAdapter;
import models.Draw;
import models.GameTip;
import models.ProfileMedium;
import utils.Calculations;
import utils.DatabaseHelper;
import utils.FirebaseUtil;
import utils.HttpConFunction;

import static utils.Calculations.BTTS;
import static utils.Calculations.CLASSIC;
import static utils.Calculations.OVER;
import static utils.Calculations.WONGAMES;
import static utils.Calculations.targetUrl;

public class FullViewActivity extends AppCompatActivity {

    RecyclerView listClassic, listVIP, listOver, listBts, listDraw, listWon;
    private JSONObject flagsJson;
    private Gson gson = new Gson();

    private ArrayList<GameTip> classicTips = new ArrayList<>();
    private ArrayList<GameTip> vipTips = new ArrayList<>();
    private ArrayList<GameTip> overTips = new ArrayList<>();
    private ArrayList<GameTip> bttTips = new ArrayList<>();
    private ArrayList<GameTip> wonTips = new ArrayList<>();
    private TextView txtDate, txtWeek, btnSeeAll;

    private TipsAdapter classicAdapter;
    private TipsAdapter vipAdapter;
    private TipsAdapter overAdapter;
    private TipsAdapter bttsAdapter;
    private TipsAdapter wonAdapter;
    private DrawAdapter drawAdapter = new DrawAdapter();

    private ShimmerFrameLayout shimmerClassicTips, shimmerVipTips, shimmerOverTips, shimmerBtsTips, shimmerDrawTips, shimmerWonTips;

    private SharedPreferences prefs;

    private RelativeLayout lnrVip, lnrOver, lnrBts, lnrDraw;
    private boolean subscriber;
    private int cutOff = 5;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    GetTips getClassicTips, getOverTips, getBttsTips, getWonTips;

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
        listDraw = findViewById(R.id.listDraw);
        listWon = findViewById(R.id.listWon);

        lnrVip = findViewById(R.id.lnrVip);
        lnrOver = findViewById(R.id.lnrOver);
        lnrBts = findViewById(R.id.lnrBts);
        lnrDraw = findViewById(R.id.lnrDraw);

        txtDate = findViewById(R.id.txtDate);
        txtWeek = findViewById(R.id.txtWeek);
        btnSeeAll = findViewById(R.id.btnSeeAll);

        listClassic.setLayoutManager(new LinearLayoutManager(this));
        listVIP.setLayoutManager(new LinearLayoutManager(this));
        listOver.setLayoutManager(new LinearLayoutManager(this));
        listBts.setLayoutManager(new LinearLayoutManager(this));
        listDraw.setLayoutManager(new LinearLayoutManager(this));
        listWon.setLayoutManager(new LinearLayoutManager(this));

        shimmerClassicTips = findViewById(R.id.shimmerClassicTips); shimmerClassicTips.startShimmer();
        shimmerVipTips = findViewById(R.id.shimmerVipTips); shimmerVipTips.startShimmer();
        shimmerOverTips = findViewById(R.id.shimmerOverTips); shimmerOverTips.startShimmer();
        shimmerBtsTips = findViewById(R.id.shimmerBtsTips); shimmerBtsTips.startShimmer();
        shimmerDrawTips = findViewById(R.id.shimmerDrawTips); shimmerDrawTips.startShimmer();
        shimmerWonTips = findViewById(R.id.shimmerWonTips); shimmerWonTips.startShimmer();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = prefs.getString("profile", "");
        ProfileMedium myProfile = (json.equals("")) ? null : gson.fromJson(json, ProfileMedium.class);
        subscriber = myProfile != null && myProfile.isD4_vipSubscriber();

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        setLayoutVisibility();

        flagsJson = HttpConFunction.getFlags(this.getResources().openRawResource(R.raw.flags));
        getClassicTips = new GetTips(CLASSIC);
        getOverTips = new GetTips(OVER);
        getBttsTips = new GetTips(BTTS);
        getWonTips = new GetTips(WONGAMES);
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
        if(subscriber)
            btnSeeAll.setVisibility(View.GONE);
        classicAdapter = new TipsAdapter(classicTips);
        vipAdapter = new TipsAdapter(vipTips);
        overAdapter = new TipsAdapter(overTips);
        bttsAdapter = new TipsAdapter(bttTips);
        wonAdapter = new TipsAdapter(wonTips);

        listClassic.setAdapter(classicAdapter);
        listVIP.setAdapter(vipAdapter);
        listOver.setAdapter(overAdapter);
        listBts.setAdapter(bttsAdapter);
        listDraw.setAdapter(drawAdapter);
        listWon.setAdapter(wonAdapter);
        if(refreshed) {
            classicTips.clear();
            wonTips.clear();
            getClassicTips.execute();
            getWonTips.execute();
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

            getWonTips.execute();
        }

        if(subscriber){
            getOverTips.execute();
            getBttsTips.execute();
            getDrawTips();
        }
    }

    private void getDrawTips() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("SystemConfig").child("draws_vip");
        ref.keepSynced(true);
        ref.limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dSnap) {
                if(!dSnap.hasChildren())
                    return;
                DataSnapshot dataSnapshot  = null;
                for(DataSnapshot snap: dSnap.getChildren()){
                    dataSnapshot = snap;
                }
                ArrayList<Draw> drawList = new ArrayList<>();
                String date = dataSnapshot.child("date").getValue(String.class);
                String week = dataSnapshot.child("week").getValue(String.class);
                //String key = dataSnapshot.child("key").getValue(String.class);
                String colorCode = dataSnapshot.child("colorCode").getValue(String.class);

                txtDate.setText(date); txtDate.setVisibility(View.VISIBLE);
                txtWeek.setText(week); txtWeek.setVisibility(View.VISIBLE);
                DataSnapshot games = dataSnapshot.child("games");
                for(DataSnapshot snapshot: games.getChildren()){
                    Draw draw = snapshot.getValue(Draw.class);
                    drawList.add(draw);
                }

                drawAdapter.setList(drawList, colorCode);
                shimmerDrawTips.stopShimmer();
                shimmerDrawTips.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLayoutVisibility() {
        if(subscriber){
            lnrVip.setVisibility(View.GONE);
            lnrOver.setVisibility(View.GONE);
            lnrBts.setVisibility(View.GONE);
            lnrDraw.setVisibility(View.GONE);

            shimmerVipTips.setVisibility(View.VISIBLE);
            shimmerOverTips.setVisibility(View.VISIBLE);
            shimmerBtsTips.setVisibility(View.VISIBLE);
            shimmerDrawTips.setVisibility(View.VISIBLE);
            listVIP.setVisibility(View.VISIBLE);
            listOver.setVisibility(View.VISIBLE);
            listBts.setVisibility(View.VISIBLE);
            listDraw.setVisibility(View.VISIBLE);
            cutOff = 12;
        }
        else{
            lnrVip.setVisibility(View.VISIBLE);
            lnrOver.setVisibility(View.VISIBLE);
            lnrBts.setVisibility(View.VISIBLE);
            lnrDraw.setVisibility(View.VISIBLE);

            shimmerVipTips.setVisibility(View.GONE);
            shimmerOverTips.setVisibility(View.GONE);
            shimmerBtsTips.setVisibility(View.GONE);
            shimmerDrawTips.setVisibility(View.GONE);
            listVIP.setVisibility(View.GONE);
            listOver.setVisibility(View.GONE);
            listBts.setVisibility(View.GONE);
            listDraw.setVisibility(View.GONE);
            cutOff = 5;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void setVip(ArrayList<GameTip> tips) {
        vipTips.clear();
        for(GameTip tip: tips){
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
                case CLASSIC:
                    xml = dbHelper.getTip(db, CLASSIC); break;
                case OVER:
                    xml = dbHelper.getTip(db, OVER); break;
                case BTTS:
                    xml = dbHelper.getTip(db, BTTS); break;
                case WONGAMES:
                    xml = dbHelper.getTip(db, WONGAMES); break;
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
                case CLASSIC:
                    s = httpConnection.executeGet( targetUrl+ "iso_date="+todayDate, "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, CLASSIC, s);
                    break;
                case OVER:
                    s = httpConnection.executeGet(targetUrl + "iso_date="+todayDate+"&market=over_25", "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, OVER, s);
                    break;
                case BTTS:
                    s = httpConnection.executeGet(targetUrl + "iso_date="+todayDate+"&market=btts", "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, BTTS, s);
                    break;
                case WONGAMES:
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, -1);
                    String yesterdaysDate = sdf.format(c.getTime());

                    s = httpConnection.executeGet( targetUrl+ "iso_date="+yesterdaysDate, "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, WONGAMES, s);
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
                    if(market.equals(WONGAMES) && !tipJSON.optString("status").equals("won"))
                        continue;
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
                case CLASSIC:
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
                case OVER:
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
                case BTTS:
                    bttTips.clear();
                    Collections.sort(tips);
                    for(GameTip tip: tips){
                        bttTips.add(tip);
                        if (bttTips.size()>=4)
                            break;
                    }
                    bttsAdapter.notifyDataSetChanged();
                    shimmerBtsTips.stopShimmer();
                    shimmerBtsTips.setVisibility(View.GONE);
                    break;
                case WONGAMES:
                    wonTips.clear();
                    Collections.sort(tips);
                    for(GameTip tip: tips){
                        wonTips.add(tip);
                        if (wonTips.size()>=6)
                            break;
                    }
                    wonAdapter.notifyDataSetChanged();
                    shimmerWonTips.stopShimmer();
                    shimmerWonTips.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void openSub(View view){
        startActivity(new Intent(FullViewActivity.this, VipSubActivity.class));
    }
}
