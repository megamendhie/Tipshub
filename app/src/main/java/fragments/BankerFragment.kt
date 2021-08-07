package fragments

import adapters.BankerAdapter
import adapters.BankerTipsterAdapter
import adapters.FilteredBankerAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.sqube.tipshub.PostActivity
import com.sqube.tipshub.R
import models.*
import utils.FirebaseUtil
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
    private var prefs: SharedPreferences? = null
    private var txtNotice: TextView? = null
    private var userId: String? = null
    private var subscribedList: RecyclerView? = null
    private var latestList: RecyclerView? = null
    private var winningsList: RecyclerView? = null
    private var bankersList: RecyclerView? = null
    private val TAG = "BankerFragment"
    private var intent: Intent? = null
    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment only if its null
        if (rootView == null) rootView = inflater.inflate(R.layout.fragment_banker, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        subscribedList = rootView!!.findViewById(R.id.subscribedList)
        latestList = rootView!!.findViewById(R.id.latestList)
        winningsList = rootView!!.findViewById(R.id.winningsList)
        bankersList = rootView!!.findViewById(R.id.bankersList)
        val fabPost: FloatingActionButton = rootView!!.findViewById(R.id.fabPost)
        txtNotice = rootView!!.findViewById(R.id.txtNotice)
        (subscribedList?.getItemAnimator() as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        (winningsList?.getItemAnimator() as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        (latestList?.getItemAnimator() as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        subscribedList?.setLayoutManager(LinearLayoutManager(activity))
        latestList?.setLayoutManager(LinearLayoutManager(activity))
        winningsList?.setLayoutManager(LinearLayoutManager(activity))
        bankersList?.setLayoutManager(LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false))
        intent = Intent(context, PostActivity::class.java)
        val user = FirebaseUtil.getFirebaseAuthentication().currentUser
        userId = user!!.uid
        fabPost.setOnClickListener { v: View? ->
            json = prefs?.getString("profile", "")
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
        return rootView
    }

    private fun loadSub() {
        if (UserNetwork.getSubscribed() == null) {
            FirebaseUtil.getFirebaseFirestore().collection("subscribed_to").document(userId!!).get()
                    .addOnCompleteListener { task -> if (task.isSuccessful && task.result.contains("list")) loadList(task.result["list"] as ArrayList<String>?) else loadList(null) }
        } else loadList(UserNetwork.getSubscribed())
    }

    private fun loadList(userIds: ArrayList<String>?) {
        if (userIds == null || userIds.isEmpty()) {
            txtNotice!!.text = "You haven't subscribed to anyone yet"
            return
        }
        val count = userIds.size
        Log.i(TAG, "loadList: $userIds")
        //create task and query for each followed id
        val queries = arrayOfNulls<Query>(count)
        val tasks = arrayOfNulls<Task<*>>(count)
        for (i in 0 until count) {
            queries[i] = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds[i]).whereEqualTo("type", 6).limit(2)
            tasks[i] = queries[i]!!.get()
        }
        Tasks.whenAllSuccess<Any>(*tasks).addOnSuccessListener { list ->
            val posts = ArrayList<Post?>()
            val snapIds = ArrayList<SnapId>()
            for (`object` in list) {
                val querySnapshot = `object` as QuerySnapshot
                if (querySnapshot != null || !querySnapshot.isEmpty()) {
                    for (snapshot in querySnapshot.documents) {
                        val post = snapshot.toObject(Post::class.java)
                        posts.add(post)
                        snapIds.add(SnapId(snapshot.id, post!!.time))
                    }
                }
            }
            if (posts.isEmpty()) {
                txtNotice!!.text = "No tips at the moment"
                txtNotice!!.visibility = View.VISIBLE
            } else txtNotice!!.visibility = View.GONE
            Collections.sort(posts)
            Collections.sort(snapIds)
            subscribedList!!.adapter = FilteredBankerAdapter(userId, context, posts, snapIds)
        }
    }

    private fun loadBankerTipsters() {
        val query = FirebaseUtil.getFirebaseFirestore().collection("profiles").orderBy("e6c_WGP")
                .whereEqualTo("c1_banker", true).limit(10)
        val options = FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort::class.java)
                .build()
        val bankerTipsterAdapter = BankerTipsterAdapter(options)
        bankersList!!.adapter = bankerTipsterAdapter
        bankerTipsterAdapter.startListening()
    }

    private fun loadLatest() {
        Log.i(TAG, "loadPost: ")
        val latestAdapter = BankerAdapter(FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).limit(8), userId, context, true)
        latestList!!.adapter = latestAdapter
        if (latestAdapter != null) latestAdapter.startListening()
    }

    private fun loadWinning() {
        Log.i(TAG, "loadWinning: ")
        val winAdapter = BankerAdapter(FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("type", 6).whereEqualTo("status", 2).limit(8), userId, context, true)
        winningsList!!.adapter = winAdapter
        if (winAdapter != null) winAdapter.startListening()
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