package com.sqube.tipshub.activities

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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityFullViewBinding
import com.sqube.tipshub.models.Draw
import com.sqube.tipshub.models.GameTip
import com.sqube.tipshub.models.ProfileMedium
import org.json.JSONException
import org.json.JSONObject
import com.sqube.tipshub.utils.*
import java.text.SimpleDateFormat
import java.util.*

class FullViewActivity : AppCompatActivity() {
    private var _binding: ActivityFullViewBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private val allCurrentTips = ArrayList<GameTip>()
    private val classicTips = ArrayList<GameTip>()
    private val vipTips = ArrayList<GameTip>()
    private val overTips = ArrayList<GameTip>()
    private val bttTips = ArrayList<GameTip>()
    private val wonTips = ArrayList<GameTip>()
    private var classicAdapter: TipsAdapter? = null
    private var vipAdapter: TipsAdapter? = null
    private var overAdapter: TipsAdapter? = null
    private var bttsAdapter: TipsAdapter? = null
    private var wonAdapter: TipsAdapter? = null
    private val drawAdapter = DrawAdapter()
    private var prefs: SharedPreferences? = null
    private var subscriber = false
    private var CUT_OFF = 6
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private var getClassicTips: GetTips? = null
    private var getOverTips: GetTips? = null
    private var getBttsTips: GetTips? = null
    private var getWonTips: GetTips? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFullViewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_full_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.listClassic.layoutManager = LinearLayoutManager(this)
        binding.listVIP.layoutManager = LinearLayoutManager(this)
        binding.listOver.layoutManager = LinearLayoutManager(this)
        binding.listBts.layoutManager = LinearLayoutManager(this)
        binding.listDraw.layoutManager = LinearLayoutManager(this)
        binding.listWon.layoutManager = LinearLayoutManager(this)

        binding.shimmerClassicTips.startShimmer()
        binding.shimmerVipTips.startShimmer()
        binding.shimmerOverTips.startShimmer()
        binding.shimmerBtsTips.startShimmer()
        binding.shimmerDrawTips.startShimmer()
        binding.shimmerWonTips.startShimmer()
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
        if (subscriber) binding.btnSeeAll!!.visibility = View.GONE
        classicAdapter = TipsAdapter(classicTips)
        vipAdapter = TipsAdapter(vipTips)
        overAdapter = TipsAdapter(overTips)
        bttsAdapter = TipsAdapter(bttTips)
        wonAdapter = TipsAdapter(wonTips)
        binding.listClassic.adapter = classicAdapter
        binding.listVIP.adapter = vipAdapter
        binding.listOver.adapter = overAdapter
        binding.listBts.adapter = bttsAdapter
        binding.listDraw.adapter = drawAdapter
        binding.listWon.adapter = wonAdapter
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
        get() {
            val ref = FirebaseDatabase.getInstance().reference.child("SystemConfig").child("draws_vip")
            ref.keepSynced(true)
            ref.limitToFirst(1).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dSnap: DataSnapshot) {
                    if (!dSnap.hasChildren()) return
                    var dataSnapshot: DataSnapshot? = null
                    for (snap in dSnap.children) {
                        dataSnapshot = snap
                    }
                    val drawList = ArrayList<Draw>()
                    val date = dataSnapshot!!.child("date").getValue(String::class.java)
                    val week = dataSnapshot.child("week").getValue(String::class.java)
                    //String key = dataSnapshot.child("key").getValue(String.class);
                    val colorCode = dataSnapshot.child("colorCode").getValue(String::class.java)
                    binding.txtDate.text = date
                    binding.txtDate.visibility = View.VISIBLE
                    binding.txtWeek.text = week
                    binding.txtWeek.visibility = View.VISIBLE
                    val games = dataSnapshot.child("games")
                    for (snapshot in games.children) {
                        val draw = snapshot.getValue(Draw::class.java)
                        drawList.add(draw!!)
                    }
                    drawAdapter.setList(drawList, colorCode)
                    binding.shimmerDrawTips.stopShimmer()
                    binding.shimmerDrawTips.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun setLayoutVisibility() {
        if (subscriber) {
            binding.lnrVip.visibility = View.GONE
            binding.lnrDraw.visibility = View.GONE
            binding.shimmerVipTips.visibility = View.VISIBLE
            binding.shimmerDrawTips.visibility = View.VISIBLE
            binding.listVIP.visibility = View.VISIBLE
            binding.listDraw.visibility = View.VISIBLE
            CUT_OFF = 12
        } else {
            binding.lnrVip.visibility = View.VISIBLE
            binding.lnrDraw.visibility = View.VISIBLE
            binding.shimmerVipTips.visibility = View.GONE
            binding.shimmerDrawTips.visibility = View.GONE
            binding.listVIP.visibility = View.GONE
            binding.listDraw.visibility = View.GONE
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
        binding.shimmerVipTips.stopShimmer()
        binding.shimmerVipTips.visibility = View.GONE
    }

    inner class GetTips(private val market: String) : AsyncTask<String, Void, ArrayList<GameTip>>() {
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
                    binding.shimmerClassicTips.stopShimmer()
                    binding.shimmerClassicTips.visibility = View.GONE
                }
                OVER -> {
                    overTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        overTips.add(tip)
                        if (overTips.size >= 4) break
                    }
                    overAdapter!!.notifyDataSetChanged()
                    binding.shimmerOverTips.stopShimmer()
                    binding.shimmerOverTips.visibility = View.GONE
                }
                BTTS -> {
                    bttTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        bttTips.add(tip)
                        if (bttTips.size >= 4) break
                    }
                    bttsAdapter!!.notifyDataSetChanged()
                    binding.shimmerBtsTips.stopShimmer()
                    binding.shimmerBtsTips.visibility = View.GONE
                }
                WONGAMES -> {
                    wonTips.clear()
                    Collections.sort(tips)
                    for (tip in tips) {
                        wonTips.add(tip)
                        if (wonTips.size >= 6) break
                    }
                    wonAdapter!!.notifyDataSetChanged()
                    binding.shimmerWonTips.stopShimmer()
                    binding.shimmerWonTips.visibility = View.GONE
                }
            }
        }
    }

    fun openSub(view: View?) {
        startActivity(Intent(this@FullViewActivity, VipSubActivity::class.java))
    }
}