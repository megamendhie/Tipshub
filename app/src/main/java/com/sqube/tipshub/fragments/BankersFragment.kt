package com.sqube.tipshub.fragments

import com.sqube.tipshub.adapters.BankerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import com.sqube.tipshub.utils.FirebaseUtil
import com.sqube.tipshub.utils.GUEST

/*
    This fragment is attached to Profile activity
 */   class BankersFragment : Fragment() {
    private var userId: String? = null
    private lateinit var myId: String
    private var postAdapter: BankerAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var user: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_bankers, container, false)
        recyclerView = rootView.findViewById(R.id.postList)
        recyclerView?.setLayoutManager(LinearLayoutManager(activity))
        user = FirebaseUtil.firebaseAuthentication?.currentUser
        myId = user?.uid ?: GUEST
        userId = requireArguments().getString("userId")
        loadPost()
        return rootView
    }

    private fun loadPost() {
        val TAG = "PostFragment"
        Log.i(TAG, "loadPost: ")
        val query = FirebaseUtil.firebaseFirestore?.collection("posts")?.orderBy("time", Query.Direction.DESCENDING)!!
                .whereEqualTo("userId", userId).whereEqualTo("type", 6)
        postAdapter = BankerAdapter(query, myId, requireContext(), false)
        recyclerView!!.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(TAG, "loadPost: started listening")
            postAdapter!!.startListening()
        }
    }

    override fun onResume() {
        super.onResume()
        user = FirebaseUtil.firebaseAuthentication?.currentUser
        myId = user?.uid ?: GUEST
        if (postAdapter != null) postAdapter!!.setUserId(myId)
    }
}