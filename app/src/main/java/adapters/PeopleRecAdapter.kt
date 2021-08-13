package adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.sqube.tipshub.LoginActivity
import com.sqube.tipshub.MemberProfileActivity
import com.sqube.tipshub.MyProfileActivity
import com.sqube.tipshub.R
import de.hdodenhof.circleimageview.CircleImageView
import models.ProfileShort
import models.UserNetwork
import utils.Calculations
import utils.FirebaseUtil.firebaseFirestore
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.getPlaceholderImage
import java.util.*

class PeopleRecAdapter(context: Context, userId: String, list: ArrayList<String>) : RecyclerView.Adapter<PeopleRecAdapter.PeopleHolder>() {
    private val TAG = "PplAdapter"
    private val context: Context
    private val userId: String
    private val list: ArrayList<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return PeopleHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: PeopleHolder, i: Int) {
        val ref = list[i]
        firebaseFirestore!!.collection("profiles").document(ref).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (!task.isSuccessful || task.isCanceled || !task.result.exists()) {
                        list.removeAt(i)
                        notifyDataSetChanged()
                        return@addOnCompleteListener
                    }
                    Log.i(TAG, "onComplete: ")
                    val model = task.result.toObject(ProfileShort::class.java)
                    holder.mUsername.text = model!!.a2_username
                    val tips = if (model.e0a_NOG > 1) "tips" else "tip"
                    holder.mPost.text = String.format(Locale.getDefault(), "%d  %s  • ", model.e0a_NOG, tips)
                    holder.mAccuracy.text = String.format(Locale.getDefault(), "%.1f%%", model.e0c_WGP.toDouble())
                    holder.btnFollow.text = if (UserNetwork.getFollowing() == null || !UserNetwork.getFollowing().contains(ref)) "FOLLOW" else "FOLLOWING"
                    Glide.with(holder.imgDp.context).load(model.b2_dpUrl)
                            .placeholder(R.drawable.dummy)
                            .error(getPlaceholderImage(ref[0]))
                            .into(holder.imgDp)
                    holder.lnrContainer.setOnClickListener { v: View? ->
                        if (ref == userId) {
                            holder.lnrContainer.context.startActivity(Intent(context, MyProfileActivity::class.java))
                        } else {
                            val intent = Intent(context, MemberProfileActivity::class.java)
                            intent.putExtra("userId", ref)
                            holder.lnrContainer.context.startActivity(intent)
                        }
                    }
                    holder.btnFollow.setOnClickListener { v: View? ->
                        if (!getNetworkAvailability(context)) {
                            Snackbar.make(holder.btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(R.id.bottom_navigation).show()
                            return@setOnClickListener
                        }
                        if (userId == Calculations.GUEST) {
                            loginPrompt(holder.btnFollow)
                            return@setOnClickListener
                        }
                        if (holder.btnFollow.text.toString().toUpperCase() == "FOLLOW") {
                            val calculations = Calculations(context)
                            calculations.followMember(holder.imgDp, userId, ref, true)
                            holder.btnFollow.text = "FOLLOWING"
                        } else unfollowPrompt(holder.btnFollow, ref, model.a2_username)
                    }
                }
    }

    private fun loginPrompt(view: View) {
        val builder = AlertDialog.Builder(view.rootView.context, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { dialogInterface: DialogInterface?, i: Int -> view.context.startActivity(Intent(view.context, LoginActivity::class.java)) }
                .show()
    }

    private fun unfollowPrompt(btnFollow: TextView, userID: String, username: String) {
        val builder = AlertDialog.Builder(context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int ->
                    val calculations = Calculations(context)
                    calculations.unfollowMember(btnFollow, userId, userID, true)
                    btnFollow.text = "FOLLOW"
                }
                .show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PeopleHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgDp: CircleImageView
        var lnrContainer: LinearLayout
        var mUsername: TextView
        var mPost: TextView
        var mAccuracy: TextView
        var btnFollow: TextView

        init {
            imgDp = itemView.findViewById(R.id.imgDp)
            lnrContainer = itemView.findViewById(R.id.lnrContainer)
            mUsername = itemView.findViewById(R.id.txtUsername)
            mPost = itemView.findViewById(R.id.txtPost)
            mAccuracy = itemView.findViewById(R.id.txtAccuracy)
            btnFollow = itemView.findViewById(R.id.btnFollow)
        }
    }

    init {
        Log.i(TAG, "PeopleRecAdapter: called")
        this.context = context
        this.userId = userId
        this.list = list
    }
}