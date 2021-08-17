package com.sqube.tipshub

import adapters.PostAdapter
import adapters.TipsAdapter
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.sqube.tipshub.LandActivity
import models.GameTip
import models.Post
import org.json.JSONException
import org.json.JSONObject
import utils.Calculations
import utils.DatabaseHelper
import utils.FirebaseUtil.firebaseFirestore
import utils.HttpConFunction
import java.text.SimpleDateFormat
import java.util.*

class LandActivity : AppCompatActivity() {
    private val classicTips = ArrayList<GameTip>()
    private val wonTips = ArrayList<GameTip>()
    private val classicAdapter = TipsAdapter(classicTips)
    private val wonAdapter = TipsAdapter(wonTips)
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private var shimmerLandingTip: ShimmerFrameLayout? = null
    private var shimmerRecentWinnings: ShimmerFrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land)
        val listLandingTip = findViewById<RecyclerView>(R.id.listLandingTip)
        val listLandingPost = findViewById<RecyclerView>(R.id.listLandingPost)
        val listRecentWinnings = findViewById<RecyclerView>(R.id.listRecentWinnings)
        listLandingTip.layoutManager = LinearLayoutManager(this)
        listRecentWinnings.layoutManager = LinearLayoutManager(this)
        listLandingPost.layoutManager = LinearLayoutManager(this)
        shimmerLandingTip = findViewById(R.id.shimmerLandingTip)
        shimmerLandingTip.startShimmer()
        shimmerRecentWinnings = findViewById(R.id.shimmerRecentWinnings)
        shimmerRecentWinnings.startShimmer()
        listLandingTip.adapter = classicAdapter
        listRecentWinnings.adapter = wonAdapter
        dbHelper = DatabaseHelper(this)
        db = dbHelper!!.readableDatabase
        val query = firebaseFirestore!!.collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(4)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        val postAdapter = PostAdapter(response, Calculations.GUEST, this@LandActivity, false)
        listLandingPost.adapter = postAdapter
        Log.i(TAG, "loadPost: started listening")
        postAdapter.startListening()
        val getClassicTips: GetTips = GetTips(Calculations.CLASSIC)
        getClassicTips.execute()
        val getWonTips: GetTips = GetTips(Calculations.WONGAMES)
        getWonTips.execute()
    }

    fun goToLogin(v: View) {
        when (v.id) {
            R.id.btnJoin, R.id.btnLogin -> {
                val inTent = Intent(this@LandActivity, LoginActivity::class.java)
                inTent.putExtra("openMainActivity", true)
                startActivity(inTent)
                finish()
            }
            R.id.txtOpenFull, R.id.txtOpenPost, R.id.txtOpenFullR -> showPrompt(Intent(this@LandActivity, LoginActivity::class.java))
            R.id.txtContact -> startActivity(Intent(this@LandActivity, ContactActivity::class.java))
            R.id.txtPolicy -> {
                val intent = Intent(this@LandActivity, NewsStoryActivity::class.java)
                intent.putExtra("url", "https://tipshub.co/privacy-policy/")
                startActivity(intent)
            }
            R.id.btnHowTo, R.id.txtGuidelines -> startActivity(Intent(this@LandActivity, AboutActivity::class.java))
        }
    }

    private fun showPrompt(intent: Intent) {
        val builder = AlertDialog.Builder(this@LandActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { dialogInterface: DialogInterface?, i: Int ->
                    intent.putExtra("openMainActivity", true)
                    startActivity(intent)
                    finish()
                }
                .show()
    }

    private inner class GetTips private constructor(private val market: String) : AsyncTask<String?, Void?, ArrayList<GameTip>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            var xml: String? = null
            when (market) {
                Calculations.CLASSIC -> xml = dbHelper!!.getTip(db, Calculations.CLASSIC)
                Calculations.WONGAMES -> xml = dbHelper!!.getTip(db, Calculations.WONGAMES)
            }
            if (xml != null && !xml.isEmpty()) onPostExecute(getTips(xml))
        }

        protected override fun doInBackground(vararg strings: String): ArrayList<GameTip> {
            val httpConnection = HttpConFunction()
            var s: String? = null
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            when (market) {
                Calculations.CLASSIC -> {
                    val today = Date()
                    val todaysDate = sdf.format(today.time)
                    s = httpConnection.executeGet(Calculations.targetUrl + "iso_date=" + todaysDate, "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, Calculations.CLASSIC, s)
                }
                Calculations.WONGAMES -> {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterdaysDate = sdf.format(c.time)
                    s = httpConnection.executeGet(Calculations.targetUrl + "iso_date=" + yesterdaysDate, "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, Calculations.WONGAMES, s)
                }
            }
            return getTips(s)
        }

        private fun getTips(s: String?): ArrayList<GameTip> {
            val tips = ArrayList<GameTip>()
            if (s == null) return tips
            try {
                val jsonObject = JSONObject(s)
                val data = jsonObject.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val tipJSON = data.getJSONObject(i)
                    val gameTip = GameTip()
                    if (market == Calculations.WONGAMES && tipJSON.optString("status") != "won") continue
                    gameTip._id = tipJSON.optString("id")
                    gameTip.awayTeam = tipJSON.optString("away_team")
                    gameTip.homeTeam = tipJSON.optString("home_team")
                    gameTip.region = tipJSON.optString("competition_cluster")
                    gameTip.league = tipJSON.optString("competition_name")
                    gameTip.prediction = tipJSON.optString("prediction")
                    gameTip.time = tipJSON.optString("start_date")
                    gameTip.result = tipJSON.optString("result")
                    gameTip.status = tipJSON.optString("status")
                    if (tipJSON.has("probabilities")) {
                        val probabilities = tipJSON.optJSONObject("probabilities")
                        gameTip.probability = probabilities.optDouble(gameTip.prediction)
                    }
                    val oddJSON = tipJSON.getJSONObject("odds")
                    if (oddJSON != null) {
                        gameTip.odd = oddJSON.optDouble(gameTip.prediction)
                    }
                    tips.add(gameTip)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return tips
        }

        override fun onPostExecute(tips: ArrayList<GameTip>) {
            Log.i("GETTIPS", "onPostExecute: $tips")
            if (tips.isEmpty()) return
            when (market) {
                Calculations.CLASSIC -> {
                    classicTips.clear()
                    for (tip in tips) {
                        classicTips.add(tip)
                        if (classicTips.size >= 4) break
                    }
                    Log.i(TAG, "onPostExecute: classicTips:$classicTips")
                    runOnUiThread { classicAdapter.notifyDataSetChanged() }
                    shimmerLandingTip!!.stopShimmer()
                    shimmerLandingTip!!.visibility = View.GONE
                }
                Calculations.WONGAMES -> {
                    wonTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        wonTips.add(tip)
                        if (wonTips.size >= 6) break
                    }
                    Log.i(TAG, "onPostExecute: wonTips:$wonTips")
                    runOnUiThread { wonAdapter.notifyDataSetChanged() }
                    shimmerRecentWinnings!!.stopShimmer()
                    shimmerRecentWinnings!!.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private const val TAG = "LandActivityTAG"
    }
}