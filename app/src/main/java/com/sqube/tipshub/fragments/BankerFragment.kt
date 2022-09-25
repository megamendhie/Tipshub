package com.sqube.tipshub.fragments

import com.sqube.tipshub.adapters.BankerAdapter
import com.sqube.tipshub.adapters.BankerTipsterAdapter
import com.sqube.tipshub.adapters.FilteredBankerAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.sqube.tipshub.activities.PostActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.FragmentBankerBinding
import com.sqube.tipshub.models.*
import com.sqube.tipshub.utils.FirebaseUtil
import com.sqube.tipshub.utils.GUEST
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/*
   This class is attached to MainActivity
*/ /**
 * A simple [Fragment] subclass.
 */
class BankerFragment : Fragment() {
    private val gson = Gson()
    private var json: String? = null
    private var myProfile: ProfileMedium? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var userId: String
    private val TAG = "BankerFragment"
    private var intent: Intent? = null
    private lateinit var _binding: FragmentBankerBinding
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment only if its null
        _binding = FragmentBankerBinding.inflate(inflater, container, false)
        prefs = requireContext().getSharedPreferences("${requireContext().applicationContext.packageName}_preferences", AppCompatActivity.MODE_PRIVATE)

        (binding.subscribedList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        (binding.winningsList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        (binding.latestList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        binding.subscribedList.layoutManager = LinearLayoutManager(activity)
        binding.latestList.layoutManager = LinearLayoutManager(activity)
        binding.winningsList.layoutManager = LinearLayoutManager(activity)
        binding.bankersList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        intent = Intent(context, PostActivity::class.java)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        userId = user?.uid ?: GUEST
        binding.fabPost.setOnClickListener {
            json = prefs.getString("profile", "")
            myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
            if (myProfile == null) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!myProfile!!.isC1_banker) {
                popUp()
                return@setOnClickListener
            }
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val currentTime = sdf.format(Date().time)
            val lastBankerTime = sdf.format(Date(myProfile!!.d3_bankerPostTime))
            try {
                val currentDate = sdf.parse(currentTime)
                val lastPostDate = sdf.parse(lastBankerTime)
                if (currentDate == lastPostDate) {
                    popUp2()
                    return@setOnClickListener
                }
                intent!!.putExtra("type", "banker")
                startActivity(intent)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        loadBankerTipsters()
        loadLatest()
        loadWinning()
        loadSub()
        return binding.root
    }

    fun scrollToTop(){
        binding.nestBanker.smoothScrollTo(0,0)
    }

    private fun loadSub() {
        loadList(UserNetwork.subscribed)
    }

    private fun loadList(userIds: ArrayList<String>?) {
        if (userIds == null || userIds.isEmpty()) {
            binding.txtNotice.text = "You haven't subscribed to anyone yet"
            return
        }
        val count = userIds.size
        Log.i(TAG, "loadList: $userIds")
        //create task and query for each followed id
        val queries = arrayOfNulls<Query>(count)
        val tasks = arrayOfNulls<Task<*>>(count)
        for (i in 0 until count) {
            queries[i] = FirebaseUtil.firebaseFirestore?.collection("posts")?.orderBy("time", Query.Direction.DESCENDING)!!
                    .whereEqualTo("userId", userIds[i]).whereEqualTo("type", 6).limit(2)
            tasks[i] = queries[i]!!.get()
        }
        Tasks.whenAllSuccess<Any>(*tasks).addOnSuccessListener { list ->
            val posts = ArrayList<Post>()
            val snapIds = ArrayList<SnapId>()
            for (`object` in list) {
                val querySnapshot = `object` as QuerySnapshot
                if (querySnapshot != null || !querySnapshot.isEmpty) {
                    for (snapshot in querySnapshot.documents) {
                        val post = snapshot.toObject(Post::class.java)!!
                        posts.add(post)
                        snapIds.add(SnapId(snapshot.id, post.time))
                    }
                }
            }
            if (posts.isEmpty()) {
                binding.txtNotice.text = "No tips at the moment"
                binding.txtNotice.visibility = View.VISIBLE
            } else binding.txtNotice.visibility = View.GONE
            Collections.sort(posts)
            Collections.sort(snapIds)
            binding.subscribedList.adapter = FilteredBankerAdapter(userId, requireContext(), posts, snapIds)
        }
    }

    private fun loadBankerTipsters() {
        val query = FirebaseUtil.firebaseFirestore?.collection("profiles")?.orderBy("e6c_WGP")!!
                .whereEqualTo("c1_banker", true).limit(10)
        val options = FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort::class.java)
                .build()
        val bankerTipsterAdapter = BankerTipsterAdapter(options)
        binding.bankersList.adapter = bankerTipsterAdapter
        bankerTipsterAdapter.startListening()
    }

    private fun loadLatest() {
        Log.i(TAG, "loadPost: ")
        val bankerTipsRef = FirebaseUtil.firebaseFirestore?.collection("posts")
            ?.orderBy("time", Query.Direction.DESCENDING)!!.whereEqualTo("type", 6).limit(8)
        val latestAdapter = BankerAdapter(bankerTipsRef, userId, requireContext(), true)
        binding.latestList.adapter = latestAdapter
        latestAdapter.startListening()
    }

    private fun loadWinning() {
        Log.i(TAG, "loadWinning: ")
        val winAdapter = BankerAdapter(FirebaseUtil.firebaseFirestore?.collection("posts")?.orderBy("time", Query.Direction.DESCENDING)!!
                .whereEqualTo("type", 6).whereEqualTo("status", 2).limit(8), userId, requireContext(), true)
        binding.winningsList.adapter = winAdapter
        winAdapter.startListening()
    }

    private fun popUp() {
        val message = """
            <p><span style="color: #F80051;"><strong>Thanks for the interest</strong></span></p>
            <p>Unfortunately, you don&rsquo;t have the approval to post banker tips yet. Banker tips are mainly for your subscribers.</p>
            <p><span style="color: #F80051;"><strong>Conditions for approval</strong></span></p>
            <ul>
            <li>You have posted at least 50 tips.</li>
            <li>Won at least 70% of them.</li>
            <li>Be very active on the app.</li>
            </ul>
            """.trimIndent()
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomMaterialAlertDialog)
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay") { dialogInterface: DialogInterface?, i: Int -> }
                .show()
    }

    private fun popUp2() {
        val message = """
            <p><span style="color: #F80051;"><strong>You already predicted today</strong></span></p>
            <p>Sorry you cannot post any banker tip again for today. Give your subscribers your very surest tip for each day.</p>
            """.trimIndent()
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomMaterialAlertDialog)
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay") { dialogInterface: DialogInterface?, i: Int -> }
                .show()
    }
}