package com.sqube.tipshub

import adapters.PeopleAdapter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import utils.Calculations
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import java.util.*

class FollowerListActivity : AppCompatActivity() {
    private var peopleList: RecyclerView? = null
    private var txtNote: TextView? = null
    private var listOfPeople: ArrayList<String>? = ArrayList()
    private var userId: String? = null
    private var user: FirebaseUser? = null
    private var peopleAdapter: PeopleAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_list)
        val actionBar = supportActionBar
        val intent = intent
        val searchType = intent.getStringExtra("search_type")
        val personId = intent.getStringExtra("personId")
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(getTitle(searchType))
        }
        txtNote = findViewById(R.id.txtNote)
        peopleList = findViewById(R.id.peopleList)
        peopleList.setLayoutManager(LinearLayoutManager(this@FollowerListActivity))
        user = firebaseAuthentication!!.currentUser
        userId = if (user == null) Calculations.GUEST else user!!.uid
        firebaseFirestore!!.collection(searchType!!).document(personId!!).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.result == null || !task.result!!.exists()) {
                        txtNote.setVisibility(View.VISIBLE)
                        return@addOnCompleteListener
                    }
                    if (task.result!!.contains("list")) {
                        listOfPeople = task.result!!["list"] as ArrayList<String>?
                        peopleAdapter = PeopleAdapter(userId, listOfPeople!!)
                        peopleList.setAdapter(peopleAdapter)
                    }
                }
                .addOnFailureListener { e: Exception? -> txtNote.setVisibility(View.VISIBLE) }
    }

    public override fun onResume() {
        super.onResume()
        user = firebaseAuthentication!!.currentUser
        userId = if (user == null) Calculations.GUEST else user!!.uid
        if (peopleAdapter != null) peopleAdapter!!.setUserId(userId)
    }

    private fun getTitle(searchType: String?): String {
        return when (searchType) {
            "followings" -> "Following"
            "subscribers" -> "Subscribers"
            "subscribed_to" -> "Subscribed To"
            else -> "Followers"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}