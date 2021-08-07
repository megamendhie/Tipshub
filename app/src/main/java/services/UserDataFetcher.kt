package services

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import models.ProfileMedium
import models.UserNetwork
import java.util.*

class UserDataFetcher : IntentService("UserDataFetcher") {
    var database: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var userID: String? = null
    var editor: SharedPreferences.Editor? = null
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs.edit()
        database = FirebaseFirestore.getInstance()
        FCM = FirebaseMessaging.getInstance()
        database!!.collection("profiles").document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: profile")
            if (documentSnapshot == null || !documentSnapshot.exists()) return@addSnapshotListener
            FCM!!.subscribeToTopic(userID!!) //subscribe user to corresponding channel with userId

            //set user profile to SharePreference
            val gson = Gson()
            Log.i(TAG, "onEvent: happended now")
            val json = gson.toJson(documentSnapshot.toObject(ProfileMedium::class.java))
            editor?.putBoolean("isVerified", documentSnapshot.toObject(ProfileMedium::class.java)!!.isC0_verified)
            editor?.putString("profile", json)
            editor?.apply()
        }

        //set user followers
        database!!.collection("followers").document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: followers")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.setFollowers(ArrayList())
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.setFollowers(documentSnapshot["list"] as ArrayList<String?>?)
            }
        }

        //set user followings
        database!!.collection("followings").document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: followings")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.setFollowing(ArrayList())
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.setFollowing(documentSnapshot["list"] as ArrayList<String?>?)
            }
        }

        //set user subscriptions
        database!!.collection("subscribed_to").document(userID!!).addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            Log.i(TAG, "onEvent: subscribed_to ")
            if (documentSnapshot == null) return@addSnapshotListener
            if (!documentSnapshot.exists()) {
                UserNetwork.setSubscribed(ArrayList())
                return@addSnapshotListener
            }
            if (documentSnapshot.exists() && documentSnapshot.contains("list")) {
                UserNetwork.setSubscribed(documentSnapshot["list"] as ArrayList<String?>?)
            }

            //subscribe for notification from people you have subscribed to
            if (UserNetwork.getSubscribed() == null || UserNetwork.getSubscribed().isEmpty()) return@addSnapshotListener
            for (s in (documentSnapshot["list"] as ArrayList<String>?)!!) {
                FCM!!.subscribeToTopic("sub_$s")
            }
        }
    }
}