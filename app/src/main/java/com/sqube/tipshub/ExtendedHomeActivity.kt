package com.sqube.tipshub

import adapters.FilteredPostAdapter
import adapters.PostAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.sqube.tipshub.databinding.ActivityExtendedHomeBinding
import models.Post
import models.ProfileMedium
import models.SnapId
import models.UserNetwork
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ExtendedHomeActivity : AppCompatActivity() {
    private var _binding: ActivityExtendedHomeBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private lateinit var prefs: SharedPreferences
    private var fromEverybody = true
    private var userId: String? = null
    private var username: String? = null
    private var json: String? = null
    private var myProfile: ProfileMedium? = null
    private val postList = ArrayList<Post?>()
    private val snapIds = ArrayList<SnapId>()
    private var postAdapter: PostAdapter? = null
    private var fAdapter: FilteredPostAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityExtendedHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        prefs = getSharedPreferences("${applicationContext.packageName}_preferences", MODE_PRIVATE)
        fromEverybody = intent.getBooleanExtra("fromEverybody", true)
        val user = firebaseAuthentication!!.currentUser
        if (user != null) {
            userId = user.uid
            username = user.displayName
        }
        fAdapter = FilteredPostAdapter(true, userId!!, this, postList, snapIds)
        json = prefs.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)

        binding.postList.layoutManager = LinearLayoutManager(this)
        val intentPost = Intent(this@ExtendedHomeActivity, PostActivity::class.java)
        binding.fabPost.setOnClickListener { v: View? ->
            binding.fabMenu.close(false)
            if (hasReachedMax()) {
                popUp()
                return@setOnClickListener
            }
            intentPost.putExtra("type", "tip")
            startActivity(intentPost)
        }
        binding.fabNormal.setOnClickListener { v: View? ->
            binding.fabMenu.close(false)
            intentPost.putExtra("type", "normal")
            startActivity(intentPost)
        }
        selectPostToLoad()
    }

    private fun selectPostToLoad() {
        if (fromEverybody) {
            loadPostFbAdapter()
        } else {
            binding.postList.adapter = fAdapter
            loadMerged()
        }
    }

    private fun loadPostFbAdapter() {
        val query = firebaseFirestore!!.collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(80)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, userId, this@ExtendedHomeActivity, false)
        binding.postList.adapter = postAdapter
        if (postAdapter != null) {
            postAdapter!!.startListening()
        }
    }

    private fun loadMerged() {
        if (postAdapter != null) postAdapter!!.stopListening()
        if (UserNetwork.getFollowing() == null) {
            firebaseFirestore!!.collection("followings").document(userId!!).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot> -> if (task.isSuccessful && task.result.contains("list")) loadList(task.result["list"] as ArrayList<String?>?) else loadList(null) }
        } else loadList(UserNetwork.getFollowing())
    }

    private fun loadList(ids: ArrayList<String?>?) {
        val userIds = ArrayList<String?>()
        userIds.add(userId)

        //check if following list has data
        if (ids != null && !ids.isEmpty()) {
            userIds.addAll(ids)
        }
        val count = userIds.size

        //create task and query for each followed id
        val queries = arrayOfNulls<Query>(count)
        val tasks = arrayOfNulls<Task<*>>(count)
        for (i in 0 until count) {
            queries[i] = firebaseFirestore!!.collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds[i]).limit(10)
            tasks[i] = queries[i]!!.get()
        }
        Tasks.whenAllSuccess<Any?>(*tasks).addOnSuccessListener { list: List<Any?> ->
            postList.clear()
            snapIds.clear()
            for (`object` in list) {
                val querySnapshot = `object` as QuerySnapshot?
                if (querySnapshot != null || !querySnapshot?.isEmpty!!) {
                    for (snapshot in querySnapshot.documents) {
                        val post = snapshot.toObject(Post::class.java)
                        if (post!!.type == 6 && post.status != 2) continue
                        postList.add(post)
                        snapIds.add(SnapId(snapshot.id, post.time))
                    }
                }
            }
            if (postList.size > 1) {
                Collections.sort(postList)
                Collections.sort(snapIds)
            }
            fAdapter!!.notifyDataSetChanged()
        }
    }

    private fun popUp() {
        val message = """
            <p><span style="color: #F80051; font-size: 16px;"><strong>Tips limit reached</strong></span></p>
            <p>Take it easy, $username. You have reached your tips limit for today.</p>
            <p>To prevent spam, each person can post tips only 4 times in a day.
            But there is no limit to normal post. Enjoy!</p>
            """.trimIndent()
        val builder = AlertDialog.Builder(this@ExtendedHomeActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay") { dialogInterface: DialogInterface?, i: Int -> }
                .show()
    }

    //method checks if user has reached max post for the day
    private fun hasReachedMax(): Boolean {
        json = prefs!!.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        if (myProfile == null) return true
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentTime = sdf.format(Date().time)
        val lastPostTime = sdf.format(Date(myProfile!!.c8_lsPostTime))
        try {
            val currentDate = sdf.parse(currentTime)
            val lastPostDate = sdf.parse(lastPostTime)
            if (currentDate.after(lastPostDate)) return false
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return myProfile!!.c9_todayPostCount >= 4
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}