package adapters

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.sqube.tipshub.databinding.ItemUserLandBinding
import models.ProfileShort
import models.UserNetwork
import utils.Calculations
import utils.FirebaseUtil.firebaseFirestore
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.getPlaceholderImage
import java.util.*

class PeopleAdapter(userId: String?, list: ArrayList<String>) : RecyclerView.Adapter<PeopleAdapter.PeopleHolder>() {
    private var userId: String? = null
    private val list: ArrayList<String>
    init {
        setUserId(userId)
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleHolder {
        val binding = ItemUserLandBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeopleHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: PeopleHolder, i: Int) {
        val binding = holder.binding
        val ref = list[i]
        firebaseFirestore!!.collection("profiles").document(ref).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (!task.isSuccessful || task.isCanceled || !task.result.exists()) {
                        list.removeAt(i)
                        notifyDataSetChanged()
                        return@addOnCompleteListener
                    }
                    val model = task.result.toObject(ProfileShort::class.java)!!
                    with(model){
                        binding.txtUsername.text = a2_username
                        val tips = if (e0a_NOG > 1) "tips" else "tip"
                        binding.txtPost.text = String.format(Locale.getDefault(), "%d  %s  • ", e0a_NOG, tips)
                        binding.txtAccuracy.text = String.format(Locale.getDefault(), "%.1f%%", e0c_WGP.toDouble())
                        binding.btnFollow.text = if (UserNetwork.getFollowing() == null || !UserNetwork.getFollowing().contains(ref)) "FOLLOW" else "FOLLOWING"
                    }
                    if (ref == userId) binding.btnFollow.visibility = View.GONE
                    try {
                        Glide.with(binding.root.context).load(model.b2_dpUrl)
                                .placeholder(R.drawable.dummy)
                                .error(getPlaceholderImage(ref[0]))
                                .into(binding.imgDp)
                    } catch (e: Exception) {
                        Log.w("{PeopleAdapter", "onBindViewHolder: " + e.message)
                    }
                    binding.lnrContainer.setOnClickListener { v: View? ->
                        if (ref == userId) {
                            binding.lnrContainer.context.startActivity(Intent(binding.root.context, MyProfileActivity::class.java))
                        } else {
                            val intent = Intent(binding.root.context, MemberProfileActivity::class.java)
                            intent.putExtra("userId", ref)
                            binding.lnrContainer.context.startActivity(intent)
                        }
                    }
                    binding.btnFollow.setOnClickListener { v: View? ->
                        if (!getNetworkAvailability(binding.root.context)) {
                            Snackbar.make(binding.btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (userId == Calculations.GUEST) {
                            loginPrompt(binding.btnFollow)
                            return@setOnClickListener
                        }
                        if (binding.btnFollow.text.toString().toUpperCase() == "FOLLOW") {
                            val calculations = Calculations(binding.root.context)
                            calculations.followMember(binding.imgDp, userId, ref, false)
                            binding.btnFollow.text = "FOLLOWING"
                        } else unfollowPrompt(binding.btnFollow, ref, model.a2_username)
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
        val builder = AlertDialog.Builder(btnFollow.rootView.context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int ->
                    val calculations = Calculations(btnFollow.context)
                    calculations.unfollowMember(btnFollow, userId, userID, false)
                    btnFollow.text = "FOLLOW"
                }
                .show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    inner class PeopleHolder(val binding: ItemUserLandBinding) : RecyclerView.ViewHolder(binding.root)

}