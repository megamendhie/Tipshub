package com.sqube.tipshub.adapters

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
import com.sqube.tipshub.activities.LoginActivity
import com.sqube.tipshub.activities.MemberProfileActivity
import com.sqube.tipshub.activities.MyProfileActivity
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemUserBinding
import com.sqube.tipshub.models.ProfileShort
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.utils.Calculations
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.GUEST
import com.sqube.tipshub.utils.Reusable.Companion.getNetworkAvailability
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import java.lang.IndexOutOfBoundsException
import java.util.*

class PeopleRecAdapter(val userId: String, val list: ArrayList<String>) : RecyclerView.Adapter<PeopleRecAdapter.PeopleHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeopleHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: PeopleHolder, i: Int) {
        val binding = holder.binding
        val ref = list[i]
        Log.i("pplRec", "onBindViewHolder: size= ${list.size} and index is $i")
        firebaseFirestore!!.collection("profiles").document(ref).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (!task.isSuccessful || task.isCanceled || !task.result.exists()) {
                        try {
                            list.removeAt(i)
                            notifyDataSetChanged()
                        }
                        catch (e: IndexOutOfBoundsException){
                            Log.i("pplRec", "onBindViewHolder exception: size= ${list.size} and index is $i")
                        }
                        return@addOnCompleteListener
                    }
                    val model = task.result.toObject(ProfileShort::class.java)!!
                    with(model){
                        binding.txtUsername.text = a2_username
                        val tips = if (e0a_NOG > 1) "tips" else "tip"
                        binding.txtPost.text = String.format(Locale.getDefault(), "%d  %s  • ", e0a_NOG, tips)
                        binding.txtAccuracy.text = String.format(Locale.getDefault(), "%.1f%%", e0c_WGP.toDouble())
                        binding.btnFollow.text = if (UserNetwork.following == null || !UserNetwork.following.contains(ref)) "FOLLOW"
                        else "FOLLOWING"
                        Glide.with(binding.imgDp.context).load(b2_dpUrl)
                                .placeholder(R.drawable.dummy)
                                .error(getPlaceholderImage(ref[0]))
                                .into(binding.imgDp)
                    }

                    binding.lnrContainer.setOnClickListener { v: View ->
                        if (ref == userId) {
                            binding.lnrContainer.context.startActivity(Intent(binding.root.context, MyProfileActivity::class.java))
                        } else {
                            val intent = Intent(binding.root.context, MemberProfileActivity::class.java)
                            intent.putExtra("userId", ref)
                            binding.lnrContainer.context.startActivity(intent)
                        }
                    }
                    binding.btnFollow.setOnClickListener { v: View ->
                        if (!getNetworkAvailability(binding.root.context)) {
                            Snackbar.make(binding.btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(R.id.bottom_navigation).show()
                            return@setOnClickListener
                        }
                        if (userId == GUEST) {
                            loginPrompt(binding.btnFollow)
                            return@setOnClickListener
                        }
                        if (binding.btnFollow.text.toString().toUpperCase() == "FOLLOW") {
                            val calculations = Calculations(binding.root.context)
                            calculations.followMember(binding.imgDp, userId, ref)
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
        val builder = AlertDialog.Builder(btnFollow.context, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int ->
                    val calculations = Calculations(btnFollow.context)
                    calculations.unfollowMember(btnFollow, userId, userID)
                    btnFollow.text = "FOLLOW"
                }
                .show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PeopleHolder (val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)
}