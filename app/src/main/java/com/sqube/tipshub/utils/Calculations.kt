package com.sqube.tipshub.utils

import android.content.Context
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.Reusable.Companion.getStatsForDelete
import com.sqube.tipshub.utils.Reusable.Companion.getStatsForWonPost
import com.google.gson.Gson
import com.sqube.tipshub.models.ProfileMedium
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.sqube.tipshub.models.UserNetwork
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.sqube.tipshub.models.Notification
import com.sqube.tipshub.models.Subscription
import java.lang.Exception
import java.util.*
import kotlin.math.max
import kotlin.math.pow

class Calculations(private val context: Context) {
    private val TAG = "Calculations"
    private val gson = Gson()
    private var json: String? = null
    private var myProfile: ProfileMedium? = null
    private var count: Long = 0
    private val prefs = context.getSharedPreferences(
        context.applicationContext.packageName + "_preferences",
        Context.MODE_PRIVATE
    )

    fun recommend(myId: String?, yourId: String) {
        if(UserNetwork.following.contains(yourId)) {
            val ref = firebaseFirestore!!.collection("recommended").document(myId!!)
                .collection("rec")
            ref.document(yourId).get().addOnSuccessListener { snapshot: DocumentSnapshot ->
                val update: MutableMap<String, Any> = HashMap()
                if (snapshot.exists()) {
                    update["count"] = snapshot.get("count", Long::class.java)!! + 1
                } else {
                    update["count"] = 1
                    update["id"] = yourId
                    update["dateAdded"] = Date().time
                }
                ref.document(yourId).set(update, SetOptions.merge())
                    .addOnFailureListener { e: Exception -> Log.i(TAG, "onFailure: " + e.message) }
            }
            ref.orderBy("dateAdded", Query.Direction.ASCENDING).get()
                .addOnSuccessListener { documentSnapshots: QuerySnapshot ->
                    if (documentSnapshots.size() >= 50) {
                        var counter = documentSnapshots.size() - 40
                        val time = Date().time
                        for (snapshot in documentSnapshots.documents) {
                            counter--
                            if (time - snapshot.get("dateAdded", Long::class.java)!! > 604800000)
                                snapshot.reference.delete() else break
                            if (counter >= 10) break
                        }
                    }
                }
        }
    }

    private fun unrecommend(myId: String, yourId: String) {
        firebaseFirestore!!.collection("recommended").document(myId)
            .collection("rec").document(yourId).get().addOnCompleteListener { task ->
                if (task.result != null && task.result!!.exists()) {
                    task.result!!.reference.delete()
                }
            }
    }

    fun getPostRelevance(like: Long, dislike: Long, repost: Long, comment: Long): Double {
        return (1 + like + repost + like * repost + comment * 0.5) / (1 + dislike)
    }

    fun getTimeRelevance(relevance: Double, time: Long): Double {
        val fixedValue: Long = 86400000
        val currentTime = Date().time
        val timeDifference = (currentTime - time) / fixedValue
        return relevance / 2.0.pow(timeDifference.toDouble())
    }

    private fun getCommentRelevance(like: Long, dislike: Long): Double {
        return like + 0.5 * dislike
    }

    private fun getUserRelevance(
        followers: Long,
        following: Long,
        subscribers: Long,
        subscribedTo: Long
    ): Double {
        return 2 * subscribers + subscribedTo + followers + 0.5 * following
    }

    fun onLike(postId: String?, userId: String, postOwnerId: String, subString: String?) {
        val postPath = firebaseFirestore!!.collection("posts").document(
            postId!!
        )
        val like = booleanArrayOf(true)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction ->
            Log.i(TAG, "apply: likes entered")
            val snapshot = transaction[postPath]
            //check if post still exists
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: like doesn't exist")
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            var likesCount = snapshot.getLong("likesCount")!!
            var dislikesCount = snapshot.getLong("dislikesCount")!!
            val repostCount = snapshot.getLong("repostCount")!!
            val commentsCount = snapshot.getLong("commentsCount")!!
            val time = snapshot.getLong("time")!!
            val likes: MutableList<String>? = snapshot["likes"] as MutableList<String>?
            val dislikes: MutableList<String>? = snapshot["dislikes"] as MutableList<String>?
            val upd: MutableMap<String, Any?> = HashMap()
            if (dislikes!!.contains(userId)) {
                like[0] = false
                dislikesCount -= 1
                likesCount += 1
                dislikes.remove(userId)
                likes!!.add(userId)
            } else {
                if (likes!!.contains(userId)) {
                    like[0] = false
                    likesCount -= 1
                    likes.remove(userId)
                } else {
                    setCount(likesCount)
                    likesCount += 1
                    likes.add(userId)
                }
            }
            val postRelevance =
                getPostRelevance(likesCount, dislikesCount, repostCount, commentsCount)
            val timeRelevance = getTimeRelevance(postRelevance, time)
            upd["likesCount"] = likesCount
            upd["dislikesCount"] = dislikesCount
            upd["likes"] = likes
            upd["dislikes"] = dislikes
            upd["relevance"] = postRelevance
            upd["timeRelevance"] = timeRelevance
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                if (like[0] && userId != postOwnerId) {
                    sendPushNotification(
                        true,
                        userId,
                        postOwnerId,
                        postId,
                        "liked",
                        "post",
                        subString
                    )
                    recommend(userId, postOwnerId)
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun onDislike(postId: String?, userId: String, postOwnerId: String, subString: String?) {
        val postPath = firebaseFirestore!!.collection("posts").document(
            postId!!
        )
        val dislike = booleanArrayOf(true)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            var likesCount = snapshot.getLong("likesCount")!!
            var dislikesCount = snapshot.getLong("dislikesCount")!!
            val repostCount = snapshot.getLong("repostCount")!!
            val commentsCount = snapshot.getLong("commentsCount")!!
            val time = snapshot.getLong("time")!!
            val likes: MutableList<String>? = snapshot["likes"] as MutableList<String>?
            val dislikes: MutableList<String>? = snapshot["dislikes"] as MutableList<String>?
            val upd: MutableMap<String, Any?> = HashMap()
            if (likes!!.contains(userId)) {
                dislike[0] = false
                likesCount -= 1
                dislikesCount += 1
                likes.remove(userId)
                dislikes!!.add(userId)
            } else {
                if (dislikes!!.contains(userId)) {
                    dislike[0] = false
                    dislikesCount -= 1
                    dislikes.remove(userId)
                } else {
                    setCount(dislikesCount)
                    dislikesCount += 1
                    dislikes.add(userId)
                }
            }
            val postRelevance =
                getPostRelevance(likesCount, dislikesCount, repostCount, commentsCount)
            val timeRelevance = getTimeRelevance(postRelevance, time)
            upd["likesCount"] = likesCount
            upd["dislikesCount"] = dislikesCount
            upd["likes"] = likes
            upd["dislikes"] = dislikes
            upd["relevance"] = postRelevance
            upd["timeRelevance"] = timeRelevance
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                Log.d(TAG, "Transaction success!")
                if (dislike[0] && userId != postOwnerId) {
                    Log.i(
                        TAG,
                        "onSuccess: recommended started" + dislike[0] + " " + (userId == postOwnerId)
                    )
                    sendPushNotification(
                        true,
                        userId,
                        postOwnerId,
                        postId,
                        "disliked",
                        "post",
                        subString
                    )
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun onCommentLike(
        userId: String,
        postOwnerId: String,
        postId: String?,
        mainPostId: String?,
        subString: String?
    ) {
        val commentRef = firebaseFirestore!!.collection("comments").document(
            postId!!
        )
        val like = booleanArrayOf(true)
        firebaseFirestore!!.runTransaction trans@{ transaction: Transaction ->
            Log.i(TAG, "apply: likes entered")
            val snapshot = transaction[commentRef]
            //check if post still exists
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: like doesn't exist")
                return@trans
            }

            //retrieve likes, likesCount, dislikes and dislikesCount from snapshot
            var likesCount = snapshot.getLong("likesCount")!!
            var dislikesCount = snapshot.getLong("dislikesCount")!!
            val time = snapshot.getLong("time")!!
            val likes: MutableList<String>? = snapshot["likes"] as MutableList<String>?
            val dislikes: MutableList<String>? = snapshot["dislikes"] as MutableList<String>?
            val upd: MutableMap<String, Any?> = HashMap()
            if (dislikes!!.contains(userId)) {
                like[0] = false
                dislikesCount -= 1
                likesCount += 1
                dislikes.remove(userId)
                likes!!.add(userId)
            } else {
                if (likes!!.contains(userId)) {
                    like[0] = false
                    likesCount -= 1
                    likes.remove(userId)
                } else {
                    setCount(likesCount)
                    likesCount += 1
                    likes.add(userId)
                }
            }
            val postRelevance = getCommentRelevance(likesCount, dislikesCount)
            val timeRelevance = getTimeRelevance(postRelevance, time)
            upd["likesCount"] = likesCount
            upd["dislikesCount"] = dislikesCount
            upd["likes"] = likes
            upd["dislikes"] = dislikes
            upd["relevance"] = postRelevance
            upd["timeRelevance"] = timeRelevance
            transaction.update(commentRef, upd)
        }
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                if (userId != postOwnerId && like[0]) {
                    sendPushNotification(
                        true,
                        userId,
                        postOwnerId,
                        mainPostId,
                        "liked",
                        "comment",
                        subString
                    )
                    recommend(userId, postOwnerId)
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }

        //send notification
    }

    fun onCommentDislike(
        userId: String,
        postOwnerId: String,
        postId: String?,
        mainPostId: String?,
        subString: String?
    ) {
        val commentRef = firebaseFirestore!!.collection("comments").document(
            postId!!
        )
        val dislike = booleanArrayOf(true)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[commentRef]
            //Check if post exist first
            if (!snapshot.exists()) {
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            var likesCount = snapshot.getLong("likesCount")!!
            var dislikesCount = snapshot.getLong("dislikesCount")!!
            val time = snapshot.getLong("time")!!
            val likes: MutableList<String>? = snapshot["likes"] as MutableList<String>?
            val dislikes: MutableList<String>? = snapshot["dislikes"] as MutableList<String>?
            val upd: MutableMap<String, Any?> = HashMap()
            if (likes!!.contains(userId)) {
                dislike[0] = false
                likesCount -= 1
                dislikesCount += 1
                likes.remove(userId)
                dislikes!!.add(userId)
            } else {
                if (dislikes!!.contains(userId)) {
                    dislike[0] = false
                    dislikesCount -= 1
                    dislikes.remove(userId)
                } else {
                    setCount(dislikesCount)
                    dislikesCount += 1
                    dislikes.add(userId)
                }
            }
            val postRelevance = getCommentRelevance(likesCount, dislikesCount)
            val timeRelevance = getTimeRelevance(postRelevance, time)
            upd["likesCount"] = likesCount
            upd["dislikesCount"] = dislikesCount
            upd["likes"] = likes
            upd["dislikes"] = dislikes
            upd["relevance"] = postRelevance
            upd["timeRelevance"] = timeRelevance
            transaction.update(commentRef, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                if (userId != postOwnerId && dislike[0]) {
                    sendPushNotification(
                        true,
                        userId,
                        postOwnerId,
                        mainPostId,
                        "disliked",
                        "comment",
                        subString
                    )
                    recommend(userId, postOwnerId)
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
        //send notification
    }

    fun increaseSubscriptions(myId: String, yourId: String) {
        Log.i(TAG, "increaseSubscriptions: $myId $yourId")
        val postPath = firebaseFirestore!!.collection("profiles").document(myId)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: snapshot is empty")
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            val followers = snapshot.getLong("c4_followers")!!
            val following = snapshot.getLong("c5_following")!!
            val subscribers = snapshot.getLong("c6_subscribers")!!
            val subscriptions = snapshot.getLong("c7_subscriptions")!! + 1
            val score = getUserRelevance(followers, following, subscribers, subscriptions)
            val upd: MutableMap<String, Any> = HashMap()
            upd["c2_score"] = score
            upd["c4_followers"] = followers
            upd["c5_following"] = following
            upd["c6_subscribers"] = subscribers
            upd["c7_subscriptions"] = subscriptions
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                //Add person's id to user's subscription list
                firebaseFirestore!!.collection("subscribed_to").document(myId).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                        val dS = task.result
                        if (dS == null || !dS.exists()) {
                            val list = ArrayList<String>()
                            list.add(yourId)
                            val upd: MutableMap<String, Any> = HashMap()
                            upd["list"] = list
                            firebaseFirestore!!.collection("subscribed_to").document(myId).set(upd)
                        } else firebaseFirestore!!.collection("subscribed_to").document(myId)
                            .update("list", FieldValue.arrayUnion(yourId))
                    }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun increaseSubscribers(myId: String, yourId: String?, sub: Subscription?) {
        val postPath = firebaseFirestore!!.collection("profiles").document(
            yourId!!
        )
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: snapshot is empty")
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            val followers = snapshot.getLong("c4_followers")!!
            val following = snapshot.getLong("c5_following")!!
            val subscribers = snapshot.getLong("c6_subscribers")!! + 1
            val subscriptions = snapshot.getLong("c7_subscriptions")!!
            val score = getUserRelevance(followers, following, subscribers, subscriptions)
            val upd: MutableMap<String, Any> = HashMap()
            upd["c2_score"] = score
            upd["c4_followers"] = followers
            upd["c5_following"] = following
            upd["c6_subscribers"] = subscribers
            upd["c7_subscriptions"] = subscriptions
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")

                //Add user to subscribed person's list
                firebaseFirestore!!.collection("subscribers").document(yourId).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                        val dS = task.result
                        if (dS == null || !dS.exists()) {
                            val list = ArrayList<String>()
                            list.add(myId)
                            val upd: MutableMap<String, Any> = HashMap()
                            upd["list"] = list
                            firebaseFirestore!!.collection("subscribers").document((yourId))
                                .set(upd)
                                .addOnSuccessListener { //adds sub to subscription table
                                    firebaseFirestore!!.collection("subscriptions").add((sub)!!)
                                }
                        } else firebaseFirestore!!.collection("subscribers").document((yourId))
                            .update("list", FieldValue.arrayUnion(myId))
                            .addOnSuccessListener { //adds sub to subscription table
                                firebaseFirestore!!.collection("subscriptions").add((sub)!!)
                            }
                    }
                Log.d(TAG, "Transaction success!")
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendPushNotification(
        addToInbox: Boolean, myId: String?, posterId: String?, intentUrl: String?,
        action: String, postType: String?, message: String?
    ) {
        val title: String
        json = prefs.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        if (myProfile == null) return
        title = if (action == "mentioned you") String.format(
            "%s mentioned you in a %s",
            myProfile!!.a2_username,
            postType
        ) else {
            when (getCount()) {
                0, 1 -> String.format("%s %s your %s", myProfile!!.a2_username, action, postType)
                else -> String.format(
                    "%s and %d others %s your %s",
                    myProfile!!.a2_username,
                    getCount(),
                    action,
                    postType
                )
            }
        }
        val notification = Notification(
            action,
            title,
            message,
            postType,
            intentUrl,
            myProfile!!.b3_dpTmUrl,
            posterId,
            myId
        )
        firebaseFirestore!!.collection("notifications").whereEqualTo("intentUrl", intentUrl)
            .whereEqualTo("action", action).whereEqualTo("type", postType).get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (!task.result.isEmpty) {
                    for (snapshot in task.result.documents) {
                        snapshot.reference.delete()
                    }
                }
                firebaseFirestore!!.collection("notifications").add(notification)
                val db = FirebaseDatabase.getInstance()
                val dbReference = db.reference
                dbReference.child("notifications").push().setValue(notification)
            }
            .addOnFailureListener { e: Exception? -> Log.i(TAG, "onFailure: Failed right here") }
        //if addToInbox is true, then send to user's inbox;
    }

    private fun sendNotification(myId: String, userId: String) {
        //sends notification to tipster
        json = prefs.getString("profile", "")
        myProfile = if (json == "") null else gson.fromJson(json, ProfileMedium::class.java)
        if (myProfile == null) return
        val title = myProfile!!.a2_username + " followed you"
        val message = "view profile"
        val notification = Notification(
            "followed", title, message, "following",
            myId, myProfile!!.b2_dpUrl, userId, myId
        )
        firebaseFirestore!!.collection("notifications").whereEqualTo("sendTo", userId)
            .whereEqualTo("type", "following").get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.result != null && !task.result!!
                        .isEmpty
                ) {
                    if (task.result!!.documents.size > 5) {
                        var c = 0
                        for (snapshot in task.result!!.documents) {
                            c++
                            if (c <= 5) continue
                            snapshot.reference.delete()
                        }
                    }
                }
                firebaseFirestore!!.collection("notifications").add(notification)
                val db = FirebaseDatabase.getInstance()
                val dbReference = db.reference

                //push to
                dbReference.child("notifications").push().setValue(notification)
            }
            .addOnFailureListener { e: Exception? -> Log.i(TAG, "onFailure: Failed right here") }
    }

    private fun getCount(): Int {
        return count.toInt()
    }

    fun setCount(count: Long) {
        this.count = count
    }

    fun followMember(v: View?, myId: String, yourId: String) {
        val postPath = firebaseFirestore!!.collection("profiles").document(myId)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: snapshot is empty")
                return@Function null
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            val followers = snapshot.getLong("c4_followers")!!
            val following = snapshot.getLong("c5_following")!! + 1
            val subscribers = snapshot.getLong("c6_subscribers")!!
            val subscriptions = snapshot.getLong("c7_subscriptions")!!
            val score = getUserRelevance(followers, following, subscribers, subscriptions)
            val upd: MutableMap<String, Any> = HashMap()
            upd["c2_score"] = score
            upd["c5_following"] = following
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                Snackbar.make(v!!, "Followed", Snackbar.LENGTH_SHORT).show()

                //Add the person's id to user's following list
                firebaseFirestore!!.collection("followings").document(myId).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                        val dS = task.result
                        if (dS == null || !dS.exists()) {
                            val list = ArrayList<String>()
                            list.add(yourId)
                            val upd: MutableMap<String, Any> = HashMap()
                            upd["list"] = list
                            firebaseFirestore!!.collection("followings").document(myId).set(upd)
                        } else firebaseFirestore!!.collection("followings").document(myId)
                            .update("list", FieldValue.arrayUnion(yourId))
                    }

                ///retrieve followers, followings, subscribers, and subscribed_to of the active user
                firebaseFirestore!!.collection("followers").document(yourId).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                        val dS = task.result
                        if (dS == null || !dS.exists()) {
                            val list = ArrayList<String>()
                            list.add(myId)
                            val upd: MutableMap<String, Any> = HashMap()
                            upd["list"] = list
                            firebaseFirestore!!.collection("followers").document(yourId)
                                .set(upd)
                        } else firebaseFirestore!!.collection("followers").document(yourId)
                            .update("list", FieldValue.arrayUnion(myId))
                    }
                updateMember(yourId, true)
                sendNotification(myId, yourId)
                unrecommend(myId, yourId)
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateMember(yourId: String, addFollower: Boolean) {
        val postPath = firebaseFirestore!!.collection("profiles").document(yourId)
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                return@Function null
            }
            //retrieve followers, followings, subscribers, and subscribed_to of the target user
            var followers = snapshot.getLong("c4_followers")!!
            followers = if(addFollower)  followers+1 else followers-1
            val following = snapshot.getLong("c5_following")!!
            val subscribers = snapshot.getLong("c6_subscribers")!!
            val subscriptions = snapshot.getLong("c7_subscriptions")!!
            val score =
                getUserRelevance(followers, following, subscribers, subscriptions)
            val upd: MutableMap<String, Any> = HashMap()
            upd["c2_score"] = score
            upd["c4_followers"] = followers
            transaction.update(postPath, upd)
            null
        })
    }

    fun unfollowMember(v: View?, myId: String?, yourId: String?) {
        val postPath = firebaseFirestore!!.collection("profiles").document(
            myId!!
        )
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                return@Function null
            }

            //retrieve followers, followings, subscribers, and subscribed_to of the active user
            val followers = snapshot.getLong("c4_followers")!!
            val following = max(0, snapshot.getLong("c5_following")!! - 1)
            val subscribers = snapshot.getLong("c6_subscribers")!!
            val subscriptions = snapshot.getLong("c7_subscriptions")!!
            val score = getUserRelevance(followers, following, subscribers, subscriptions)
            val upd: MutableMap<String, Any> = HashMap()
            upd["c2_score"] = score
            upd["c5_following"] = following
            transaction.update(postPath, upd)
            null
        })
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                Snackbar.make(v!!, "Unfollowed", Snackbar.LENGTH_SHORT).show()

                //Add followed id to current user's following list
                firebaseFirestore!!.collection("followings").document(myId)
                    .update("list", FieldValue.arrayRemove(yourId))

                //Add current user to followed person's list
                firebaseFirestore!!.collection("followers").document(yourId!!)
                    .update("list", FieldValue.arrayRemove(myId))
                updateMember(yourId, false)
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(TAG, "Transaction failure.", e)
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun onDeletePost(
        view: ImageView?,
        postId: String?,
        userId: String?,
        wonStatus: Boolean,
        type: Int
    ) {
        val postPath = firebaseFirestore!!.collection("profiles").document(
            userId!!
        )
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]

            //Check if post exist first
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: snapshot is empty")
                return@Function null
            }
            val profile = snapshot.toObject(ProfileMedium::class.java)
            val updates: MutableMap<String, Any> = HashMap()

            //Retrieve general stats
            val totalPostCount = Math.max(0, profile!!.e0a_NOG - 1)
            var wonGamesCount = profile.e0b_WG
            wonGamesCount = if (wonStatus) wonGamesCount - 1 else wonGamesCount
            val wonGamesPercentage =
                if (totalPostCount > 0) wonGamesCount * 100 / totalPostCount else 0

            //retrieve stat for that game type
            val stats = getStatsForDelete(profile, type, wonStatus)
            updates["e0a_NOG"] = totalPostCount
            updates["e0b_WG"] = wonGamesCount
            updates["e0c_WGP"] = wonGamesPercentage
            updates["e" + type + "a_NOG"] = stats[0]
            updates["e" + type + "b_WG"] = stats[1]
            updates["e" + type + "c_WGP"] = stats[2]
            transaction.update(postPath, updates)
            null
        }).addOnSuccessListener { aVoid: Void? ->
            firebaseFirestore!!.collection("posts").document(
                postId!!
            ).delete()
            Snackbar.make(view!!, "Deleted", Snackbar.LENGTH_SHORT).show()
            firebaseFirestore!!.collection("comments").whereEqualTo("commentOn", postId).get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.result == null || task.result!!
                            .isEmpty
                    ) return@addOnCompleteListener
                    for (snapshot: DocumentSnapshot in task.result!!.documents) snapshot.reference.delete()
                }
        }.addOnFailureListener { e: Exception? ->
            Snackbar.make(view!!, "Something went wrong", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun onPostWon(
        view: View?,
        postId: String?,
        userId: String?,
        type: Int
    ) {
        val postPath = firebaseFirestore!!.collection("profiles").document(
            userId!!
        )
        firebaseFirestore!!.runTransaction(Transaction.Function<Void?> { transaction: Transaction ->
            val snapshot = transaction[postPath]
            //Check if post exist first
            if (!snapshot.exists()) {
                Log.i(TAG, "apply: snapshot is empty")
                return@Function null
            }
            val profile = snapshot.toObject(ProfileMedium::class.java)
            val updates: MutableMap<String, Any> = HashMap()

            //Retrieve general stats
            val totalPostCount = profile!!.e0a_NOG
            val wonGamesCount = profile.e0b_WG + 1
            val wonGamesPercentage =
                if (totalPostCount > 0) wonGamesCount * 100 / totalPostCount else 0

            //retrieve stat for that game type
            val stats = getStatsForWonPost(profile, type)
            updates["e0b_WG"] = wonGamesCount
            updates["e0c_WGP"] = wonGamesPercentage
            updates["e" + type + "b_WG"] = stats[0]
            updates["e" + type + "c_WGP"] = stats[1]
            transaction.update(postPath, updates)
            null
        }).addOnSuccessListener { aVoid: Void? ->
            firebaseFirestore!!.collection("posts").document(
                postId!!
            ).update("status", 2)
            Snackbar.make(view!!, "updated", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception? ->
            Snackbar.make(view!!, "Something went wrong", Snackbar.LENGTH_SHORT).show()
        }
    }

}