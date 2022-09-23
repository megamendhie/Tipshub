package com.sqube.tipshub.activities

import com.sqube.tipshub.utils.FirebaseUtil.firebaseAuthentication
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.Reusable.Companion.applyLinkfy
import androidx.appcompat.app.AppCompatActivity
import com.sqube.tipshub.models.ProfileMedium
import android.widget.TextView
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import com.sqube.tipshub.fragments.PostFragment
import com.sqube.tipshub.fragments.BankersFragment
import com.sqube.tipshub.fragments.ReviewFragment
import androidx.fragment.app.FragmentPagerAdapter
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import adapters.PerformanceAdapter
import android.app.AlertDialog
import android.graphics.Color
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivityMyProfileBinding
import java.util.*

class MyProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var _binding: ActivityMyProfileBinding
    private val binding get() = _binding
    private var userId: String? = null
    private var imgUrl: String? = null
    var profile: ProfileMedium? = null
    var performanceList = ArrayList<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tabs.setupWithViewPager(binding.viewpager)
        binding.lnrFollowing.setOnClickListener(this)
        binding.lnrFollowers.setOnClickListener(this)
        binding.lnrSubscribers.setOnClickListener(this)
        binding.lnrSubscription.setOnClickListener(this)
        val btnEdit = findViewById<TextView>(R.id.btnEdit)
        btnEdit.setOnClickListener { v: View? -> startActivity(Intent(this@MyProfileActivity, SettingsActivity::class.java)) }
        binding.performanceList.layoutManager = LinearLayoutManager(this)
        val user = firebaseAuthentication!!.currentUser
        userId = user!!.uid
        setupViewPager(binding.viewpager) //set up view pager with com.sqube.tipshub.fragments
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
        bundle.putString("userId", userId)
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

    private fun showDp() {
        val builder = AlertDialog.Builder(this@MyProfileActivity)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.image_viewer, null)
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

    override fun onPostResume() {
        super.onPostResume()
        firebaseFirestore!!.collection("profiles").document(userId!!).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                    if (documentSnapshot == null || !documentSnapshot.exists()) return@addOnSuccessListener
                    profile = documentSnapshot.toObject(ProfileMedium::class.java)
                    imgUrl = profile!!.b2_dpUrl
                    if (imgUrl!="") binding.imgDp.setOnClickListener(this@MyProfileActivity)
                    val name = String.format(Locale.getDefault(), "%s %s", profile!!.a0_firstName, profile!!.a1_lastName)
                    supportActionBar?.title = name
                    binding.txtFullName.text = name
                    binding.txtUsername.text = String.format(Locale.getDefault(), "@%s", profile!!.a2_username)
                    binding.txtBio.text = profile!!.a5_bio
                    applyLinkfy(this@MyProfileActivity, profile!!.a5_bio, binding.txtBio)
                    binding.txtFollowers.text = profile!!.c4_followers.toString()
                    binding.txtFollowing.text = profile!!.c5_following.toString()
                    binding.txtSubscribers.text = profile!!.c6_subscribers.toString()
                    binding.txtSubscription.text = profile!!.c7_subscriptions.toString()
                    val tips = if (profile!!.e0a_NOG > 1) "tips" else "tip"
                    binding.txtPost.text = String.format(Locale.getDefault(), "%d  %s  • ", profile!!.e0a_NOG, tips)
                    binding.txtWon.text = String.format(Locale.getDefault(), "%d  won  • ", profile!!.e0b_WG)
                    binding.txtAccuracy.text = String.format(Locale.getDefault(), "%.1f%%", profile!!.e0c_WGP.toDouble())

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
                        binding.performanceList.adapter = PerformanceAdapter(performanceList)
                    }
                }
    }

    override fun onClick(v: View) {
        val intent = Intent(this, FollowerListActivity::class.java)
        intent.putExtra("personId", userId)
        when (v.id) {
            R.id.imgDp -> showDp()
            R.id.lnrFollowers -> {
                if (Integer.valueOf(binding.txtFollowers.text.toString()) < 1) return
                intent.putExtra("search_type", "followers")
                startActivity(intent)
            }
            R.id.lnrFollowing -> {
                if (Integer.valueOf(binding.txtFollowing.text.toString()) < 1) return
                intent.putExtra("search_type", "followings")
                startActivity(intent)
            }
            R.id.lnrSubscribers -> {
                if (Integer.valueOf(binding.txtSubscribers.text.toString()) < 1) return
                intent.putExtra("search_type", "subscribers")
                startActivity(intent)
            }
            R.id.lnrSubscription -> {
                if (Integer.valueOf(binding.txtSubscription.text.toString()) < 1) return
                intent.putExtra("search_type", "subscribed_to")
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

}