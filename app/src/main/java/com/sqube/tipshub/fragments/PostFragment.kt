package com.sqube.tipshub.fragments

import adapters.PostAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Query
import com.sqube.tipshub.databinding.FragmentPostBinding
import com.sqube.tipshub.models.Post
import com.sqube.tipshub.utils.FirebaseUtil
import com.sqube.tipshub.utils.GUEST

class PostFragment : Fragment() {
    private var userId: String? = null
    private lateinit var myId: String
    private var user: FirebaseUser? = null
    private var postAdapter: PostAdapter? = null
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        (binding.postList.itemAnimator as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        binding.postList.layoutManager = LinearLayoutManager(activity)
        user = FirebaseUtil.firebaseAuthentication?.currentUser
        myId = user?.uid ?: GUEST
        userId = arguments?.getString("userId")
        loadPost()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        user = FirebaseUtil.firebaseAuthentication?.currentUser
        myId = if (user == null) GUEST else user!!.uid
        if (postAdapter != null) postAdapter!!.setUserId(myId)
    }

    private fun loadPost() {
        val TAG = "PostFragment"
        Log.i(TAG, "loadPost: ")
        val query = FirebaseUtil.firebaseFirestore?.collection("posts")!!.orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", userId)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, myId, requireContext())
        binding.postList.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(TAG, "loadPost: started listening")
            postAdapter!!.startListening()
        }
    }
}