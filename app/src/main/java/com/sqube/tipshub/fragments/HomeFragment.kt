package com.sqube.tipshub.fragments

import adapters.*
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.*
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.sqube.tipshub.*
import com.sqube.tipshub.R
import com.sqube.tipshub.activities.ExtendedHomeActivity
import com.sqube.tipshub.activities.FullViewActivity
import com.sqube.tipshub.activities.LeaguesActivity
import com.sqube.tipshub.activities.PostActivity
import com.sqube.tipshub.databinding.ActivitySignup2Binding
import com.sqube.tipshub.databinding.FragmentHomeBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.sqube.tipshub.interfaces.HeaderActions
import com.sqube.tipshub.models.*
import org.json.JSONException
import org.json.JSONObject
import com.sqube.tipshub.utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var headerListener: HeaderActions
    private var _binding: FragmentHomeBinding? = null
    private val binding get():FragmentHomeBinding = _binding!!
    private lateinit var dialogBinder: ActivitySignup2Binding
    private val homeFeedState = "homeFeedState"
    private val gson = Gson()
    private val _tag = "HomeFragment"
    private lateinit var prefs: SharedPreferences
    private val postsList = ArrayList<Post?>()
    private val snapIds = ArrayList<SnapId>()
    private val trendingPostList = ArrayList<Post?>()
    private val trendingSnapIds = ArrayList<SnapId>()
    private lateinit var userId: String
    private var username: String? = null
    private var postAdapter: PostAdapter? = null
    private var trendingAdapter: FilteredPostAdapter? = null

    private val homepageTips = ArrayList<GameTip>()
    private var tipsAdapter: TipsAdapter? = null
    private lateinit var json: String
    private var myProfile: ProfileMedium? = null
    private var subscriber = false
    private var filePath: Uri? = null
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private val customTabBuilder = CustomTabsIntent.Builder()
    private lateinit var customTab: CustomTabsIntent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        //Build CustomTabsIntent
        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(Color.parseColor("#1E73F4"))
            .build()
        customTabBuilder.setDefaultColorSchemeParams(colorScheme)
        customTab = customTabBuilder.build()

        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        userId = user!!.uid
        username = user.displayName
        prefs = requireContext().getSharedPreferences("${requireContext().applicationContext.packageName}_preferences", AppCompatActivity.MODE_PRIVATE)
        json = prefs.getString(PROFILE, "")!!
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        Log.i(_tag, "onCreate: done")
        subscriber = myProfile != null && myProfile!!.isD4_vipSubscriber
        dbHelper = DatabaseHelper(context)
        db = dbHelper!!.readableDatabase

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        dialogBinder = ActivitySignup2Binding.inflate(layoutInflater)
        binding.refresher.setColorSchemeResources(R.color.colorPrimary)
        binding.postList.layoutManager = LinearLayoutManager(context)
        binding.tipsList.layoutManager = LinearLayoutManager(context)
        binding.trendingList.layoutManager = LinearLayoutManager(context)
        binding.bankersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.sportSitesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        (binding.postList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        //((DefaultItemAnimator) trendingFeed.getItemAnimator()).setSupportsChangeAnimations(false);

        binding.txtOpenFullPost.setOnClickListener {seeMore() }
        binding.txtOpenFull.setOnClickListener{requireContext().startActivity(Intent(context, FullViewActivity::class.java)) }
        if (homepageTips.isEmpty()) binding.shimmerTips.startShimmer() else {
            binding.txtOpenFull.visibility = View.VISIBLE
            binding.shimmerTips.stopShimmer()
            binding.shimmerTips.visibility = View.GONE
            binding.crdTips.visibility = View.VISIBLE
        }
        binding.shimmerPosts.startShimmer()

        val intent = Intent(context, PostActivity::class.java)

        binding.fabPost.setOnClickListener { v: View? ->
            binding.fabMenu.close(false)
            if (hasReachedMax()) {
                popUp()
                return@setOnClickListener
            }
            intent.putExtra("type", "tip")
            startActivity(intent)
        }
        binding.fabNormal.setOnClickListener { v: View? ->
            binding.fabMenu.close(false)
            intent.putExtra("type", "normal")
            startActivity(intent)
        }
        binding.lnrMnu.setOnClickListener { headerListener.headerActionClick(0) }
        binding.lnrEpl.setOnClickListener { openSite("https://www.livescore.com/en/football/england/premier-league/")}
        binding.lnrLvs.setOnClickListener { openSite("http://www.hesgoal.com/leagues/11/Football_News") }
        binding.lnrNews.setOnClickListener {  headerListener.headerActionClick(3) }
        binding.lnrscore.setOnClickListener { openSite("https://www.livescore.com/") }
        binding.lnrMore.setOnClickListener { requireContext().startActivity(Intent(requireContext(), LeaguesActivity::class.java)) }

        tipsAdapter = TipsAdapter(homepageTips)
        binding.tipsList.adapter = tipsAdapter
        loadTips()
        selectPostToLoad()
        loadBankerTipsters()
        loadSportSites()
        loadTrendingPost()
        if (myProfile != null && (myProfile!!.a2_username.isEmpty() || myProfile!!.b1_phone.isEmpty())) promptForUsername()
        binding.refresher.setOnRefreshListener {
            binding.refresher.isRefreshing = true
            onRefresh()
            binding.refresher.isRefreshing = false
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        headerListener = context as HeaderActions
    }

    private fun openSite(url: String){
        customTab.launchUrl(requireContext(), Uri.parse(url))
    }

    fun scrollToTop(){
        binding.nestHome.smoothScrollTo(0,0)
    }

    private fun onRefresh() = selectPostToLoad()

    private fun seeMore() {
        val fromEverybody = prefs.getBoolean("fromEverybody", true)
        val intent = Intent(context, ExtendedHomeActivity::class.java)
        intent.putExtra("fromEverybody", fromEverybody)
        startActivity(intent)
    }

    private fun loadSportSites() {
        val siteList = ArrayList<Website>()
        val websiteAdapter = WebsiteAdapter(siteList)
        binding.sportSitesList.adapter = websiteAdapter
        val ref = FirebaseDatabase.getInstance().reference.child("sportSites")
        ref.keepSynced(true)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                siteList.clear()
                for (snapshot in dataSnapshot.children) {
                    val website = snapshot.getValue(Website::class.java)!!
                    siteList.add(website)
                }
                websiteAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadBankerTipsters() {
        val query = FirebaseUtil.firebaseFirestore?.collection("profiles")?.orderBy("e6c_WGP")?.whereEqualTo("c1_banker", true)!!.limit(10)
        val options = FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort::class.java)
                .build()
        val bankerTipsterAdapter = BankerTipsterAdapter(options)
        binding.bankersList.adapter = bankerTipsterAdapter
        bankerTipsterAdapter.startListening()
    }

    private fun loadTrendingPost() {
        trendingAdapter = FilteredPostAdapter(false, userId!!, requireContext(), trendingPostList, trendingSnapIds)
        binding.trendingList.adapter = trendingAdapter
        FirebaseUtil.firebaseFirestore?.collection("posts")!!
                .orderBy("timeRelevance", Query.Direction.DESCENDING).limit(30).get()
                .addOnSuccessListener { result: QuerySnapshot? ->
                    if (result == null || result.isEmpty) return@addOnSuccessListener
                    trendingSnapIds.clear()
                    trendingPostList.clear()
                    for (snapshot in result.documents) {
                        val post = snapshot.toObject(Post::class.java)
                        if (post!!.type == 6 && post.status != 2) continue
                        trendingPostList.add(post)
                        trendingSnapIds.add(SnapId(snapshot.id, post.time))
                    }
                    trendingAdapter!!.notifyDataSetChanged()
                }
    }

    private fun promptForUsername() {
        val builder = AlertDialog.Builder(requireContext())
        val dialog = builder.setView(dialogBinder.root).create()
        dialog.setCancelable(false)
        dialog.show()

        //Initialize variables
        val numberValid = booleanArrayOf(false)
        dialogBinder.imgDp.setOnClickListener { grabImage() }
        dialogBinder.ccp.registerCarrierNumberEditText(dialogBinder.edtPhone)
        if (myProfile!!.b2_dpUrl.isEmpty())
            Glide.with(this).load(R.drawable.dummy).into(dialogBinder.imgDp)
        else
            Glide.with(this).load(myProfile!!.b2_dpUrl).into(dialogBinder.imgDp)
        dialogBinder.ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> numberValid[0] = isValidNumber }
        dialogBinder.btnSave.setOnClickListener {
            val rdbGroup = dialogBinder.rdbGroupGender
            val username = dialogBinder.edtUsername.text.toString().replace("\\s".toRegex(), "")
            val phone = dialogBinder.ccp.fullNumber
            val country = dialogBinder.ccp.selectedCountryName
            var gender = ""
            when (rdbGroup.checkedRadioButtonId) {
                R.id.rdbMale -> gender = "male"
                R.id.rdbFemale -> gender = "female"
            }

            //verify fields meet requirement
            if (TextUtils.isEmpty(username)) {
                dialogBinder.edtUsername.error = "Enter username"
                dialogBinder.txtError.text = "Enter username"
                dialogBinder.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (username.length < 3) {
                dialogBinder.edtUsername.error = "Username too short"
                dialogBinder.txtError.text = "Username too short"
                dialogBinder.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phone)) {
                dialogBinder.txtError.text = "Enter phone number"
                dialogBinder.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!numberValid[0]) {
                dialogBinder.txtError.text = "Phone number is incorrect"
                dialogBinder.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(gender)) {
                dialogBinder.txtError.text = "Select gender (M/F)"
                dialogBinder.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            dialogBinder.txtError.visibility = View.GONE
            val finalGender = gender
            val ref = FirebaseUtil.firebaseFirestore?.collection("profiles")
            ref?.whereEqualTo("a2_username", username)!!.limit(1).get()
                    .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        if (task.result == null || !task.result!!.isEmpty) {
                            dialogBinder.edtUsername.error = "Username already exist"
                            Toast.makeText(context, "Username already exist. Try another one", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        //Map new user datails, and ready to save to db
                        val url: MutableMap<String, String?> = HashMap()
                        url["a2_username"] = username
                        url["a4_gender"] = finalGender
                        url["b0_country"] = country
                        url["b1_phone"] = phone

                        //set the new username to firebase auth user
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                        FirebaseUtil.firebaseAuthentication?.currentUser!!.updateProfile(profileUpdate)

                        //save username, phone number, and gender to database
                        ref.document(userId!!)[url] = SetOptions.merge()
                        Reusable.updateAlgoliaIndex(myProfile!!.a0_firstName, myProfile!!.a1_lastName, username, userId, myProfile!!.c2_score, true) //add to Algolia index
                        dialog.cancel()
                    }
        }
    }

    private fun grabImage() {
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(requireActivity())
    }

    private fun uploadImage() {
        dialogBinder.txtError.text = "Uploading image..."
        dialogBinder.txtError.visibility = View.VISIBLE
        FirebaseUtil.firebaseStorage?.reference?.child("profile_images")?.child(userId!!)!!.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url = uri.toString()
                                FirebaseUtil.firebaseFirestore?.collection("profiles")?.document(userId!!)!!.update("b2_dpUrl", url)
                                dialogBinder.txtError.visibility = View.GONE
                                Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                                dialogBinder.txtError.text = "Image uploaded"
                                dialogBinder.imgDp.setImageURI(filePath)
                            }
                }
                .addOnFailureListener { e: Exception? -> dialogBinder.txtError.text = "Could not upload image... Try again later" }
                .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                            .totalByteCount
                    dialogBinder.txtError.text = "$progress% completed"
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                filePath = result.uri
                uploadImage()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun loadTips() {
        val getTips = GetTips()
        getTips.execute()
    }

    fun selectPostToLoad() {
        val fromEverybody = prefs.getBoolean("fromEverybody", true)
        if (fromEverybody) {
            loadPostFbAdapter()
        } else {
            loadMerged()
        }
    }

    private fun popUp() {
        val message = """
            <p><span style="color: #F80051; font-size: 16px;"><strong>Tips limit reached</strong></span></p>
            <p>Take it easy, $username. You have reached your tips limit for today.</p>
            <p>To prevent spam, each person can post tips only 4 times in a day.
            But there is no limit to normal post. Enjoy!</p>
            """.trimIndent()
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomMaterialAlertDialog)
        builder.setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay") { dialogInterface: DialogInterface?, i: Int -> }
                .show()
    }

    //method checks if user has reached max post for the day
    private fun hasReachedMax(): Boolean {
        json = prefs.getString("profile", "")!!
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

    private fun loadPostFbAdapter() {
        val query = FirebaseUtil.firebaseFirestore?.collection("posts")!!
                .orderBy("time", Query.Direction.DESCENDING).limit(20)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, userId, requireContext())
        binding.postList.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(_tag, "loadPost: started listening")
            postAdapter!!.startListening()
            binding.shimmerPosts.stopShimmer()
            binding.shimmerPosts.visibility = View.GONE
            binding.crdPosts.visibility = View.VISIBLE
        }
        //refresher.setRefreshing(false);
    }

    private fun loadMerged() {
        if (postAdapter != null) postAdapter!!.stopListening()
        loadList(UserNetwork.following)
    }

    private fun loadList(ids: ArrayList<String>) {
        val userIds = ArrayList<String?>()
        userIds.add(userId)

        //check if following list has data
        if (ids.isNotEmpty()) {
            userIds.addAll(ids)
        }
        val count = userIds.size

        //create task and query for each followed id
        val queries = arrayOfNulls<Query>(count)
        val tasks = arrayOfNulls<Task<*>>(count)
        for (i in 0 until count) {
            queries[i] = FirebaseUtil.firebaseFirestore?.collection("posts")!!.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds[i]).limit(10)
            tasks[i] = queries[i]!!.get()
        }
        Tasks.whenAllSuccess<Any?>(*tasks).addOnSuccessListener { list: List<Any?> ->
            postsList.clear()
            snapIds.clear()
            val postList2 = ArrayList<Post?>()
            val snapIds2 = ArrayList<SnapId>()
            for (`object` in list) {
                val querySnapshot = `object` as QuerySnapshot?
                if (!querySnapshot?.isEmpty!!) {
                    for (snapshot in querySnapshot.documents) {
                        val post = snapshot.toObject(Post::class.java)
                        if (post!!.type == 6 && post.status != 2) continue
                        postList2.add(post)
                        snapIds2.add(SnapId(snapshot.id, post.time))
                    }
                }
            }
            if (postList2.size > 1) {
                Collections.sort(postList2)
                Collections.sort(snapIds2)
            }
            val adapter = FilteredPostAdapter(true, userId!!, requireContext(), postsList, snapIds)
            binding.postList.adapter = adapter
            val size = Math.min(20, postList2.size)
            for (i in 0 until size) {
                postsList.add(postList2[i])
                snapIds.add(snapIds2[i])
            }
            val i = postsList.size
            adapter.notifyDataSetChanged()
            postList2.clear()
            snapIds2.clear()
            binding.shimmerPosts.stopShimmer()
            binding.shimmerPosts.visibility = View.GONE
            binding.crdPosts.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val homeFeedState = binding.postList.layoutManager!!.onSaveInstanceState()
        outState.putParcelable(this.homeFeedState, homeFeedState)
        super.onSaveInstanceState(outState)
    }

    inner class GetTips : AsyncTask<String, Void, ArrayList<GameTip>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            val xml = dbHelper!!.getTip(db, CLASSIC)
            if (xml != null && !xml.isEmpty()) onPostExecute(getTips(xml))
        }

        override fun doInBackground(vararg strings: String): ArrayList<GameTip> {
            val today = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = sdf.format(today.time)
            val httpConnection = HttpConFunction()
            val s = httpConnection.executeGet(targetUrl + "iso_date=" + todayDate, "HOME")
            if (s != null && s.length >= 10) dbHelper!!.updateTip(db, CLASSIC, s)
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
                    } else Log.i(_tag, "getTips: null")
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
            homepageTips.clear()
            var k = 0
            for (tip in tips) {
                homepageTips.add(tip)
                k++
                if (k >= 3) break
            }
            tipsAdapter!!.notifyDataSetChanged()
            binding.txtOpenFull.visibility = View.VISIBLE
            binding.shimmerTips.stopShimmer()
            binding.shimmerTips.visibility = View.GONE
            binding.crdTips.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}