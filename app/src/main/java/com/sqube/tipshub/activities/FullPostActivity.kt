package com.sqube.tipshub.activities

import com.sqube.tipshub.utils.FirebaseUtil.firebaseAuthentication
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.Reusable.Companion.signature
import com.sqube.tipshub.utils.Reusable.Companion.applyLinkfy
import com.sqube.tipshub.utils.Reusable.Companion.getTime
import com.sqube.tipshub.utils.Reusable.Companion.shareTips
import androidx.appcompat.app.AppCompatActivity
import android.text.TextWatcher
import com.sqube.tipshub.adapters.CommentAdapter
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import com.sqube.tipshub.models.Post
import com.google.firebase.auth.FirebaseUser
import com.sqube.tipshub.utils.Calculations
import com.google.firebase.storage.StorageReference
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.sqube.tipshub.utils.SpaceTokenizer
import com.bumptech.glide.signature.ObjectKey
import android.graphics.drawable.ColorDrawable
import com.google.android.material.snackbar.Snackbar
import android.text.Html
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.models.SnapId
import android.text.TextUtils
import kotlin.Throws
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityFullPostBinding
import de.hdodenhof.circleimageview.CircleImageView
import com.sqube.tipshub.models.Comment
import com.sqube.tipshub.services.GlideApp
import com.sqube.tipshub.utils.GUEST
import java.util.*

class FullPostActivity() : AppCompatActivity(), View.OnClickListener, TextWatcher {
    private lateinit var binding: ActivityFullPostBinding
    private var commentReference: CollectionReference? = null
    private var postReference: DocumentReference? = null
    private var commentAdapter: CommentAdapter? = null

    private var model: Post? = null
    private lateinit var funIntent: Intent
    private val POST_ID = "postId"
    private var comment: String? = null
    private val TAG = "FullPostActivity"
    private var user: FirebaseUser? = null
    private lateinit var userId: String
    private var username: String? = null
    private var postId: String? = null
    private var childLink: String? = null
    private var childDisplayed = false
    private lateinit var prefs: SharedPreferences
    var calculations: Calculations? = null
    private val code = arrayOf("1xBet", "Bet9ja", "Nairabet", "SportyBet", "BlackBet", "Bet365")
    private val type =
        arrayOf("3-5 odds", "6-10 odds", "11-50 odds", "50+ odds", "Draws", "Banker tip")
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setTitle("Post")
        prefs = getSharedPreferences(applicationContext.packageName + "_preferences", Context.MODE_PRIVATE)
        init()
        calculations = Calculations(applicationContext)
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        user = firebaseAuthentication!!.currentUser
        if (user == null)
            userId = GUEST
        else {
            userId = user!!.uid
            username = user!!.displayName
        }
        if (savedInstanceState != null) postId = savedInstanceState.getString(POST_ID)
        else postId =
            intent.getStringExtra(POST_ID)
        postReference = firebaseFirestore!!.collection("posts").document(postId!!)
        val clubs = resources.getStringArray(R.array.club_arrays)
        val club_adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clubs)
        binding.edtComment.setAdapter(club_adapter)
        binding.edtComment.setTokenizer(SpaceTokenizer())
        binding.edtComment.threshold = 4
        binding.txtPost.setOnLongClickListener { view: View? ->
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Tipshub_post", model!!.getContent())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this@FullPostActivity, "Copied!", Toast.LENGTH_SHORT).show()
            false
        }
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
        binding.txtUsername.setOnClickListener(this)
        binding.fabPost.setOnClickListener(this)
        binding.edtComment.addTextChangedListener(this)
        binding.imgOverflow.setOnClickListener(this)
        binding.imgShare.setOnClickListener(this)
        binding.imgDp.setOnClickListener(this)
        binding.imgLike.setOnClickListener(this)
        binding.imgDislike.setOnClickListener(this)
        binding.prgPost.visibility = View.VISIBLE
        binding.containerPost.visibility = View.GONE
        binding.containerChildPost.visibility = View.GONE
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
                    binding.containerPost.visibility = View.VISIBLE
                    binding.prgPost.visibility = View.GONE
                    //retrieve post from database
                    model = documentSnapshot.toObject(Post::class.java)

                    //bind post contents to views
                    binding.imgStatus.visibility = if (model!!.status == 1) View.GONE else View.VISIBLE
                    binding.txtUsername.text = model!!.username
                    binding.txtPost.text = model!!.content
                    applyLinkfy(applicationContext, model!!.content, binding.txtPost)
                    binding.txtTime.text = getTime(model!!.time)

                    //display booking code if available
                    if (model!!.bookingCode != null && !model!!.bookingCode.isEmpty()) {
                        binding.txtCode.text =
                            model!!.bookingCode + " @" + code.get((model!!.recommendedBookie - 1))
                        binding.txtCode.visibility = View.VISIBLE
                    } else binding.txtCode.visibility = View.GONE
                    if (model!!.type == 0) {
                        binding.txtPostType.visibility = View.GONE
                    } else {
                        binding.txtPostType.visibility = View.VISIBLE
                        binding.txtPostType.text = type.get(model!!.type - 1)
                    }

                    //display likes, dislikes, and comments
                    binding.imgLike.setColorFilter(
                        if (model!!.likes.contains(userId)) resources.getColor(
                            R.color.likeGold
                        ) else resources.getColor(R.color.likeGrey)
                    )
                    binding.imgDislike.setColorFilter(
                        if (model!!.dislikes.contains(userId)) resources.getColor(
                            R.color.likeGold
                        ) else resources.getColor(R.color.likeGrey)
                    )
                    binding.txtComment.text =
                        if (model!!.commentsCount == 0L) "" else model!!.commentsCount.toString()
                    binding.txtLike.text =
                        if (model!!.likesCount == 0L) "" else model!!.likesCount.toString()
                    binding.txtDislike.text =
                        if (model!!.dislikesCount == 0L) "" else model!!.dislikesCount.toString()
                    GlideApp.with(applicationContext).load(storageReference!!.child(model!!.userId))
                        .placeholder(R.drawable.dummy)
                        .error(getPlaceholderImage(model!!.userId[0]))
                        .signature(ObjectKey(model!!.userId + "_" + signature))
                        .into(binding.imgDp)
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
        binding.childDp.setOnClickListener(this)
        childDisplayed = true
        firebaseFirestore!!.collection("posts").document((childLink)!!).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (!task.getResult().exists()) {
                    childPost.text = "This content has been deleted"
                    binding.childDp.visibility = View.GONE
                    childUsername.visibility = View.GONE
                    childType.visibility = View.GONE
                    imgChildStatus.visibility = View.GONE
                    childCode.visibility = View.GONE
                    binding.containerChildPost.setBackgroundResource(R.color.placeholder_bg)
                    binding.containerChildPost.visibility = View.VISIBLE //display child layout if child post exists
                    return@addOnCompleteListener
                }
                val childModel: Post? =
                    task.getResult().toObject(Post::class.java) //retrieve child post

                //bind post to views
                imgChildStatus.visibility = if (childModel!!.getStatus() == 1) View.GONE else View.VISIBLE
                if (childModel.bookingCode != null && !childModel.getBookingCode().isEmpty()) {
                    childCode.text = childModel.getBookingCode() + " @" + code.get((childModel.getRecommendedBookie() - 1))
                    childCode.visibility = View.VISIBLE
                } else childCode.visibility = View.GONE
                if (childModel.type == 0) {
                    childType.visibility = View.GONE
                } else {
                    childType.visibility = View.VISIBLE
                    childType.text = type.get(childModel.getType() - 1)
                }
                childUsername.text = childModel.getUsername()
                childPost.text = childModel.getContent()
                applyLinkfy(applicationContext, childModel.content, childPost)
                GlideApp.with(applicationContext)
                    .load(storageReference!!.child(childModel.userId))
                    .placeholder(R.drawable.dummy)
                    .error(getPlaceholderImage(childModel.userId[0]))
                    .signature(ObjectKey(childModel.userId + "_" + signature))
                    .into(binding.childDp)
                binding.containerChildPost.setVisibility(View.VISIBLE) //display child layout if child post exists
                childPost.setOnClickListener(this@FullPostActivity)
                binding.containerChildPost.setOnClickListener(this@FullPostActivity)
            }
    }

    //Displays overflow containing options like follow, subscribe, disagree, etc.
    private fun displayOverflow() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View
        if ((model!!.userId == userId)) dialogView =
            inflater.inflate(R.layout.dialog_mine, null)
        else dialogView =
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
        btnDelete.setOnClickListener { v: View? ->
            if ((btnDelete.text.toString().toLowerCase() == "flag")) {
                funIntent = Intent(this@FullPostActivity, FlagActivity::class.java)
                funIntent!!.putExtra("postId", postId)
                funIntent!!.putExtra("reportedUsername", model!!.getUsername())
                funIntent!!.putExtra("reportedUserId", model!!.getUserId())
                startActivity(funIntent)
                dialog.cancel()
            } else {
                Log.i(TAG, "onClick: " + model!!.getType())
                if (model!!.getType() > 0) calculations!!.onDeletePost(
                    binding.imgOverflow,
                    postId,
                    userId,
                    model!!.getStatus() == 2,
                    model!!.getType()
                ) else {
                    firebaseFirestore!!.collection("posts").document((postId)!!).delete()
                    Snackbar.make(binding.imgOverflow, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
                dialog.cancel()
            }
        }
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
                        "Yes"
                    ) { dialogInterface: DialogInterface?, i: Int ->
                        calculations!!.onPostWon(
                            binding.imgOverflow,
                            postId,
                            userId,
                            model!!.getType()
                        )
                    }
                    .setNegativeButton("Cancel", { dialogInterface: DialogInterface?, i: Int -> })
                    .show()
            }
        })
        val btnFollowText =
            if (UserNetwork.following.contains(model!!.userId)) "UNFOLLOW" else "FOLLOW"
        btnFollow.text = btnFollowText
        btnRepost.setOnClickListener { v: View? ->
            if ((userId == GUEST)) {
                loginPrompt()
                return@setOnClickListener
            }
            funIntent = Intent(this@FullPostActivity, RepostActivity::class.java)
            funIntent.putExtra("postId", postId)
            funIntent.putExtra("model", model)
            startActivity(funIntent)
            dialog.cancel()
        }
        btnFollow.setOnClickListener { v: View? ->
            if ((userId == GUEST)) {
                loginPrompt()
                return@setOnClickListener
            }
            if ((btnFollow.getText() == "FOLLOW")) {
                calculations!!.followMember(binding.imgOverflow, (userId)!!, model!!.getUserId())
            } else {
                calculations!!.unfollowMember(binding.imgOverflow, userId, model!!.getUserId())
            }
            dialog.cancel()
        }
    }

    private fun loginPrompt() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(
            this@FullPostActivity,
            R.style.CustomMaterialAlertDialog
        )
        builder.setMessage("You have to login first")
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
            .setPositiveButton("Login") { dialogInterface: DialogInterface?, i: Int ->
                startActivity(Intent(this@FullPostActivity, LoginActivity::class.java))
                finish()
            }
            .show()
    }

    private fun loadComment() {
        //loads comment into commentList
        commentReference = firebaseFirestore!!.collection("comments")
        val query = commentReference!!.whereEqualTo("commentOn", postId)
            .orderBy("time", Query.Direction.DESCENDING)
        commentAdapter = CommentAdapter(postId!!, query, userId, this@FullPostActivity, applicationContext)
        binding.listComments.adapter = commentAdapter
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
                funIntent = Intent(this, MemberProfileActivity::class.java)
                funIntent!!.putExtra("userId", model!!.userId)
                startActivity(funIntent)
            }
            R.id.childDp -> if ((model!!.childUserId == userId)) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                funIntent = Intent(this, MemberProfileActivity::class.java)
                funIntent.putExtra("userId", model!!.childUserId)
                startActivity(funIntent)
            }
            R.id.imgOverflow -> displayOverflow()
            R.id.txtChildPost, R.id.container_child_post -> {
                funIntent = Intent(applicationContext, FullPostActivity::class.java)
                funIntent.putExtra("postId", childLink)
                startActivity(funIntent)
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
            binding.imgLike.setColorFilter(resources.getColor(R.color.likeGold))
            binding.imgDislike.setColorFilter(resources.getColor(R.color.likeGrey))
            binding.txtLike.text = (model!!.likesCount + 1).toString()
            binding.txtDislike.text =
                if (model!!.dislikesCount - 1 > 0) (model!!.dislikesCount - 1).toString() else ""
        } else {
            if (model!!.likes.contains(userId)) {
                binding.imgLike.setColorFilter(resources.getColor(R.color.likeGrey))
                binding.txtLike.text =
                    if (model!!.likesCount - 1 > 0) (model!!.likesCount - 1).toString() else ""
            } else {
                binding.imgLike.setColorFilter(resources.getColor(R.color.likeGold))
                binding.txtLike.text = (model!!.likesCount + 1).toString()
            }
        }
    }

    private fun onDislike() {
        if (model!!.likes.contains(userId)) {
            binding.imgLike.setColorFilter(resources.getColor(R.color.likeGrey))
            binding.imgDislike.setColorFilter(resources.getColor(R.color.likeGold))
            binding.txtLike.text =
                if (model!!.likesCount - 1 > 0) (model!!.likesCount - 1).toString() else ""
            binding.txtDislike.text = (model!!.dislikesCount + 1).toString()
        } else {
            if (model!!.dislikes.contains(userId)) {
                binding.imgDislike.setColorFilter(resources.getColor(R.color.likeGrey))
                binding.txtDislike.text =
                    if (model!!.dislikesCount - 1 > 0) (model!!.dislikesCount - 1).toString() else ""
            } else {
                binding.imgDislike.setColorFilter(resources.getColor(R.color.likeGold))
                binding.txtDislike.text = (model!!.dislikesCount + 1).toString()
            }
        }
    }

    private fun postComment() {
        val isVerified = prefs.getBoolean("isVerified", false)
        commentReference!!.add(Comment(username, userId, comment, postId, false, isVerified))
            .addOnSuccessListener {
                val content = comment
                comment = ""
                if (userId != model!!.userId) {
                    calculations!!.recommend(userId, model!!.userId)
                    sendNotification(content)
                    notifyMentionedUsers(content)
                }
            }
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
            binding.edtComment.setText("")
            commentAdapter!!.resetRepliesList()
        }
    }

    fun increaseCommentCount() {
        comment = binding.edtComment.text.toString()
        if (TextUtils.isEmpty(comment)) {
            binding.edtComment.error = "Type your comment"
            return
        } else {
            binding.fabPost.isEnabled = false
            binding.edtComment.isEnabled = false
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
                    binding.fabPost.isEnabled = true
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
            .addOnSuccessListener {
                Log.d(TAG, "Transaction success!")
                postComment()
                binding.fabPost.isEnabled = true
                binding.edtComment.isEnabled = true
                binding.edtComment.setText("")
                Snackbar.make(binding.edtComment, "Comment added", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.fabPost.isEnabled = true
                Toast.makeText(this@FullPostActivity, "Connection failed", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString().trim { it <= ' ' }.length > 1) {
            comment = binding.edtComment.text.toString()
        }
    }

    override fun afterTextChanged(s: Editable) {}

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(POST_ID, postId)
        super.onSaveInstanceState(outState)
    }
}