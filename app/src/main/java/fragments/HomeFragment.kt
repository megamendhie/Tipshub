package fragments

import adapters.*
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.sqube.tipshub.ExtendedHomeActivity
import com.sqube.tipshub.FullViewActivity
import com.sqube.tipshub.PostActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivitySignup2Binding
import com.sqube.tipshub.databinding.FragmentHomeBinding
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import models.*
import org.json.JSONException
import org.json.JSONObject
import utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binder: FragmentHomeBinding? = null
    private val binder get():FragmentHomeBinding = _binder!!
    private var txtError: TextView? = null
    private val homeFeedState = "homeFeedState"
    private val gson = Gson()
    private val _tag = "HomeFragment"
    private var prefs: SharedPreferences? = null
    private var fromEverybody = true
    private val postsList = ArrayList<Post?>()
    private val snapIds = ArrayList<SnapId>()
    private val trendingPostList = ArrayList<Post?>()
    private val trendingSnapIds = ArrayList<SnapId>()
    private var userId: String? = null
    private var username: String? = null
    private var postAdapter: PostAdapter? = null
    private var trendingAdapter: FilteredPostAdapter? = null

    //private SwipeRefreshLayout refresher;
    private val homepageTips = ArrayList<GameTip>()
    private var tipsAdapter: TipsAdapter? = null
    private var json: String? = null
    private var myProfile: ProfileMedium? = null
    private var subscriber = false
    private var filePath: Uri? = null
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private var imgDp: CircleImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        userId = user!!.uid
        username = user.displayName
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        json = prefs?.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        subscriber = myProfile != null && myProfile!!.isD4_vipSubscriber
        dbHelper = DatabaseHelper(context)
        db = dbHelper!!.readableDatabase
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binder = FragmentHomeBinding.inflate(inflater, container, false)

        binder.refresher.setColorSchemeResources(R.color.colorPrimary)
        binder.postList.layoutManager = LinearLayoutManager(context)
        binder.tipsList.layoutManager = LinearLayoutManager(context)
        binder.trendingList.layoutManager = LinearLayoutManager(context)
        binder.bankersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binder.sportSitesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        (binder.postList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        //((DefaultItemAnimator) trendingFeed.getItemAnimator()).setSupportsChangeAnimations(false);

        binder.txtOpenFullPost.setOnClickListener {seeMore() }
        binder.txtOpenFull.setOnClickListener{requireContext().startActivity(Intent(context, FullViewActivity::class.java)) }
        if (homepageTips.isEmpty()) binder.shimmerTips.startShimmer() else {
            binder.txtOpenFull.visibility = View.VISIBLE
            binder.shimmerTips.stopShimmer()
            binder.shimmerTips.visibility = View.GONE
            binder.crdTips.visibility = View.VISIBLE
        }
        binder.shimmerPosts.startShimmer()

        val intent = Intent(context, PostActivity::class.java)
        binder.fabPost.setOnClickListener { v: View? ->
            binder.fabMenu.close(false)
            if (hasReachedMax()) {
                popUp()
                return@setOnClickListener
            }
            intent.putExtra("type", "tip")
            startActivity(intent)
        }
        binder.fabNormal.setOnClickListener { v: View? ->
            binder.fabMenu.close(false)
            intent.putExtra("type", "normal")
            startActivity(intent)
        }

        //confirm if user is seeing everybody's post
        fromEverybody = prefs!!.getBoolean("fromEverybody", true)
        tipsAdapter = TipsAdapter(homepageTips)
        binder.tipsList.adapter = tipsAdapter
        onRefresh(savedInstanceState)
        loadBankerTipsters()
        loadSportSites()
        if (myProfile != null && (myProfile!!.a2_username.isEmpty() || myProfile!!.b1_phone.isEmpty())) promptForUsername()
        binder.refresher.setOnRefreshListener {
            binder.refresher.isRefreshing = true
            onRefresh(savedInstanceState)
            binder.refresher.isRefreshing = false
        }
        return binder.root
    }

    private fun onRefresh(savedInstanceState: Bundle?) {
        selectPostToLoad(savedInstanceState)
        loadTips()
        loadTrendingPost()
    }

    private fun seeMore() {
        val intent = Intent(context, ExtendedHomeActivity::class.java)
        intent.putExtra("fromEverybody", fromEverybody)
        startActivity(intent)
    }

    private fun loadSportSites() {
        val siteList = ArrayList<Website>()
        val websiteAdapter = WebsiteAdapter(siteList)
        binder.sportSitesList.adapter = websiteAdapter
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
        binder.bankersList.adapter = bankerTipsterAdapter
        bankerTipsterAdapter.startListening()
    }

    private fun loadTrendingPost() {
        trendingAdapter = FilteredPostAdapter(false, userId!!, requireContext(), trendingPostList, trendingSnapIds)
        binder.trendingList.adapter = trendingAdapter
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
        val dialogBinder = ActivitySignup2Binding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogBinder.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        //Initialize variables
        val numberValid = booleanArrayOf(false)
        txtError = dialogBinder.txtError
        dialogBinder.imgDp.setOnClickListener { grabImage() }
        dialogBinder.ccp.registerCarrierNumberEditText(dialogBinder.edtPhone)
        if (myProfile!!.b2_dpUrl.isEmpty())
            Glide.with(this).load(R.drawable.dummy).into(imgDp!!)
        else
            Glide.with(this).load(myProfile!!.b2_dpUrl).into(imgDp!!)
        dialogBinder.ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> numberValid[0] = isValidNumber }
        dialogBinder.btnSave.setOnClickListener { v: View? ->
            val rdbGroup = dialogBinder.rdbGroupGender
            val username = dialogBinder.edtUsername.text.toString().trim { it <= ' ' }
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
                txtError!!.text = "Enter username"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (username.length < 3) {
                dialogBinder.edtUsername.error = "Username too short"
                txtError!!.text = "Username too short"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phone)) {
                txtError!!.text = "Enter phone number"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!numberValid[0]) {
                txtError!!.text = "Phone number is incorrect"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(gender)) {
                txtError!!.text = "Select gender (M/F)"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            txtError!!.visibility = View.GONE
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
        txtError?.text = "Uploading image..."
        txtError?.visibility = View.VISIBLE
        FirebaseUtil.firebaseStorage?.reference?.child("profile_images")?.child(userId!!)!!.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url = uri.toString()
                                FirebaseUtil.firebaseFirestore?.collection("profiles")?.document(userId!!)!!.update("b2_dpUrl", url)
                                txtError!!.visibility = View.GONE
                                Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                                txtError!!.text = "Image uploaded"
                                imgDp!!.setImageURI(filePath)
                            }
                }
                .addOnFailureListener { e: Exception? -> txtError!!.text = "Could not upload image... Try again later" }
                .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                            .totalByteCount
                    txtError?.text = "$progress% completed"
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
        val getTips: GetTips = GetTips()
        getTips.execute()
    }

    private fun selectPostToLoad(savedInstanceState: Bundle?) {
        //refresher.setRefreshing(true);
        if (fromEverybody) {
            loadPostFbAdapter()
        } else {
            loadMerged()
        }
        if (savedInstanceState != null) {
            val homeFeedState = savedInstanceState.getParcelable<Parcelable>(homeFeedState)
            binder.postList.layoutManager!!.onRestoreInstanceState(homeFeedState)
        } else {
            val layoutManager = binder.postList.layoutManager as LinearLayoutManager?
            layoutManager!!.smoothScrollToPosition(binder.postList, null, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder.postList.adapter = null
        binder.trendingList.adapter = null
        Log.i(_tag, "onDestroyView: ")
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

    private fun loadPostFbAdapter() {
        val query = FirebaseUtil.firebaseFirestore?.collection("posts")!!
                .orderBy("time", Query.Direction.DESCENDING).limit(20)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, userId, requireContext(), true)
        binder.postList.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(_tag, "loadPost: started listening")
            postAdapter!!.startListening()
            binder.shimmerPosts.stopShimmer()
            binder.shimmerPosts.visibility = View.GONE
            binder.crdPosts.visibility = View.VISIBLE
        }
        //refresher.setRefreshing(false);
    }

    override fun onResume() {
        super.onResume()
        json = prefs!!.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        val subscriber = myProfile != null && myProfile!!.isD4_vipSubscriber
        if (subscriber && !this.subscriber) {
            this.subscriber = true
            loadTips()
        }
    }

    private fun loadMerged() {
        if (postAdapter != null) postAdapter!!.stopListening()
        if (UserNetwork.getFollowing() == null) {
            FirebaseUtil.firebaseFirestore?.collection("followings")?.document(userId!!)?.get()!!
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
            binder.postList.adapter = adapter
            val size = Math.min(20, postList2.size)
            for (i in 0 until size) {
                postsList.add(postList2[i])
                snapIds.add(snapIds2[i])
            }
            val i = postsList.size
            adapter.notifyDataSetChanged()
            postList2.clear()
            snapIds2.clear()
            binder.shimmerPosts.stopShimmer()
            binder.shimmerPosts.visibility = View.GONE
            binder.crdPosts.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val homeFeedState = binder.postList.layoutManager!!.onSaveInstanceState()
        outState.putParcelable(this.homeFeedState, homeFeedState)
        super.onSaveInstanceState(outState)
    }

    inner class GetTips : AsyncTask<String, Void, ArrayList<GameTip>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            val xml = dbHelper!!.getTip(db, Calculations.CLASSIC)
            if (xml != null && !xml.isEmpty()) onPostExecute(getTips(xml))
        }

        override fun doInBackground(vararg strings: String): ArrayList<GameTip> {
            val today = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = sdf.format(today.time)
            val httpConnection = HttpConFunction()
            val s = httpConnection.executeGet(Calculations.targetUrl + "iso_date=" + todayDate, "HOME")
            if (s != null && s.length >= 10) dbHelper!!.updateTip(db, Calculations.CLASSIC, s)
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
            //Log.i("GETTIPS", "onPostExecute: "+ tips);
            if (tips.isEmpty()) return
            homepageTips.clear()
            var k = 0
            for (tip in tips) {
                homepageTips.add(tip)
                k++
                if (k >= 3) break
            }
            tipsAdapter!!.notifyDataSetChanged()
            binder.txtOpenFull.visibility = View.VISIBLE
            binder.shimmerTips.stopShimmer()
            binder.shimmerTips.visibility = View.GONE
            binder.crdTips.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binder = null
    }
}