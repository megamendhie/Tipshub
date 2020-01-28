package com.sqube.tipshub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import adapters.PostAdapter;
import adapters.TipsAdapter;
import models.GameTip;
import models.Post;
import utils.DatabaseHelper;
import utils.FirebaseUtil;
import utils.HttpConFunction;

import static utils.Calculations.CLASSIC;
import static utils.Calculations.GUEST;
import static utils.Calculations.WONGAMES;
import static utils.Calculations.targetUrl;

public class LandActivity extends AppCompatActivity {
    private static final String TAG = "LandActivityTAG";

    private final String classic = "classic";
    private final String won = "won";

    private ArrayList<GameTip> classicTips = new ArrayList<>();
    private ArrayList<GameTip> wonTips = new ArrayList<>();

    private TipsAdapter classicAdapter = new TipsAdapter(classicTips, true);
    private TipsAdapter wonAdapter = new TipsAdapter(wonTips, true);
    private JSONObject flagsJson;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ShimmerFrameLayout shimmerLandingTip, shimmerRecentWinnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_land);
        RecyclerView listLandingTip = findViewById(R.id.listLandingTip);
        RecyclerView listLandingPost = findViewById(R.id.listLandingPost);
        RecyclerView listRecentWinnings = findViewById(R.id.listRecentWinnings);

        listLandingTip.setLayoutManager(new LinearLayoutManager(this));
        listRecentWinnings.setLayoutManager(new LinearLayoutManager(this));
        listLandingPost.setLayoutManager(new LinearLayoutManager(this));

        shimmerLandingTip = findViewById(R.id.shimmerLandingTip); shimmerLandingTip.startShimmer();
        shimmerRecentWinnings = findViewById(R.id.shimmerRecentWinnings); shimmerRecentWinnings.startShimmer();

        listLandingTip.setAdapter(classicAdapter);
        listRecentWinnings.setAdapter(wonAdapter);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        Query query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(4);
        FirestoreRecyclerOptions<Post> response = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        PostAdapter postAdapter = new PostAdapter(response, GUEST, getApplicationContext());
        listLandingPost.setAdapter(postAdapter);
        Log.i(TAG, "loadPost: started listening");
        postAdapter.startListening();

        flagsJson = HttpConFunction.getFlags(getResources().openRawResource(R.raw.flags));

        GetTips getClassicTips = new GetTips(classic);
        getClassicTips.execute();

        GetTips getWonTips = new GetTips(won);
        getWonTips.execute();

    }

    public void goToLogin(View v){
        switch (v.getId()){
            case R.id.btnJoin:
            case R.id.btnLogin:
                Intent inTent = new Intent(LandActivity.this, LoginActivity.class);
                inTent.putExtra("openMainActivity", true);
                startActivity(inTent);
                finish();
                break;
            case R.id.txtOpenFull:
            case R.id.txtOpenPost:
            case R.id.txtOpenFullR:
                showPrompt(new Intent(LandActivity.this, LoginActivity.class));
                break;
            case R.id.txtContact:
                startActivity(new Intent(LandActivity.this, ContactActivity.class));
                break;
            case R.id.txtPolicy:
                Intent intent = new Intent(LandActivity.this, NewsStoryActivity.class);
                intent.putExtra("url", "https://tipshub.co/privacy-policy/");
                startActivity(intent);
                break;
            case R.id.btnHowTo:
            case R.id.txtGuidelines:
                startActivity(new Intent(LandActivity.this, AboutActivity.class));
                break;
        }
    }

    private void showPrompt(Intent intent){
        AlertDialog.Builder builder = new AlertDialog.Builder(LandActivity.this,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        intent.putExtra("openMainActivity", true);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    private class GetTips extends AsyncTask<String, Void, ArrayList<GameTip>> {
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
                case won:
                    xml = dbHelper.getTip(db, WONGAMES); break;
            }

            if(xml!=null && !xml.isEmpty())
                onPostExecute(getTips(xml));
        }

        @Override
        protected ArrayList<GameTip> doInBackground(String... strings) {
            HttpConFunction httpConnection = new HttpConFunction();
            String s = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            switch (market){
                case classic:
                    Date today = new Date();
                    String todaysDate = sdf.format(today.getTime());

                    s = httpConnection.executeGet( targetUrl+ "iso_date="+todaysDate, "HOME");
                    if(s!=null && s.length() >= 10)
                        dbHelper.updateTip(db, CLASSIC, s);
                    break;
                case won:
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
                    if(market.equals(won) && !tipJSON.optString("status").equals("won"))
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
                case classic:
                    classicTips.clear();
                    for(GameTip tip: tips){
                        classicTips.add(tip);
                        if (classicTips.size()>=3)
                            break;
                    }
                    Log.i(TAG, "onPostExecute: classicTips:"  + classicTips);
                    runOnUiThread(() -> classicAdapter.notifyDataSetChanged());
                    shimmerLandingTip.stopShimmer();
                    shimmerLandingTip.setVisibility(View.GONE);
                    break;
                case won:
                    wonTips.clear();
                    Collections.sort(tips);
                    for(GameTip tip: tips){
                        wonTips.add(tip);
                        if (wonTips.size()>=5)
                            break;
                    }
                    Log.i(TAG, "onPostExecute: wonTips:"  + wonTips);
                    runOnUiThread(() -> wonAdapter.notifyDataSetChanged());
                    shimmerRecentWinnings.stopShimmer();
                    shimmerRecentWinnings.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
