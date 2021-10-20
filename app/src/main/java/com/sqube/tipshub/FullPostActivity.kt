package com.sqube.tipshub

import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import utils.Reusable.Companion.getPlaceholderImage
import utils.Reusable.Companion.signature
import utils.Reusable.Companion.applyLinkfy
import utils.Reusable.Companion.getTime
import utils.Calculations.onDeletePost
import utils.Calculations.onPostWon
import utils.Calculations.followMember
import utils.Calculations.unfollowMember
import adapters.CommentAdapter.setUserId
import utils.Calculations.onLike
import utils.Calculations.onDislike
import utils.Reusable.Companion.shareTips
import utils.Calculations.setCount
import utils.Calculations.sendPushNotification
import utils.Calculations.recommend
import adapters.CommentAdapter.repliedList
import adapters.CommentAdapter.resetRepliesList
import androidx.appcompat.app.AppCompatActivity
import android.text.TextWatcher
import androidx.recyclerview.widget.RecyclerView
import adapters.CommentAdapter
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import models.Post
import com.google.firebase.auth.FirebaseUser
import utils.Calculations
import com.google.firebase.storage.StorageReference
import android.os.Bundle
import com.sqube.tipshub.R
import android.preference.PreferenceManager
import com.google.firebase.storage.FirebaseStorage
import utils.FirebaseUtil
import utils.SpaceTokenizer
import android.view.View.OnLongClickListener
import services.GlideApp
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.tasks.OnCompleteListener
import android.view.LayoutInflater
import android.graphics.drawable.ColorDrawable
import com.sqube.tipshub.FlagActivity
import com.google.android.material.snackbar.Snackbar
import android.text.Html
import models.UserNetwork
import com.sqube.tipshub.RepostActivity
import com.sqube.tipshub.LoginActivity
import com.sqube.tipshub.MyProfileActivity
import com.sqube.tipshub.MemberProfileActivity
import com.sqube.tipshub.FullPostActivity
import com.google.android.gms.tasks.OnSuccessListener
import models.SnapId
import android.text.TextUtils
import kotlin.Throws
import com.google.android.gms.tasks.OnFailureListener
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import de.hdodenhof.circleimageview.CircleImageView
import models.Comment
import java.lang.Exception
import java.util.*

class FullPostActivity() : AppCompatActivity(), View.OnClickListener, TextWatcher {
    private var commentReference: CollectionReference? = null
    private var postReference: DocumentReference? = null
    private var lnrFullPost: LinearLayout? = null
    private var lnrChildPost: LinearLayout? = null
    private var mpost: TextView? = null
    private var mUsername: TextView? = null
    private var mTime: TextView? = null
    private var mLikes: TextView? = null
    private var mDislikes: TextView? = null
    private var mComment: TextView? = null
    private var mCode: TextView? = null
    private var mType: TextView? = null
    private var imgDp: CircleImageView? = null
    private var imgChildDp: CircleImageView? = null
    private var imgOverflow: ImageView? = null
    private var imgShare: ImageView? = null
    private var imgLike: ImageView? = null
    private var imgDislike: ImageView? = null
    private var imgStatus: ImageView? = null
    private var edtComment: MultiAutoCompleteTextView? = null
    private var fabPost: FloatingActionButton? = null
    private var prgPost: ProgressBar? = null
    private var commentsList: RecyclerView? = null
    private var commentAdapter: CommentAdapter? = null
    private var model: Post? = null
    private var intent: Intent? = null
    private val POST_ID = "postId"
    private var comment: String? = null
    private val TAG = "FullPostActivity"
    private var user: FirebaseUser? = null
    private var userId: String? = null
    private var username: String? = null
    private var postId: String? = null
    private var childLink: String? = null
    private var childDisplayed = false
    private var prefs: SharedPreferences? = null
    var calculations: Calculations? = null
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type =
        arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    private var storageReference: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_post)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setTitle("Post")
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        init()
        calculations = Calculations(applicationContext)
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        user = firebaseAuthentication!!.currentUser
        if (user == null) userId = GUEST else {
            userId = user!!.uid
            username = user!!.displayName
        }
        commentsList = findViewById(R.id.listComments)
        if (savedInstanceState != null) postId = savedInstanceState.getString(POST_ID) else postId =
            getIntent().getStringExtra(POST_ID)
        postReference = firebaseFirestore!!.collection("posts").document((postId)!!)
        val clubs = resources.getStringArray(R.array.club_arrays)
        val club_adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clubs)
        edtComment!!.setAdapter(club_adapter)
        edtComment!!.setTokenizer(SpaceTokenizer())
        edtComment!!.threshold = 4
        mpost!!.setOnLongClickListener({ view: View? ->
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Tipshub_post", model!!.getContent())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this@FullPostActivity, "Copied!", Toast.LENGTH_SHORT).show()
            false
        })
        val imgMyDp = findViewById<CircleImageView>(R.id.imgMyDp)
        if ((userId == GUEST)) imgMyDp.setImageResource(R.drawable.dummy) else GlideApp.with(
            applicationContext
        ).load(
            storageReference!!.child((userId)!!)
        )
            .placeholder(R.drawable.dummy)
            .error(getPlaceholderImage(userId!![0]))
            .signature(ObjectKey(userId + "_" + signature))
            .into(imgMyDp)
        listener()
        loadComment()
    }

    private fun init() {
        mpost = findViewById(R.id.txtPost)
        mUsername = findViewById(R.id.txtUsername)
        mUsername.setOnClickListener(this)
        mTime = findViewById(R.id.txtTime)
        mLikes = findViewById(R.id.txtLike)
        mDislikes = findViewById(R.id.txtDislike)
        mComment = findViewById(R.id.txtComment)
        mCode = findViewById(R.id.txtCode)
        mType = findViewById(R.id.txtPostType)
        fabPost = findViewById(R.id.fabPost)
        fabPost.setOnClickListener(this)
        edtComment = findViewById(R.id.edtComment)
        edtComment.addTextChangedListener(this)
        imgOverflow = findViewById(R.id.imgOverflow)
        imgOverflow.setOnClickListener(this)
        imgShare = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
        imgDp = findViewById(R.id.imgDp)
        imgDp.setOnClickListener(this)
        imgLike = findViewById(R.id.imgLike)
        imgLike.setOnClickListener(this)
        imgDislike = findViewById(R.id.imgDislike)
        imgDislike.setOnClickListener(this)
        imgStatus = findViewById(R.id.imgStatus)
        prgPost = findViewById(R.id.prgPost)
        prgPost.setVisibility(View.VISIBLE)
        lnrFullPost = findViewById(R.id.container_post)
        lnrFullPost.setVisibility(View.GONE)
        lnrChildPost = findViewById(R.id.container_child_post)
        lnrChildPost.setVisibility(View.GONE)
    }

    //listen for changes in likesCount, dislikesCount and update
    private fun listener() {
        postReference!!.addSnapshotListener(
            this@FullPostActivity,
            EventListener { documentSnapshot, e ->
                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    Toast.makeText(
                        this@FullPostActivity,
                        "Content doesn't exist",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    lnrFullPost!!.visibility = View.VISIBLE
                    prgPost!!.visibility = View.GONE
                    //retrieve post from database
                    model = documentSnapshot.toObject(Post::class.java)

                    //bind post contents to views
                    imgStatus!!.visibility = if (model!!.status == 1) View.GONE else View.VISIBLE
                    mUsername!!.text = model!!.username
                    mpost!!.text = model!!.content
                    applyLinkfy(applicationContext, model!!.content, (mpost)!!)
                    mTime!!.text = getTime(model!!.time)

                    //display booking code if available
                    if (model!!.bookingCode != null && !model!!.bookingCode.isEmpty()) {
                        mCode!!.text =
                            model!!.bookingCode + " @" + code.get((model!!.recommendedBookie - 1))
                        mCode!!.visibility = View.VISIBLE
                    } else mCode!!.visibility = View.GONE
                    if (model!!.type == 0) {
                        mType!!.visibility = View.GONE
                    } else {
                        mType!!.visibility = View.VISIBLE
                        mType!!.text = type.get(model!!.type - 1)
                    }

                    //display likes, dislikes, and comments
                    imgLike!!.setColorFilter(
                        if (model!!.likes.contains(userId)) resources.getColor(
                            R.color.likeGold
                        ) else resources.getColor(R.color.likeGrey)
                    )
                    imgDislike!!.setColorFilter(
                        if (model!!.dislikes.contains(userId)) resources.getColor(
                            R.color.likeGold
                        ) else resources.getColor(R.color.likeGrey)
                    )
                    mComment!!.text =
                        if (model!!.commentsCount == 0L) "" else model!!.commentsCount.toString()
                    mLikes!!.text =
                        if (model!!.likesCount == 0L) "" else model!!.likesCount.toString()
                    mDislikes!!.text =
                        if (model!!.dislikesCount == 0L) "" else model!!.dislikesCount.toString()
                    GlideApp.with(applicationContext).load(storageReference!!.child(model!!.userId))
                        .placeholder(R.drawable.dummy)
                        .error(getPlaceholderImage(model!!.userId[0]))
                        .signature(ObjectKey(model!!.userId + "_" + signature))
                        .into((imgDp)!!)
                    if (model!!.isHasChild) {
                        childLink = model!!.childLink
                        displayChildContent()
                    }
                }
            })
    }

    private fun displayChildContent() {
        if (childDisplayed) {
            return
        }
        //initialize child post views
        val childPost = findViewById<TextView>(R.id.txtChildPost)
        val childUsername = findViewById<TextView>(R.id.txtChildUsername)
        val childCode = findViewById<TextView>(R.id.txtChildCode)
        val childType = findViewById<TextView>(R.id.txtChildType)
        val imgChildStatus = findViewById<ImageView>(R.id.imgChildStatus)
        imgChildDp = findViewById(R.id.childDp)
        imgChildDp.setOnClickListener(this)
        childDisplayed = true
        firebaseFirestore!!.collection("posts").document((childLink)!!).get()
            .addOnCompleteListener({ task: Task<DocumentSnapshot> ->
                if (!task.getResult().exists()) {
                    childPost.setText("This content has been deleted")
                    imgChildDp.setVisibility(View.GONE)
                    childUsername.setVisibility(View.GONE)
                    childType.setVisibility(View.GONE)
                    imgChildStatus.setVisibility(View.GONE)
                    childCode.setVisibility(View.GONE)
                    lnrChildPost!!.setBackgroundResource(R.color.placeholder_bg)
                    lnrChildPost!!.setVisibility(View.VISIBLE) //display child layout if child post exists
                    return@addOnCompleteListener
                }
                val childModel: Post? =
                    task.getResult().toObject(Post::class.java) //retrieve child post

                //bind post to views
                imgChildStatus.setVisibility(if (childModel!!.getStatus() == 1) View.GONE else View.VISIBLE)
                if (childModel.getBookingCode() != null && !childModel.getBookingCode().isEmpty()) {
                    childCode.setText(childModel.getBookingCode() + " @" + code.get((childModel.getRecommendedBookie() - 1)))
                    childCode.setVisibility(View.VISIBLE)
                } else childCode.setVisibility(View.GONE)
                if (childModel.getType() == 0) {
                    childType.setVisibility(View.GONE)
                } else {
                    childType.setVisibility(View.VISIBLE)
                    childType.setText(type.get(childModel.getType() - 1))
                }
                childUsername.setText(childModel.getUsername())
                childPost.setText(childModel.getContent())
                applyLinkfy(getApplicationContext(), childModel.getContent(), childPost)
                GlideApp.with(getApplicationContext())
                    .load(storageReference!!.child(childModel.getUserId()))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(childModel.getUserId().get(0)))
                    .signature(ObjectKey(childModel.getUserId() + "_" + signature))
                    .into(imgChildDp)
                lnrChildPost!!.setVisibility(View.VISIBLE) //display child layout if child post exists
                childPost.setOnClickListener(this@FullPostActivity)
                lnrChildPost!!.setOnClickListener(this@FullPostActivity)
            })
    }

    //Displays overflow containing options like follow, subscribe, disagree, etc.
    private fun displayOverflow() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View
        if ((model!!.userId == userId)) dialogView =
            inflater.inflate(R.layout.dialog_mine, null) else dialogView =
            inflater.inflate(R.layout.dialog_member, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val btnSubmit: Button
        val btnDelete: Button
        val btnRepost: Button
        val btnFollow: Button
        btnSubmit = dialog.findViewById(R.id.btnSubmit)
        btnDelete = dialog.findViewById(R.id.btnDelete)
        btnRepost = dialog.findViewById(R.id.btnRepost)
        btnFollow = dialog.findViewById(R.id.btnFollow)
        val timeDifference = Date().time - model!!.time
        if ((model!!.userId == userId) && (model!!.type > 0) && (timeDifference > 9000000)) btnDelete.isEnabled =
            false
        if ((model!!.userId == userId) && model!!.type == 0) btnSubmit.visibility =
            View.GONE else if ((model!!.userId == userId) && timeDifference > 144000000) btnSubmit.visibility =
            View.GONE else {
            if ((model!!.userId == userId) && (model!!.status == 2) && (timeDifference <= 9000000)) btnSubmit.text =
                "CANCEL WON"
            if ((model!!.userId == userId) && (model!!.status == 2) && (timeDifference > 9000000)) btnSubmit.visibility =
                View.GONE
        }
        btnDelete.setOnClickListener({ v: View? ->
            if ((btnDelete.getText().toString().toLowerCase() == "flag")) {
                intent = Intent(this@FullPostActivity, FlagActivity::class.java)
                intent!!.putExtra("postId", postId)
                intent!!.putExtra("reportedUsername", model!!.getUsername())
                intent!!.putExtra("reportedUserId", model!!.getUserId())
                startActivity(intent)
                dialog.cancel()
            } else {
                Log.i(TAG, "onClick: " + model!!.getType())
                if (model!!.getType() > 0) calculations!!.onDeletePost(
                    imgOverflow,
                    postId,
                    userId,
                    model!!.getStatus() == 2,
                    model!!.getType()
                ) else {
                    firebaseFirestore!!.collection("posts").document((postId)!!).delete()
                    Snackbar.make((imgOverflow)!!, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
                dialog.cancel()
            }
        })
        btnSubmit.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (model!!.type > 0) popUp()
                dialog.cancel()
            }

            private fun popUp() {
                val message =
                    ("<p><span style=\"color: #F80051; font-size: 16px;\"><strong>Your tips have delivered?</strong></span></p>\n" +
                            "<p>By clicking 'YES', you confirm that your prediction has delivered.</p>\n" +
                            "<p>Your account may be suspended or terminated if that's not true.</p>")
                val builder = androidx.appcompat.app.AlertDialog.Builder(
                    applicationContext,
                    R.style.CustomMaterialAlertDialog
                )
                builder.setMessage(Html.fromHtml(message))
                    .setPositiveButton(
                        "Yes",
                        { dialogInterface: DialogInterface?, i: Int ->
                            calculations!!.onPostWon(
                                imgOverflow,
                                postId,
                                userId,
                                model!!.getType()
                            )
                        })
                    .setNegativeButton("Cancel", { dialogInterface: DialogInterface?, i: Int -> })
                    .show()
            }
        })
        val btnFollowText =
            if (UserNetwork.following.contains(model!!.userId)) "UNFOLLOW" else "FOLLOW"
        btnFollow.text = btnFollowText
        btnRepost.setOnClickListener({ v: View? ->
            if ((userId == GUEST)) {
                loginPrompt()
                return@setOnClickListener
            }
            intent = Intent(this@FullPostActivity, RepostActivity::class.java)
            intent!!.putExtra("postId", postId)
            intent!!.putExtra("model", model)
            startActivity(intent)
            dialog.cancel()
        })
        btnFollow.setOnClickListener({ v: View? ->
            if ((userId == GUEST)) {
                loginPrompt()
                return@setOnClickListener
            }
            if ((btnFollow.getText() == "FOLLOW")) {
                calculations!!.followMember(imgOverflow, (userId)!!, model!!.getUserId(), false)
            } else {
                calculations!!.unfollowMember(imgOverflow, userId, model!!.getUserId(), false)
            }
            dialog.cancel()
        })
    }

    private fun loginPrompt() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(
            this@FullPostActivity,
            R.style.CustomMaterialAlertDialog
        )
        builder.setMessage("You have to login first")
            .setNegativeButton("Cancel", { dialogInterface: DialogInterface?, i: Int -> })
            .setPositiveButton("Login", { dialogInterface: DialogInterface?, i: Int ->
                startActivity(Intent(this@FullPostActivity, LoginActivity::class.java))
                finish()
            })
            .show()
    }

    private fun loadComment() {
        //loads comment into commentList
        commentReference = firebaseFirestore!!.collection("comments")
        val query = commentReference!!.whereEqualTo("commentOn", postId)
            .orderBy("time", Query.Direction.DESCENDING)
        commentAdapter = CommentAdapter(
            (postId)!!,
            query,
            userId,
            this@FullPostActivity,
            applicationContext,
            false
        )
        commentsList!!.adapter = commentAdapter
        if (commentAdapter != null) {
            commentAdapter!!.startListening()
        }
    }

    public override fun onResume() {
        super.onResume()
        user = firebaseAuthentication!!.currentUser
        if (user == null) userId = GUEST else {
            userId = user!!.uid
            username = user!!.displayName
        }
        if (commentAdapter != null) commentAdapter!!.setUserId(userId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        val substring: String
        when (v.id) {
            R.id.txtUsername, R.id.imgDp -> if ((model!!.userId == userId)) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                intent = Intent(this, MemberProfileActivity::class.java)
                intent!!.putExtra("userId", model!!.userId)
                startActivity(intent)
            }
            R.id.childDp -> if ((model!!.childUserId == userId)) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                intent = Intent(this, MemberProfileActivity::class.java)
                intent!!.putExtra("userId", model!!.childUserId)
                startActivity(intent)
            }
            R.id.imgOverflow -> displayOverflow()
            R.id.txtChildPost, R.id.container_child_post -> {
                intent = Intent(applicationContext, FullPostActivity::class.java)
                intent!!.putExtra("postId", childLink)
                startActivity(intent)
            }
            R.id.imgLike -> {
                if ((userId == GUEST)) {
                    loginPrompt()
                    return
                }
                onLike()
                substring = model!!.content.substring(0, Math.min(model!!.content.length, 90))
                calculations!!.onLike(postId, (userId)!!, model!!.userId, substring)
            }
            R.id.imgDislike -> {
                if ((userId == GUEST)) {
                    loginPrompt()
                    return
                }
                onDislike()
                substring = model!!.content.substring(0, Math.min(model!!.content.length, 90))
                calculations!!.onDislike(postId, (userId)!!, model!!.userId, substring)
            }
            R.id.imgShare -> shareTips(this@FullPostActivity, model!!.username, model!!.content)
            R.id.fabPost -> {
                if ((userId == GUEST)) {
                    loginPrompt()
                    return
                }
                increaseCommentCount()
            }
        }
    }

    private fun sendNotification(content: String?) {
        val substring = content!!.substring(0, Math.min(content.length, 90))
        calculations!!.setCount(model!!.commentsCount)
        calculations!!.sendPushNotification(
            true,
            userId,
            model!!.userId,
            postId,
            "commented on",
            "post",
            substring
        )
    }

    private fun onLike() {
        if (model!!.dislikes.contains(userId)) {
            imgLike!!.setColorFilter(resources.getColor(R.color.likeGold))
            imgDislike!!.setColorFilter(resources.getColor(R.color.likeGrey))
            mLikes!!.text = (model!!.likesCount + 1).toString()
            mDislikes!!.text =
                if (model!!.dislikesCount - 1 > 0) (model!!.dislikesCount - 1).toString() else ""
        } else {
            if (model!!.likes.contains(userId)) {
                imgLike!!.setColorFilter(resources.getColor(R.color.likeGrey))
                mLikes!!.text =
                    if (model!!.likesCount - 1 > 0) (model!!.likesCount - 1).toString() else ""
            } else {
                imgLike!!.setColorFilter(resources.getColor(R.color.likeGold))
                mLikes!!.text = (model!!.likesCount + 1).toString()
            }
        }
    }

    private fun onDislike() {
        if (model!!.likes.contains(userId)) {
            imgLike!!.setColorFilter(resources.getColor(R.color.likeGrey))
            imgDislike!!.setColorFilter(resources.getColor(R.color.likeGold))
            mLikes!!.text =
                if (model!!.likesCount - 1 > 0) (model!!.likesCount - 1).toString() else ""
            mDislikes!!.text = (model!!.dislikesCount + 1).toString()
        } else {
            if (model!!.dislikes.contains(userId)) {
                imgDislike!!.setColorFilter(resources.getColor(R.color.likeGrey))
                mDislikes!!.text =
                    if (model!!.dislikesCount - 1 > 0) (model!!.dislikesCount - 1).toString() else ""
            } else {
                imgDislike!!.setColorFilter(resources.getColor(R.color.likeGold))
                mDislikes!!.text = (model!!.dislikesCount + 1).toString()
            }
        }
    }

    private fun postComment() {
        val isVerified = prefs!!.getBoolean("isVerified", false)
        commentReference!!.add(Comment(username, userId, comment, postId, false, isVerified))
            .addOnSuccessListener(object : OnSuccessListener<DocumentReference?> {
                override fun onSuccess(documentReference: DocumentReference?) {
                    val content = comment
                    comment = ""
                    if (userId != model!!.userId) {
                        calculations!!.recommend(userId, model!!.userId)
                        sendNotification(content)
                        notifyMentionedUsers(content)
                    }
                }
            })
    }

    //send notification to mentioned users
    private fun notifyMentionedUsers(comment: String?) {
        val repliedUsers = commentAdapter!!.repliedList
        if (!repliedUsers.isEmpty()) {
            val set: Set<SnapId> = HashSet(repliedUsers)
            repliedUsers.clear()
            repliedUsers.addAll(set)
            val substring = comment!!.substring(0, Math.min(comment.length, 90))
            for (user: SnapId in repliedUsers) {
                if (comment.contains("@" + user.username) && model!!.userId != user.id) calculations!!.sendPushNotification(
                    true, userId, user.id, postId,
                    "mentioned you", "comment", substring
                )
            }
            edtComment!!.setText("")
            commentAdapter!!.resetRepliesList()
        }
    }

    fun increaseCommentCount() {
        comment = edtComment!!.text.toString()
        if (TextUtils.isEmpty(comment)) {
            edtComment!!.error = "Type your comment"
            return
        } else {
            fabPost!!.isEnabled = false
            edtComment!!.isEnabled = false
        }
        firebaseFirestore!!.runTransaction(object : Transaction.Function<Void?> {
            @Throws(FirebaseFirestoreException::class)
            override fun apply(transaction: Transaction): Void? {
                Log.i(TAG, "apply: likes entered")
                val snapshot = transaction[(postReference)!!]

                //check if post still exists
                if (!snapshot.exists()) {
                    Toast.makeText(
                        this@FullPostActivity,
                        "Seems the post has been deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    fabPost!!.isEnabled = true
                    return null
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                var commentsCount = (snapshot.getLong("commentsCount"))!!
                val upd: MutableMap<String, Any> = HashMap()
                commentsCount += 1
                upd["commentsCount"] = commentsCount
                transaction.update((postReference)!!, upd)
                return null
            }
        })
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    Log.d(TAG, "Transaction success!")
                    postComment()
                    fabPost!!.isEnabled = true
                    edtComment!!.isEnabled = true
                    edtComment!!.setText("")
                    Snackbar.make((edtComment)!!, "Comment added", Snackbar.LENGTH_SHORT).show()
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    fabPost!!.isEnabled = true
                    Toast.makeText(this@FullPostActivity, "Connection failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString().trim { it <= ' ' }.length > 1) {
            comment = edtComment!!.text.toString()
        }
    }

    override fun afterTextChanged(s: Editable) {}
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(POST_ID, postId)
        super.onSaveInstanceState(outState)
    }
}