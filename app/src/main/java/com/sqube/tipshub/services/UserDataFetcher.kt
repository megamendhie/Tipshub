package com.sqube.tipshub.services

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.sqube.tipshub.models.ProfileMedium
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.utils.*
import java.util.*

class UserDataFetcher : IntentService("UserDataFetcher") {
    var database: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var userID: String? = null
    private val TAG = "UserDataFetcher"
    private var FCM: FirebaseMessaging? = null
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
    }

    override fun onHandleIntent(intent: Intent?) {
        auth = FirebaseAuth.getInstance()
        Log.i(TAG, "onCreate: ")
        if (auth!!.currentUser == null) onDestroy() else {
            userID = auth!!.currentUser!!.uid
            setUserData()
        }
    }

    private fun setUserData() {
        val prefs = getSharedPreferences("${applicationContext.packageName}_preferences", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        database = FirebaseFirestore.getInstance()
        FCM = FirebaseMessaging.getInstance()
        database!!.collection(PROFILES).document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: profile")
            if (documentSnapshot == null || !documentSnapshot.exists()) return@addSnapshotListener
            FCM!!.subscribeToTopic(userID!!) //subscribe user to corresponding channel with userId

            //set user profile to SharePreference
            val gson = Gson()
            Log.i(TAG, "onEvent: happended now")
            val json = gson.toJson(documentSnapshot.toObject(ProfileMedium::class.java))
            editor.putBoolean("isVerified", documentSnapshot.toObject(ProfileMedium::class.java)!!.isC0_verified)
            editor.putString(PROFILE, json)
            editor.apply()
        }

        //set user followers
        database!!.collection(FOLLOWERS).document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: followers")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.followersList = ArrayList()
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.followersList = documentSnapshot["list"] as ArrayList<String>?
            }
        }

        //set user followings
        database!!.collection(FOLLOWINGS).document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: followings")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.followingList = ArrayList()
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.followingList = documentSnapshot["list"] as ArrayList<String>?
            }
        }

        //set user subscriptions
        database!!.collection(SUBSCRIBED_TO).document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: subscribed_to ")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.subscribedList = ArrayList()
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.subscribedList = documentSnapshot["list"] as ArrayList<String>?
            }

            //subscribe for notification from people you have subscribed to
            if (UserNetwork.subscribed == null || UserNetwork.subscribed.isEmpty()) return@addSnapshotListener
            for (s in (documentSnapshot["list"] as ArrayList<String>?)!!) {
                FCM!!.subscribeToTopic("sub_$s")
            }
        }
    }
}