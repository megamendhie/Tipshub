package com.sqube.tipshub.activities

import adapters.PeopleAdapter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.sqube.tipshub.databinding.ActivityFollowerListBinding
import com.sqube.tipshub.utils.FirebaseUtil.firebaseAuthentication
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.GUEST
import java.util.*

class FollowerListActivity : AppCompatActivity() {
    private var _binding: ActivityFollowerListBinding? = null
    private val binding get() = _binding!!
    private var listOfPeople: ArrayList<String> = ArrayList()
    private lateinit var userId: String
    private var user: FirebaseUser? = null
    private var peopleAdapter: PeopleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFollowerListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        val searchType = intent.getStringExtra("search_type")
        val personId = intent.getStringExtra("personId")
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = getTitle(searchType)
        }
        binding.peopleList.layoutManager = LinearLayoutManager(this@FollowerListActivity)
        user = firebaseAuthentication!!.currentUser
        userId = user?.uid ?: GUEST
        firebaseFirestore!!.collection(searchType!!).document(personId!!).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.result == null || !task.isSuccessful || !task.result!!.exists()) {
                        binding.txtNote.visibility = View.VISIBLE
                        return@addOnCompleteListener
                    }
                    if (task.result!!.contains("list")) {
                        listOfPeople = task.result!!["list"] as ArrayList<String>
                        peopleAdapter = PeopleAdapter(userId, listOfPeople)
                        binding.peopleList.adapter = peopleAdapter
                    }
                }
                .addOnFailureListener { binding.txtNote.visibility = View.VISIBLE }
    }

    public override fun onResume() {
        super.onResume()
        user = firebaseAuthentication!!.currentUser
        userId = if (user == null) GUEST else user!!.uid
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