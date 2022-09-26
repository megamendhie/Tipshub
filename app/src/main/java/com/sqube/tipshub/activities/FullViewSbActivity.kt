package com.sqube.tipshub.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.sqube.tipshub.adapters.DrawAdapter
import com.sqube.tipshub.adapters.FirebaseTipsAdapter
import com.sqube.tipshub.databinding.ActivityFullViewSbBinding
import com.sqube.tipshub.models.Draw
import com.sqube.tipshub.models.ProfileMedium
import com.sqube.tipshub.models.Tip
import com.sqube.tipshub.utils.TipsHolder
import java.util.*

class FullViewSbActivity : AppCompatActivity() {
    private var _binding: ActivityFullViewSbBinding? = null
    private val binding get() = _binding!!
    private val freeAdapter = FirebaseTipsAdapter()
    private var vipAdapter = FirebaseTipsAdapter()
    private val drawAdapter = DrawAdapter()
    private lateinit var prefs: SharedPreferences
    private var listFree = arrayListOf<Tip>()
    private var listVip = arrayListOf<Tip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFullViewSbBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        prefs = getSharedPreferences("${applicationContext.packageName}_preferences", MODE_PRIVATE)

        setLayoutManager()
        binding.shimmerFreeTips.startShimmer()

        loadFreeTips()
        loadVipTips()
        loadDrawTips()

        binding.fabFree.setOnClickListener {
            val intent = Intent(this, PostTipsActivity::class.java)
            intent.putExtra("FREE", true)
            TipsHolder.tipsList = arrayListOf(Tip())
            startActivity(intent)
        }
        binding.fabVip.setOnClickListener {
            val intent = Intent(this, PostTipsActivity::class.java)
            intent.putExtra("FREE", false)
            TipsHolder.tipsList = arrayListOf(Tip())
            startActivity(intent)
        }
        binding.imgFree.setOnClickListener {
            val intent = Intent(this, PostTipsActivity::class.java)
            intent.putExtra("FREE", true)
            TipsHolder.tipsList = listFree
            startActivity(intent)
        }
        binding.imgVip.setOnClickListener {
            val intent = Intent(this, PostTipsActivity::class.java)
            intent.putExtra("FREE", false)
            TipsHolder.tipsList = listVip
            startActivity(intent)
        }

    }

    private fun setLayoutManager() {
        binding.listFree.layoutManager = LinearLayoutManager(this)
        binding.listVIP.layoutManager = LinearLayoutManager(this)
        binding.listDraw.layoutManager = LinearLayoutManager(this)
        binding.listFree.adapter = freeAdapter
    }

    private fun loadFreeTips() {
        val ref = FirebaseDatabase.getInstance().reference.child("free_tips")
            .orderByChild("time")
        ref.keepSynced(true)
        ref.limitToFirst(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dSnap: DataSnapshot) {
                if (!dSnap.hasChildren()) return
                val dataSnapshot = dSnap.children.elementAt(0)
                val tips = arrayListOf<Tip>()
                if(!dataSnapshot.hasChild("games"))
                    return

                val games = dataSnapshot.child("games")
                for (snapshot in games.children) {
                    val tip = snapshot.getValue(Tip::class.java)!!
                    tips.add(tip)
                }
                freeAdapter.updateTips(tips)
                listFree = tips
                binding.shimmerFreeTips.stopShimmer()
                binding.shimmerFreeTips.visibility = GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadVipTips() {
        binding.lnrVip.visibility = GONE
        binding.shimmerVipTips.visibility = View.VISIBLE
        binding.shimmerVipTips.startShimmer()
        binding.listVIP.visibility = View.VISIBLE
        binding.listVIP.adapter = vipAdapter
        val ref = FirebaseDatabase.getInstance().reference.child("vip_tips")
            .orderByChild("time")
        ref.keepSynced(true)
        ref.limitToFirst(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dSnap: DataSnapshot) {
                if (!dSnap.hasChildren()) return
                val dataSnapshot = dSnap.children.elementAt(0)
                val tips = arrayListOf<Tip>()
                if(!dataSnapshot.hasChild("games"))
                    return

                val games = dataSnapshot.child("games")
                for (snapshot in games.children) {
                    val tip = snapshot.getValue(Tip::class.java)!!
                    tips.add(tip)
                }
                vipAdapter.updateTips(tips)
                listFree = tips
                binding.shimmerVipTips.stopShimmer()
                binding.shimmerVipTips.visibility = GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadDrawTips(){
        binding.lnrDraw.visibility = GONE
        binding.shimmerDrawTips.visibility = View.VISIBLE
        binding.shimmerDrawTips.startShimmer()
        binding.listDraw.visibility = View.VISIBLE
        binding.listDraw.adapter = drawAdapter

        val ref = FirebaseDatabase.getInstance().reference.child("SystemConfig")
            .child("draws_vip")
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
                binding.shimmerDrawTips.visibility = GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    fun openSub(view: View?) {
        startActivity(Intent(this, VipSubActivity::class.java))
    }
}