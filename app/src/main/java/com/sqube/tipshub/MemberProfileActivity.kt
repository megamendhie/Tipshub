package com.sqube.tipshub

import utils.FirebaseUtil.firebaseAuthentication
import utils.Reusable.Companion.applyLinkfy
import utils.Reusable.Companion.getPlaceholderImage
import utils.Reusable.Companion.getNetworkAvailability
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import models.ProfileMedium
import adapters.PerformanceAdapter
import android.app.AlertDialog
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import utils.Calculations
import androidx.recyclerview.widget.LinearLayoutManager
import models.UserNetwork
import com.google.firebase.firestore.DocumentSnapshot
import com.bumptech.glide.Glide
import fragments.PostFragment
import fragments.BankersFragment
import fragments.ReviewFragment
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sqube.tipshub.databinding.ActivityMemberProfileBinding
import java.util.*

class MemberProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var _binding: ActivityMemberProfileBinding
    private val binding get() = _binding
    private var userId: String? = null
    private val USER_ID = "userId"
    private var database: FirebaseFirestore? = null
    private var user: FirebaseUser? = null
    private var profile: ProfileMedium? = null
    var adapter: PerformanceAdapter? = null
    private lateinit var myID: String
    private var imgUrl: String? = null
    var performanceList = ArrayList<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemberProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnFollow.setOnClickListener(this)
        binding.btnSubscribe.setOnClickListener(this)
        binding.lnrFollowers.setOnClickListener(this)
        binding.lnrFollowing.setOnClickListener(this)
        binding.lnrSubscribers.setOnClickListener(this)
        binding.lnrSubscription.setOnClickListener(this)
        binding.tabs.setupWithViewPager(binding.viewpager)
        user = firebaseAuthentication!!.currentUser
        myID = if (user == null) Calculations.GUEST else user!!.uid
        adapter = PerformanceAdapter(performanceList)
        binding.performanceList.layoutManager = LinearLayoutManager(this)
        userId = if (savedInstanceState != null) savedInstanceState.getString(USER_ID) else intent.getStringExtra(USER_ID)
        if (UserNetwork.getFollowing() == null) binding.btnFollow.visibility = View.GONE
        else binding.btnFollow.text = if (UserNetwork.getFollowing().contains(userId)) "FOLLOWING" else "FOLLOW"
        database = FirebaseFirestore.getInstance()
        setupViewPager(binding.viewpager) //set up view pager with fragments
    }

    override fun onResume() {
        super.onResume()
        user = firebaseAuthentication!!.currentUser
        myID = if (user == null) Calculations.GUEST else user!!.uid
    }

    override fun onPostResume() {
        super.onPostResume()
        database!!.collection("profiles").document(userId!!).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                    if (documentSnapshot == null || !documentSnapshot.exists()) return@addOnSuccessListener
                    profile = documentSnapshot.toObject(ProfileMedium::class.java)
                    imgUrl = profile!!.b2_dpUrl
                    if (imgUrl != "") binding.imgDp.setOnClickListener(this@MemberProfileActivity)
                    val name = String.format(Locale.getDefault(), "%s %s", profile!!.a0_firstName, profile!!.a1_lastName)
                    supportActionBar?.title = name
                    binding.txtFullName.text = name
                    binding.txtUsername.text = String.format(Locale.getDefault(), "@%s", profile!!.a2_username)
                    binding.txtBio.text = profile!!.a5_bio
                    binding.txtBio.visibility = if (profile!!.a5_bio.isEmpty()) View.GONE else View.VISIBLE
                    applyLinkfy(this@MemberProfileActivity, profile!!.a5_bio, binding.txtBio)
                    if (profile!!.isD5_allowChat) {
                        binding.txtWhatsapp.text = String.format("Chat with %s", profile!!.a2_username)
                        binding.crdWhatsapp.visibility = View.VISIBLE
                    }
                    binding.txtFollowers.text = profile!!.c4_followers.toString()
                    binding.txtFollowing.text = profile!!.c5_following.toString()
                    binding.txtSubscribers.text = profile!!.c6_subscribers.toString()
                    binding.txtSubscription.text = profile!!.c7_subscriptions.toString()
                    val tips = if (profile!!.e0a_NOG > 1) "tips" else "tip"
                    binding.txtPost.text = String.format(Locale.getDefault(), "%d  %s  • ", profile!!.e0a_NOG, tips)
                    binding.txtWon.text = String.format(Locale.getDefault(), "%d  won  • ", profile!!.e0b_WG)
                    binding.txtAccuracy.text = String.format(Locale.getDefault(), "%.1f%%", profile!!.e0c_WGP.toDouble())
                    if (UserNetwork.getSubscribed() != null && profile!!.isC1_banker
                            && !UserNetwork.getSubscribed().contains(userId)) binding.btnSubscribe.visibility = View.VISIBLE

                    //set Display picture
                    Glide.with(applicationContext).load(profile!!.b2_dpUrl)
                            .placeholder(R.drawable.dummy)
                            .error(getPlaceholderImage(userId!![0]))
                            .into(binding.imgDp)
                    if (!performanceList.isEmpty()) return@addOnSuccessListener
                    if (profile!!.e0a_NOG > 0) {
                        for (i in 1..6) {
                            val row = getRow(i)
                            if (!row.isEmpty()) performanceList.add(row)
                        }
                        binding.performanceList.adapter = adapter
                    }
                }
    }

    private fun getRow(i: Int): Map<String, Any> {
        val row: MutableMap<String, Any> = HashMap()
        when (i) {
            1 -> if (profile!!.e1a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e1a_NOG
                row["WG"] = profile!!.e1b_WG
                row["WGP"] = profile!!.e1c_WGP
            }
            2 -> if (profile!!.e2a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e2a_NOG
                row["WG"] = profile!!.e2b_WG
                row["WGP"] = profile!!.e2c_WGP
            }
            3 -> if (profile!!.e3a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e3a_NOG
                row["WG"] = profile!!.e3b_WG
                row["WGP"] = profile!!.e3c_WGP
            }
            4 -> if (profile!!.e4a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e4a_NOG
                row["WG"] = profile!!.e4b_WG
                row["WGP"] = profile!!.e4c_WGP
            }
            5 -> if (profile!!.e5a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e5a_NOG
                row["WG"] = profile!!.e5b_WG
                row["WGP"] = profile!!.e5c_WGP
            }
            6 -> if (profile!!.e6a_NOG > 0) {
                row["type"] = i
                row["NOG"] = profile!!.e6a_NOG
                row["WG"] = profile!!.e6b_WG
                row["WGP"] = profile!!.e6c_WGP
            }
        }
        return row
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val bundle = Bundle()
        bundle.putString(USER_ID, userId)
        val postFragment = PostFragment()
        val bankerFragment = BankersFragment()
        val reviewFragment = ReviewFragment()
        //passing bunder with userId to fragment
        postFragment.arguments = bundle
        bankerFragment.arguments = bundle
        reviewFragment.arguments = bundle
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(postFragment, "Posts")
        adapter.addFragment(bankerFragment, "Bankers")
        adapter.addFragment(reviewFragment, "Review")
        viewPager.adapter = adapter
    }

    private fun showDp() {
        val builder = AlertDialog.Builder(this@MemberProfileActivity)
        val dialogView = layoutInflater.inflate(R.layout.image_viewer, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val imgProfile = dialog.findViewById<ImageView>(R.id.imgDp)
        //set Display picture
        Glide.with(applicationContext).load(profile!!.b2_dpUrl)
                .placeholder(R.drawable.dummy)
                .error(getPlaceholderImage(userId!![0]))
                .into(imgProfile)
    }

    override fun onClick(v: View) {
        val intent = Intent(this, FollowerListActivity::class.java)
        intent.putExtra("personId", userId)
        when (v) {
            binding.imgDp -> showDp()
            binding.lnrFollowers -> {
                if (Integer.valueOf(binding.txtFollowers.text.toString()) < 1) return
                intent.putExtra("search_type", "followers")
                startActivity(intent)
            }
            binding.lnrFollowing -> {
                if (Integer.valueOf(binding.txtFollowing.text.toString()) < 1) return
                intent.putExtra("search_type", "followings")
                startActivity(intent)
            }
            binding.lnrSubscribers -> {
                if (Integer.valueOf(binding.txtSubscribers.text.toString()) < 1) return
                intent.putExtra("search_type", "subscribers")
                startActivity(intent)
            }
            binding.lnrSubscription -> {
                if (Integer.valueOf(binding.txtSubscription.text.toString()) < 1) return
                intent.putExtra("search_type", "subscribed_to")
                startActivity(intent)
            }
            binding.btnFollow -> {
                if (userId == Calculations.GUEST) {
                    loginPrompt()
                    return
                }
                if (!getNetworkAvailability(this)) {
                    Snackbar.make(binding.btnFollow, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                    return
                }
                if (binding.btnFollow.text == "FOLLOW") {
                    val calculations = Calculations(this@MemberProfileActivity)
                    calculations.followMember(binding.btnFollow, myID, userId, false)
                    profile!!.c4_followers = profile!!.c4_followers + 1
                    binding.txtFollowers.text = profile!!.c4_followers.toString()
                    binding.btnFollow.text = "FOLLOWING"
                } else unfollowPrompt()
            }
            binding.btnSubscribe -> {
                if (userId == Calculations.GUEST) {
                    loginPrompt()
                    return
                }
                val intentSub = Intent(applicationContext, SubscriptionActivity::class.java)
                intentSub.putExtra(USER_ID, userId)
                startActivity(intentSub)
            }
        }
    }

    fun startChat(view: View?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@MemberProfileActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to chat with %s?", profile!!.a2_username))
                .setTitle("Start chat")
                .setNegativeButton("No") { _: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { _: DialogInterface?, i: Int ->
                    val pkMgt = packageManager
                    val toNumber = profile!!.b1_phone
                    val uri = Uri.parse("http://api.whatsapp.com/send?phone=$toNumber&text=")
                    try {
                        val whatsApp = Intent(Intent.ACTION_VIEW)
                        whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        whatsApp.data = uri
                        startActivity(whatsApp)
                    } catch (e: PackageManager.NameNotFoundException) {
                        Toast.makeText(this@MemberProfileActivity, "No WhatApp installed", Toast.LENGTH_LONG).show()
                    }
                }
                .show()
    }

    private fun loginPrompt() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@MemberProfileActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("You have to login first")
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Login") { dialogInterface: DialogInterface?, i: Int ->
                    startActivity(Intent(this@MemberProfileActivity, LoginActivity::class.java))
                    finish()
                }
                .show()
    }

    private fun unfollowPrompt() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@MemberProfileActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage(String.format("Do you want to unfollow %s?", profile!!.a2_username))
                .setTitle("Unfollow")
                .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int -> }
                .setPositiveButton("Yes") { dialogInterface: DialogInterface?, i: Int ->
                    val calculations = Calculations(this@MemberProfileActivity)
                    calculations.unfollowMember(binding.btnFollow, myID, userId, false)
                    profile!!.c4_followers = Math.max(0, profile!!.c4_followers - 1)
                    binding.txtFollowers.text = profile!!.c4_followers.toString()
                    binding.btnFollow.text = "FOLLOW"
                }
                .show()
    }

    inner class ViewPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(USER_ID, userId)
        super.onSaveInstanceState(outState)
    }
}