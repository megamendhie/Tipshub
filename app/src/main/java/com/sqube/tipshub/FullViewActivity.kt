package com.sqube.tipshub

import adapters.DrawAdapter
import adapters.TipsAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import models.Draw
import models.GameTip
import models.ProfileMedium
import org.json.JSONException
import org.json.JSONObject
import utils.DatabaseHelper
import utils.HttpConFunction
import java.text.SimpleDateFormat
import java.util.*

class FullViewActivity : AppCompatActivity() {
    var listClassic: RecyclerView? = null
    var listVIP: RecyclerView? = null
    var listOver: RecyclerView? = null
    var listBts: RecyclerView? = null
    var listDraw: RecyclerView? = null
    var listWon: RecyclerView? = null
    private val gson = Gson()
    private val allCurrentTips = ArrayList<GameTip>()
    private val classicTips = ArrayList<GameTip>()
    private val vipTips = ArrayList<GameTip>()
    private val overTips = ArrayList<GameTip>()
    private val bttTips = ArrayList<GameTip>()
    private val wonTips = ArrayList<GameTip>()
    private var txtDate: TextView? = null
    private var txtWeek: TextView? = null
    private var btnSeeAll: TextView? = null
    private var classicAdapter: TipsAdapter? = null
    private var vipAdapter: TipsAdapter? = null
    private var overAdapter: TipsAdapter? = null
    private var bttsAdapter: TipsAdapter? = null
    private var wonAdapter: TipsAdapter? = null
    private val drawAdapter = DrawAdapter()
    private var shimmerClassicTips: ShimmerFrameLayout? = null
    private var shimmerVipTips: ShimmerFrameLayout? = null
    private var shimmerOverTips: ShimmerFrameLayout? = null
    private var shimmerBtsTips: ShimmerFrameLayout? = null
    private var shimmerDrawTips: ShimmerFrameLayout? = null
    private var shimmerWonTips: ShimmerFrameLayout? = null
    private var prefs: SharedPreferences? = null
    private var lnrVip: RelativeLayout? = null
    private var lnrDraw: RelativeLayout? = null
    private var subscriber = false
    private var CUT_OFF = 6
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    var getClassicTips: GetTips? = null
    var getOverTips: GetTips? = null
    var getBttsTips: GetTips? = null
    var getWonTips: GetTips? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_view)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        listClassic = findViewById(R.id.listClassic)
        listVIP = findViewById(R.id.listVIP)
        listOver = findViewById(R.id.listOver)
        listBts = findViewById(R.id.listBts)
        listDraw = findViewById(R.id.listDraw)
        listWon = findViewById(R.id.listWon)
        lnrVip = findViewById(R.id.lnrVip)
        lnrDraw = findViewById(R.id.lnrDraw)
        txtDate = findViewById(R.id.txtDate)
        txtWeek = findViewById(R.id.txtWeek)
        btnSeeAll = findViewById(R.id.btnSeeAll)
        listClassic.setLayoutManager(LinearLayoutManager(this))
        listVIP.setLayoutManager(LinearLayoutManager(this))
        listOver.setLayoutManager(LinearLayoutManager(this))
        listBts.setLayoutManager(LinearLayoutManager(this))
        listDraw.setLayoutManager(LinearLayoutManager(this))
        listWon.setLayoutManager(LinearLayoutManager(this))
        shimmerClassicTips = findViewById(R.id.shimmerClassicTips)
        shimmerClassicTips.startShimmer()
        shimmerVipTips = findViewById(R.id.shimmerVipTips)
        shimmerVipTips.startShimmer()
        shimmerOverTips = findViewById(R.id.shimmerOverTips)
        shimmerOverTips.startShimmer()
        shimmerBtsTips = findViewById(R.id.shimmerBtsTips)
        shimmerBtsTips.startShimmer()
        shimmerDrawTips = findViewById(R.id.shimmerDrawTips)
        shimmerDrawTips.startShimmer()
        shimmerWonTips = findViewById(R.id.shimmerWonTips)
        shimmerWonTips.startShimmer()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        dbHelper = DatabaseHelper(this)
        db = dbHelper!!.readableDatabase
        getClassicTips = GetTips(CLASSIC)
        getOverTips = GetTips(OVER)
        getBttsTips = GetTips(BTTS)
        getWonTips = GetTips(WONGAMES)
    }

    override fun onPostResume() {
        super.onPostResume()
        val json = prefs!!.getString("profile", "")
        val myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        subscriber = myProfile != null && myProfile.isD4_vipSubscriber
        setLayoutVisibility()
        loadTips()
    }

    private fun loadTips() {
        if (subscriber) btnSeeAll!!.visibility = View.GONE
        classicAdapter = TipsAdapter(classicTips)
        vipAdapter = TipsAdapter(vipTips)
        overAdapter = TipsAdapter(overTips)
        bttsAdapter = TipsAdapter(bttTips)
        wonAdapter = TipsAdapter(wonTips)
        listClassic!!.adapter = classicAdapter
        listVIP!!.adapter = vipAdapter
        listOver!!.adapter = overAdapter
        listBts!!.adapter = bttsAdapter
        listDraw!!.adapter = drawAdapter
        listWon!!.adapter = wonAdapter
        try {
            getClassicTips!!.execute()
            getWonTips!!.execute()
            getOverTips!!.execute()
            getBttsTips!!.execute()
        } catch (e: Exception) {
            Log.i("FullView", "loadTips: " + e.message)
        }
        if (subscriber) {
            setVipTips()
            drawTips
        }
    }

    //String key = dataSnapshot.child("key").getValue(String.class);
    private val drawTips: Unit
        private get() {
            val ref = FirebaseDatabase.getInstance().reference.child("SystemConfig").child("draws_vip")
            ref.keepSynced(true)
            ref.limitToFirst(1).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dSnap: DataSnapshot) {
                    if (!dSnap.hasChildren()) return
                    var dataSnapshot: DataSnapshot? = null
                    for (snap in dSnap.children) {
                        dataSnapshot = snap
                    }
                    val drawList = ArrayList<Draw?>()
                    val date = dataSnapshot!!.child("date").getValue(String::class.java)
                    val week = dataSnapshot.child("week").getValue(String::class.java)
                    //String key = dataSnapshot.child("key").getValue(String.class);
                    val colorCode = dataSnapshot.child("colorCode").getValue(String::class.java)
                    txtDate!!.text = date
                    txtDate!!.visibility = View.VISIBLE
                    txtWeek!!.text = week
                    txtWeek!!.visibility = View.VISIBLE
                    val games = dataSnapshot.child("games")
                    for (snapshot in games.children) {
                        val draw = snapshot.getValue(Draw::class.java)
                        drawList.add(draw)
                    }
                    drawAdapter.setList(drawList, colorCode)
                    shimmerDrawTips!!.stopShimmer()
                    shimmerDrawTips!!.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun setLayoutVisibility() {
        if (subscriber) {
            lnrVip!!.visibility = View.GONE
            lnrDraw!!.visibility = View.GONE
            shimmerVipTips!!.visibility = View.VISIBLE
            shimmerDrawTips!!.visibility = View.VISIBLE
            listVIP!!.visibility = View.VISIBLE
            listDraw!!.visibility = View.VISIBLE
            CUT_OFF = 12
        } else {
            lnrVip!!.visibility = View.VISIBLE
            lnrDraw!!.visibility = View.VISIBLE
            shimmerVipTips!!.visibility = View.GONE
            shimmerDrawTips!!.visibility = View.GONE
            listVIP!!.visibility = View.GONE
            listDraw!!.visibility = View.GONE
            CUT_OFF = 6
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun setVipTips() {
        Collections.sort(allCurrentTips)
        for (tip in allCurrentTips) {
            vipTips.add(tip)
            if (vipTips.size >= 4) break
        }
        vipAdapter!!.notifyDataSetChanged()
        shimmerVipTips!!.stopShimmer()
        shimmerVipTips!!.visibility = View.GONE
    }

    inner class GetTips private constructor(private val market: String) : AsyncTask<String?, Void?, ArrayList<GameTip>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            var xml: String? = null
            when (market) {
                CLASSIC -> xml = dbHelper!!.getTip(db, CLASSIC)
                OVER -> xml = dbHelper!!.getTip(db, OVER)
                BTTS -> xml = dbHelper!!.getTip(db, BTTS)
                WONGAMES -> xml = dbHelper!!.getTip(db, WONGAMES)
            }
            if (xml != null && !xml.isEmpty()) onPostExecute(getTips(xml))
        }

        protected override fun doInBackground(vararg strings: String): ArrayList<GameTip> {
            val httpConnection = HttpConFunction()
            var s: String? = null
            val today = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = sdf.format(today.time)
            when (market) {
                CLASSIC -> {
                    s = httpConnection.executeGet(targetUrl + "iso_date=" + todayDate, "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, CLASSIC, s)
                }
                OVER -> {
                    s = httpConnection.executeGet(targetUrl + "iso_date=" + todayDate + "&market=over_25", "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, OVER, s)
                }
                BTTS -> {
                    s = httpConnection.executeGet(targetUrl + "iso_date=" + todayDate + "&market=btts", "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, BTTS, s)
                }
                WONGAMES -> {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterdaysDate = sdf.format(c.time)
                    s = httpConnection.executeGet(targetUrl + "iso_date=" + yesterdaysDate, "HOME")
                    if (s != null && s.length >= 10) dbHelper!!.updateTip(db, WONGAMES, s)
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
                    if (market == WONGAMES && tipJSON.optString("status") != "won") continue
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
            if (tips.isEmpty()) return
            when (market) {
                CLASSIC -> {
                    allCurrentTips.clear()
                    classicTips.clear()
                    for (tip in tips) {
                        allCurrentTips.add(tip)
                        if (classicTips.size < CUT_OFF) classicTips.add(tip)
                    }
                    classicAdapter!!.notifyDataSetChanged()
                    shimmerClassicTips!!.stopShimmer()
                    shimmerClassicTips!!.visibility = View.GONE
                }
                OVER -> {
                    overTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        overTips.add(tip)
                        if (overTips.size >= 4) break
                    }
                    overAdapter!!.notifyDataSetChanged()
                    shimmerOverTips!!.stopShimmer()
                    shimmerOverTips!!.visibility = View.GONE
                }
                BTTS -> {
                    bttTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        bttTips.add(tip)
                        if (bttTips.size >= 4) break
                    }
                    bttsAdapter!!.notifyDataSetChanged()
                    shimmerBtsTips!!.stopShimmer()
                    shimmerBtsTips!!.visibility = View.GONE
                }
                WONGAMES -> {
                    wonTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        wonTips.add(tip)
                        if (wonTips.size >= 6) break
                    }
                    wonAdapter!!.notifyDataSetChanged()
                    shimmerWonTips!!.stopShimmer()
                    shimmerWonTips!!.visibility = View.GONE
                }
            }
        }
    }

    fun openSub(view: View?) {
        startActivity(Intent(this@FullViewActivity, VipSubActivity::class.java))
    }
}