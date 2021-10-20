package com.sqube.tipshub

import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import android.widget.TextView
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import services.UserDataFetcher
import com.google.firebase.auth.FirebaseAuth
import fragments.HomeFragment
import fragments.RecommendedFragment
import fragments.BankerFragment
import fragments.NotificationFragment
import android.widget.CompoundButton
import androidx.core.view.GravityCompat
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import services.NotificationCheckWorker
import services.DailyNotificationWorker
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.DocumentSnapshot
import android.text.Html
import android.content.DialogInterface
import android.widget.Toast
import models.ProfileMedium
import models.ProfileShort
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import models.UserNetwork
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.sqube.tipshub.databinding.ActivityMainBinding
import com.sqube.tipshub.databinding.NavHeaderBinding
import utils.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding
    private lateinit var bindingHeader: NavHeaderBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var user: FirebaseUser? = null
    private var serviceIntent: Intent? = null
    private val unseenNotList = ArrayList<String>()
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val gson = Gson()
    private val versionCode = BuildConfig.VERSION_CODE
    private val FB_RC_KEY_TITLE = "update_title"
    private val FB_RC_KEY_DESCRIPTION = "update_description"
    private val FB_RC_KEY_FORCE_UPDATE_VERSION = "force_update_version"
    private val FB_RC_KEY_LATEST_VERSION = "latest_version"
    private val defaultMap = HashMap<String, Any>()
    private val fragmentHome: HomeFragment = HomeFragment()
    private val fragmentRec: RecommendedFragment = RecommendedFragment()
    private val fragmentBanker: BankerFragment = BankerFragment()
    private val fragmentNot: NotificationFragment = NotificationFragment()
    private var fragmentActive: Fragment = fragmentHome
    private val fragmentManager = supportFragmentManager
    var userId: String? = null
    var tag = "Tipshub MainAct"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        bindingHeader = NavHeaderBinding.bind(binding.navView.getHeaderView(0))
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        //initialize Preference
        prefs = getSharedPreferences("${applicationContext.packageName}_preferences", MODE_PRIVATE)
        editor = prefs.edit()

        //initialize DrawerLayout and NavigationView
        binding.navView.setNavigationItemSelectedListener(this)
        val switchMenuItem = binding.navView.menu.findItem(R.id.nav_switch)
        val actionView = switchMenuItem.actionView
        val aSwitch: SwitchCompat = actionView.findViewById(R.id.drawer_switch)

        //confirm if user reading posts from everybody
        aSwitch.isChecked = prefs.getBoolean("fromEverybody", true)
        bindingHeader.imgProfilePic.setOnClickListener(this)
        bindingHeader.txtName.setOnClickListener(this)
        bindingHeader.txtUsername.setOnClickListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
        if (firebaseAuthentication!!.currentUser == null) {
            startActivity(Intent(this@MainActivity, LandActivity::class.java))
            finish()
            return
        }
        serviceIntent = Intent(this@MainActivity, UserDataFetcher::class.java)
        startService(serviceIntent)
        firebaseAuthentication!!.addAuthStateListener {
            if (it.currentUser == null) {
                editor.putBoolean(IS_VERIFIED, false)
                editor.putString(PROFILE, "")
                editor.apply()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra("openMainActivity", true)
                finish()
                startActivity(intent)
            }
            else
                firebaseFirestore!!.collection(PROFILES).document(it.currentUser!!.uid).get()
                        .addOnCompleteListener(this@MainActivity) {
                            if(it.isSuccessful && it.result != null && it.result.exists()){
                                val snapshot = it.result

                                //set user profile to SharePreference
                                Log.i(tag, "onEvent: happended now")
                                val json = gson.toJson(snapshot.toObject(ProfileMedium::class.java))
                                editor.putBoolean(IS_VERIFIED, snapshot.toObject(ProfileMedium::class.java)!!.isC0_verified)
                                editor.putString(PROFILE, json)
                                editor.apply()
                            }
            }
        }

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        user = firebaseAuthentication!!.currentUser
        userId = user!!.uid
        supportActionBar?.title = "Home"
        checkForUpdate()
        if (!timeIsValid()) popUp()
        setBadge()
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().apply {
                add(R.id.main_container, fragmentNot, FRAG_NOTIFICATION).hide(fragmentNot)
                add(R.id.main_container, fragmentBanker, FRAG_BANKER).hide(fragmentBanker)
                add(R.id.main_container, fragmentRec, FRAG_REC).hide(fragmentRec)
                add(R.id.main_container, fragmentHome, FRAG_HOME)
            }.commit()
        }
        else {
            fragmentManager.beginTransaction().show(fragmentActive).commit()
        }
        aSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            editor.apply { editor.putBoolean("fromEverybody", isChecked) }
            fragmentHome.selectPostToLoad()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        setWorkManager()
    }

    private fun setWorkManager() {
        val workManager = WorkManager.getInstance(this@MainActivity)
        if (prefs.getBoolean(WORKER_ACTIVATED, false)) {
            val id = UUID.fromString(prefs.getString(NOTIFICATION_WORKER_ID, ""))
            workManager.getWorkInfoByIdLiveData(id).observe(this, { workInfo: WorkInfo? ->
                if (workInfo == null) {
                    Log.i("WorkManager", "setWorkManager: Worker is null")
                    Snackbar.make(bindingHeader.imgProfilePic, "Worker is null", Snackbar.LENGTH_SHORT).show()
                }
            })
        }
        else {
            val workRequest = PeriodicWorkRequest.Builder(NotificationCheckWorker::class.java, 30, TimeUnit.MINUTES)
                    .build()
            workManager.enqueue(workRequest)
            val workRequestDaily = PeriodicWorkRequest.Builder(DailyNotificationWorker::class.java, 20, TimeUnit.HOURS)
                    .setInitialDelay(6, TimeUnit.HOURS).build()
            workManager.enqueue(workRequestDaily)
            val uuid = workRequest.id.toString()
            editor.putBoolean(WORKER_ACTIVATED, true)
            editor.putString(NOTIFICATION_WORKER_ID, uuid)
            editor.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceIntent != null) stopService(serviceIntent)
    }

    private fun setBadge() {
        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        val itemView = menuView.getChildAt(3) as BottomNavigationItemView //get notification item
        val notificationBadge = LayoutInflater.from(this).inflate(R.layout.notification_badge, menuView, false)
        notificationBadge.visibility = View.GONE
        val txtBadge = notificationBadge.findViewById<TextView>(R.id.badge)
        itemView.addView(notificationBadge)
        firebaseFirestore!!.collection("notifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("sendTo", userId).whereEqualTo("seen", false)
                .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        val count = queryDocumentSnapshots.size()
                        txtBadge.text = if (count >= 9) "$count+" else count.toString()
                        notificationBadge.visibility = View.VISIBLE
                        unseenNotList.clear()
                        for (snap in queryDocumentSnapshots.documents) unseenNotList.add(snap.id)
                    } else notificationBadge.visibility = View.GONE
                }
    }

    private fun clearNotification() {
        if (unseenNotList.isEmpty()) return
        for (i in unseenNotList.indices) {
            firebaseFirestore!!.collection("notifications").document(unseenNotList[i])
                    .update("seen", true)
        }
    }

    private fun popUp() {
        val message = """
            <p><span style="color: #F80051; font-size: 16px;"><strong>Error: Incorrect time</strong></span></p>
            <p>Your phone time is incorrect and this may affect some app functions. Kindly set your phone time.</p>
            """.trimIndent()
        val builder = AlertDialog.Builder(this@MainActivity, R.style.CustomMaterialAlertDialog)
        builder.setCancelable(false).setMessage(Html.fromHtml(message))
                .setNegativeButton("Okay") { dialogInterface: DialogInterface?, i: Int -> finish() }
                .show()
    }

    override fun onResume() {
        super.onResume()
        setHeader()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.recommended_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                binding.drawerLayout.closeDrawer(GravityCompat.START) else binding.drawerLayout.openDrawer(GravityCompat.START)
            R.id.mnuSearch -> {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_home -> {
                if (fragmentActive != fragmentHome) {
                    supportActionBar?.title = "Home"
                    fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentHome).commit()
                    fragmentActive = fragmentHome
                }
                else fragmentHome.scrollToTop()
                return true
            }
            R.id.nav_recommended -> {
                if (fragmentActive != fragmentRec) {
                    supportActionBar?.title = "Recommended"
                    fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentRec).commit()
                    fragmentActive = fragmentRec
                }
                else fragmentRec.scrollToTop()
                return true
            }
            R.id.nav_banker -> {
                if (fragmentActive != fragmentBanker) {
                    supportActionBar?.title = "Sure Banker"
                    fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentBanker).commit()
                    fragmentActive = fragmentBanker
                }
                else fragmentBanker.scrollToTop()
                return true
            }
            R.id.nav_notification -> {
                if (fragmentActive != fragmentNot) {
                    supportActionBar?.title = "Notifications"
                    fragmentManager.beginTransaction().hide(fragmentActive).show(fragmentNot).commit()
                    fragmentActive = fragmentNot
                    clearNotification()
                }
                else fragmentNot.scrollToTop()
                return true
            }
            R.id.nav_profile -> startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
            R.id.nav_contact -> startActivity(Intent(this@MainActivity, ContactActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            R.id.nav_guide -> startActivity(Intent(this@MainActivity, GuideActivity::class.java))
            R.id.nav_logout -> showLogoutPrompt()
            R.id.nav_wallet -> startActivity(Intent(this@MainActivity, AccountActivity::class.java))
        }
        return false
    }

    private fun showLogoutPrompt() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("Do you want to logout of Tipshub?")
                .setTitle("Logout")
                .setIcon(R.drawable.ic_power_settings_new_color_24dp)
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int -> if (firebaseAuthentication!!.currentUser != null) logout() else Toast.makeText(this@MainActivity, "No user logged in", Toast.LENGTH_LONG).show() }
                .show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgProfilePic, R.id.txtName, R.id.txtUsername -> startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }
    }

    private fun timeIsValid(): Boolean {
        val json = prefs.getString("profile", "")
        val myProfile = (if (json == "") null else gson.fromJson(json, ProfileMedium::class.java))
                ?: return true
        val currentTime = Date()
        val lastSeen = Date(myProfile.a8_lastSeen)
        if (lastSeen.after(currentTime)) return false
        Log.i("MainActivity", "timeIsValid: happended now")
        firebaseFirestore!!.collection("profiles").document(userId!!)
                .update("a8_lastSeen", currentTime.time)
        return true
    }

    private fun setHeader() {
        firebaseFirestore!!.collection("profiles").document(userId!!)
                .addSnapshotListener(this@MainActivity) { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val profile = documentSnapshot.toObject(ProfileShort::class.java)
                        bindingHeader.txtName.text = String.format(Locale.getDefault(), "%s %s", profile!!.a0_firstName, profile.a1_lastName)
                        bindingHeader.txtUsername.text = profile.a2_username
                        bindingHeader.txtTips.text = if (profile.e0a_NOG > 1) profile.e0a_NOG.toString() + " tips" else profile.e0a_NOG.toString() + " tip"
                        bindingHeader.txtFollowers.text = profile.c4_followers.toString()
                        bindingHeader.txtFollowing.text = profile.c5_following.toString()
                        Glide.with(this@MainActivity)
                                .load(profile.b2_dpUrl)
                                .into(bindingHeader.imgProfilePic)
                    }
                }
    }

    private fun logout() {
        val FCM = FirebaseMessaging.getInstance()
        FCM.unsubscribeFromTopic(userId!!)
        val sub_to = UserNetwork.subscribed
        if (sub_to != null && !sub_to.isEmpty()) {
            for (s in sub_to) {
                FCM.unsubscribeFromTopic("sub_$s")
            }
        }
        clearCache()
        if (user!!.providerData[1].providerId == "google.com") {
            firebaseAuthentication!!.signOut()
            mGoogleSignInClient!!.signOut()
        } else firebaseAuthentication!!.signOut()
    }

    private fun clearCache() {
        UserNetwork.followersList = null
        UserNetwork.followingList = null
        UserNetwork.subscribedList = null
    }

    private fun checkForUpdate() {
        // Hashmap which contains the default values for all the parameter defined in the remote config server
        defaultMap[FB_RC_KEY_TITLE] = "Update Available"
        defaultMap[FB_RC_KEY_DESCRIPTION] = "A new version of the application is available please click below to update the latest version."
        defaultMap[FB_RC_KEY_FORCE_UPDATE_VERSION] = "" + versionCode
        defaultMap[FB_RC_KEY_LATEST_VERSION] = "" + versionCode
        val cacheExpiration = if (BuildConfig.DEBUG) 60 else TimeUnit.HOURS.toSeconds(12)
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration).build()
        mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig!!.setDefaultsAsync(defaultMap)
        mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener(this) { task: Task<Boolean?> ->
            if (task.isSuccessful) {
                // Config data is successfully fetched and activated
                var visible = true
                val title = mFirebaseRemoteConfig!!.getString(FB_RC_KEY_TITLE)
                val description = mFirebaseRemoteConfig!!.getString(FB_RC_KEY_DESCRIPTION)
                val forceUpdateVersion = mFirebaseRemoteConfig!!.getString(FB_RC_KEY_FORCE_UPDATE_VERSION).toInt()
                val latestAppVersion = mFirebaseRemoteConfig!!.getString(FB_RC_KEY_LATEST_VERSION).toInt()
                Log.i(tag, "checkForUpdate: version code: " + versionCode + "latest code: " + latestAppVersion)
                if (latestAppVersion > versionCode) {
                    if (forceUpdateVersion > versionCode) visible = false
                    updateAlert(title, description, visible)
                }
            } else {
                Log.i(tag, "checkForUpdate: remote config fetch failed")
            }
        }
    }

    private fun updateAlert(title: String, description: String, visible: Boolean) {
        val builder = android.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.update_alert, null)
        builder.setView(dialogView).setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
        val btnUpdate = alertDialog.findViewById<Button>(R.id.btnUpdate)
        val btnLater = alertDialog.findViewById<TextView>(R.id.btnLater)
        btnLater.visibility = if (visible) View.VISIBLE else View.GONE
        btnUpdate.setOnClickListener { view: View? -> rateApp() }
        btnLater.setOnClickListener { view: View? -> alertDialog.cancel() }
        val txtTitle = alertDialog.findViewById<TextView>(R.id.txtTitle)
        val txtDescription = alertDialog.findViewById<TextView>(R.id.txtDescription)
        txtTitle.text = title
        txtDescription.text = Html.fromHtml(description)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(LAST_FRAGMENT, fragmentActive.tag)
        super.onSaveInstanceState(outState)
    }

    private fun rateApp() {
        val uri = Uri.parse("market://details?id=" + applicationContext.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}