package com.sqube.tipshub.activities

import com.sqube.tipshub.adapters.PostAdapter
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import com.sqube.tipshub.adapters.FirebaseTipsAdapter
import com.sqube.tipshub.databinding.ActivityLandBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.models.Tip
import com.sqube.tipshub.utils.*
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import java.util.*

class LandActivity : AppCompatActivity() {
    private var _binding: ActivityLandBinding? = null
    private val binding get() =  _binding!!
    private val tipsAdapter = FirebaseTipsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding){
            listLandingTip.layoutManager = LinearLayoutManager(this@LandActivity)
            listLandingPost.layoutManager = LinearLayoutManager(this@LandActivity)
            shimmerLandingTip.startShimmer()
            listLandingTip.adapter = tipsAdapter
        }
        val query = firebaseFirestore!!.collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(6)
        val response = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        val postAdapter = PostAdapter(response, GUEST, this@LandActivity)
        binding.listLandingPost.adapter = postAdapter
        postAdapter.startListening()
        loadTips()
    }

    /**
     * load soccer tips from soccer
     */
    private fun loadTips() {
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
                var k = 0;
                for (snapshot in games.children) {
                    if(k>=4)
                        break
                    val tip = snapshot.getValue(Tip::class.java)!!
                    tips.add(tip)
                    k++
                }
                tipsAdapter.updateTips(tips)
                binding.shimmerLandingTip.stopShimmer()
                binding.shimmerLandingTip.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun goToLogin(v: View) {
        when (v.id) {
            R.id.btnJoin, R.id.btnLogin -> {
                val inTent = Intent(this@LandActivity, LoginActivity::class.java)
                inTent.putExtra("openMainActivity", true)
                startActivity(inTent)
                finish()
            }
            R.id.txtOpenFull, R.id.txtOpenPost -> showPrompt(Intent(this@LandActivity, LoginActivity::class.java))
            R.id.txtContact -> startActivity(Intent(this@LandActivity, ContactActivity::class.java))
            R.id.txtPolicy -> {
                val intent = CustomTabsIntent.Builder().setToolbarColor(resources.getColor(R.color.colorPrimary)).build()
                intent.launchUrl(this@LandActivity, Uri.parse("https://tipshub.co/privacy-policy/"))
            }
            R.id.btnHowTo, R.id.txtGuidelines -> startActivity(Intent(this@LandActivity, AboutActivity::class.java))
        }
    }

    private fun showPrompt(intent: Intent) {
        val builder = AlertDialog.Builder(this@LandActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { _: DialogInterface?, i: Int ->
                    intent.putExtra("openMainActivity", true)
                    startActivity(intent)
                    finish()
                }
                .show()
    }

}