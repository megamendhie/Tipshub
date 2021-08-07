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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
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
import com.hbb20.CountryCodePicker
import com.sqube.tipshub.ExtendedHomeActivity
import com.sqube.tipshub.FullViewActivity
import com.sqube.tipshub.PostActivity
import com.sqube.tipshub.R
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
    private val TAG = "HomeFrag"
    private val HOME_FEED_STATE = "homeFeedState"
    private var shimmerLayoutTips: ShimmerFrameLayout? = null
    private var shimmerLayoutPosts: ShimmerFrameLayout? = null
    private val gson = Gson()
    private var prefs: SharedPreferences? = null
    private var fromEverybody = true
    private val postList = ArrayList<Post?>()
    private val snapIds = ArrayList<SnapId>()
    private val trendingPostList = ArrayList<Post?>()
    private val trendingSnapIds = ArrayList<SnapId>()
    private var userId: String? = null
    private var username: String? = null
    private var postAdapter: PostAdapter? = null
    private var trendingAdapter: FilteredPostAdapter? = null
    private var fabMenu: FloatingActionMenu? = null
    private var homeFeed: RecyclerView? = null
    private var tipsFeed: RecyclerView? = null
    private var bankerTipstersFeed: RecyclerView? = null
    private var siteFeed: RecyclerView? = null
    private var trendingFeed: RecyclerView? = null
    private var intent: Intent? = null

    //private SwipeRefreshLayout refresher;
    private val homepageTips = ArrayList<GameTip>()
    private var tipsAdapter: TipsAdapter? = null
    private var crdTips: CardView? = null
    private var crdPosts: CardView? = null
    private var txtOpenFull: TextView? = null
    private var txtError: TextView? = null
    private var json: String? = null
    private var myProfile: ProfileMedium? = null
    private var subscriber = false
    private var filePath: Uri? = null
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private var imgDp: CircleImageView? = null
    private var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = FirebaseUtil.getFirebaseAuthentication().currentUser
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

        // Inflate the layout for this fragment only if its null
        if (rootView == null) rootView = inflater.inflate(R.layout.fragment_home, container, false)
        intent = Intent(context, PostActivity::class.java)
        homeFeed = rootView!!.findViewById(R.id.postList)
        tipsFeed = rootView!!.findViewById(R.id.tipsList)
        bankerTipstersFeed = rootView!!.findViewById(R.id.bankersList)
        siteFeed = rootView!!.findViewById(R.id.sportSitesList)
        trendingFeed = rootView!!.findViewById(R.id.trendingList)
        shimmerLayoutTips = rootView!!.findViewById(R.id.shimmerTips)
        shimmerLayoutPosts = rootView!!.findViewById(R.id.shimmerPosts)
        crdTips = rootView!!.findViewById(R.id.crdTips)
        crdPosts = rootView!!.findViewById(R.id.crdPosts)
        val refresher: SwipeRefreshLayout = rootView!!.findViewById(R.id.refresher)
        refresher.setColorSchemeResources(R.color.colorPrimary)
        homeFeed?.layoutManager = LinearLayoutManager(context)
        tipsFeed?.layoutManager = LinearLayoutManager(context)
        trendingFeed?.layoutManager = LinearLayoutManager(activity)
        bankerTipstersFeed?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        siteFeed?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        (homeFeed?.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        //((DefaultItemAnimator) trendingFeed.getItemAnimator()).setSupportsChangeAnimations(false);
        fabMenu = rootView!!.findViewById(R.id.fabMenu)
        val fabNormal: FloatingActionButton = rootView!!.findViewById(R.id.fabNormal)
        val fabTip: FloatingActionButton = rootView!!.findViewById(R.id.fabPost)
        //refresher = rootView.findViewById(R.id.refresher);
        //refresher.setColorSchemeResources(R.color.colorPrimary);
        val txtOpenFullPost = rootView!!.findViewById<TextView>(R.id.txtOpenFullPost)
        txtOpenFullPost.setOnClickListener { view: View? -> seeMore() }
        txtOpenFull = rootView!!.findViewById(R.id.txtOpenFull)
        txtOpenFull?.setOnClickListener(View.OnClickListener { view: View? -> requireContext().startActivity(Intent(context, FullViewActivity::class.java)) })
        if (homepageTips.isEmpty()) shimmerLayoutTips?.startShimmer() else {
            txtOpenFull?.visibility = View.VISIBLE
            shimmerLayoutTips?.stopShimmer()
            shimmerLayoutTips?.visibility = View.GONE
            crdTips?.visibility = View.VISIBLE
        }
        shimmerLayoutPosts?.startShimmer()
        fabTip.setOnClickListener { v: View? ->
            fabMenu?.close(false)
            if (hasReachedMax()) {
                popUp()
                return@setOnClickListener
            }
            intent!!.putExtra("type", "tip")
            startActivity(intent)
        }
        fabNormal.setOnClickListener { v: View? ->
            fabMenu?.close(false)
            intent!!.putExtra("type", "normal")
            startActivity(intent)
        }

        //confirm if user is seeing everybody's post
        fromEverybody = prefs!!.getBoolean("fromEverybody", true)
        tipsAdapter = TipsAdapter(homepageTips)
        tipsFeed?.adapter = tipsAdapter
        onRefresh(savedInstanceState)
        loadBankerTipsters()
        loadSportSites()
        if (myProfile != null && (myProfile!!.a2_username.isEmpty() || myProfile!!.b1_phone.isEmpty())) promptForUsername()
        refresher.setOnRefreshListener {
            refresher.isRefreshing = true
            onRefresh(savedInstanceState)
            refresher.isRefreshing = false
        }
        return rootView
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
        val siteList = ArrayList<Website?>()
        val websiteAdapter = WebsiteAdapter(siteList)
        siteFeed!!.adapter = websiteAdapter
        val ref = FirebaseDatabase.getInstance().reference.child("sportSites")
        ref.keepSynced(true)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                siteList.clear()
                for (snapshot in dataSnapshot.children) {
                    val website = snapshot.getValue(Website::class.java)
                    siteList.add(website)
                }
                websiteAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadBankerTipsters() {
        val query = FirebaseUtil.getFirebaseFirestore().collection("profiles").orderBy("e6c_WGP").whereEqualTo("c1_banker", true).limit(10)
        val options = FirestoreRecyclerOptions.Builder<ProfileShort>()
                .setQuery(query, ProfileShort::class.java)
                .build()
        val bankerTipsterAdapter = BankerTipsterAdapter(options)
        bankerTipstersFeed!!.adapter = bankerTipsterAdapter
        bankerTipsterAdapter.startListening()
    }

    private fun loadTrendingPost() {
        trendingAdapter = FilteredPostAdapter(false, userId, context, trendingPostList, trendingSnapIds)
        trendingFeed!!.adapter = trendingAdapter
        FirebaseUtil.getFirebaseFirestore().collection("posts")
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
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_signup2, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        //Initialize variables
        val numberValid = booleanArrayOf(false)
        txtError = dialog.findViewById(R.id.txtError)
        val edtUsername = dialog.findViewById<EditText>(R.id.edtUsername)
        val edtPhone = dialog.findViewById<EditText>(R.id.editText_carrierNumber)
        val rdbGroup = dialog.findViewById<RadioGroup>(R.id.rdbGroupGender)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        imgDp = dialog.findViewById(R.id.imgDp)
        imgDp!!.setOnClickListener { view: View? -> grabImage() }
        val ccp = dialog.findViewById<CountryCodePicker>(R.id.ccp)
        ccp!!.registerCarrierNumberEditText(edtPhone)
        if (myProfile!!.b2_dpUrl.isEmpty()) Glide.with(this).load(R.drawable.dummy).into(imgDp!!) else Glide.with(this).load(myProfile!!.b2_dpUrl).into(imgDp!!)
        ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> numberValid[0] = isValidNumber }
        btnSave!!.setOnClickListener { v: View? ->
            val username = edtUsername!!.text.toString().trim { it <= ' ' }
            val phone = ccp.fullNumber
            val country = ccp.selectedCountryName
            var gender = ""
            when (rdbGroup!!.checkedRadioButtonId) {
                R.id.rdbMale -> gender = "male"
                R.id.rdbFemale -> gender = "female"
            }

            //verify fields meet requirement
            if (TextUtils.isEmpty(username)) {
                edtUsername.error = "Enter username"
                txtError!!.text = "Enter username"
                txtError!!.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (username.length < 3) {
                edtUsername.error = "Username too short"
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
            FirebaseUtil.getFirebaseFirestore().collection("profiles")
                    .whereEqualTo("a2_username", username).limit(1).get()
                    .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        if (task.result == null || !task.result!!.isEmpty) {
                            edtUsername.error = "Username already exist"
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
                        FirebaseUtil.getFirebaseAuthentication().currentUser!!.updateProfile(profileUpdate)

                        //save username, phone number, and gender to database
                        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId!!)[url] = SetOptions.merge()
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
        txtError!!.text = "Uploading image..."
        txtError!!.visibility = View.VISIBLE
        FirebaseUtil.getFirebaseStorage().reference.child("profile_images").child(userId!!).putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url = uri.toString()
                                FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId!!).update("b2_dpUrl", url)
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
            val homeFeedState = savedInstanceState.getParcelable<Parcelable>(HOME_FEED_STATE)
            homeFeed!!.layoutManager!!.onRestoreInstanceState(homeFeedState)
        } else {
            val layoutManager = homeFeed!!.layoutManager as LinearLayoutManager?
            layoutManager!!.smoothScrollToPosition(homeFeed, null, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeFeed!!.adapter = null
        trendingFeed!!.adapter = null
        Log.i(TAG, "onDestroyView: ")
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
        val query = FirebaseUtil.getFirebaseFirestore().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING).limit(20)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, userId, context, true)
        homeFeed!!.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(TAG, "loadPost: started listening")
            postAdapter!!.startListening()
            shimmerLayoutPosts!!.stopShimmer()
            shimmerLayoutPosts!!.visibility = View.GONE
            crdPosts!!.visibility = View.VISIBLE
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
            FirebaseUtil.getFirebaseFirestore().collection("followings").document(userId!!).get()
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
            queries[i] = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("userId", userIds[i]).limit(10)
            tasks[i] = queries[i]!!.get()
        }
        Tasks.whenAllSuccess<Any?>(*tasks).addOnSuccessListener { list: List<Any?> ->
            postList.clear()
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
            val adapter = FilteredPostAdapter(true, userId, context, postList, snapIds)
            homeFeed!!.adapter = adapter
            val size = Math.min(20, postList2.size)
            for (i in 0 until size) {
                postList.add(postList2[i])
                snapIds.add(snapIds2[i])
            }
            val i = postList.size
            adapter.notifyDataSetChanged()
            postList2.clear()
            snapIds2.clear()
            shimmerLayoutPosts!!.stopShimmer()
            shimmerLayoutPosts!!.visibility = View.GONE
            crdPosts!!.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val homeFeedState = homeFeed!!.layoutManager!!.onSaveInstanceState()
        outState.putParcelable(HOME_FEED_STATE, homeFeedState)
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
                    } else Log.i(TAG, "getTips: null")
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
            txtOpenFull!!.visibility = View.VISIBLE
            shimmerLayoutTips!!.stopShimmer()
            shimmerLayoutTips!!.visibility = View.GONE
            crdTips!!.visibility = View.VISIBLE
        }
    }
}