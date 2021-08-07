package fragments

import adapters.PostAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import models.Post
import utils.Calculations
import utils.FirebaseUtil

class PostFragment : Fragment() {
    private var userId: String? = null
    private var myId: String? = null
    private var recyclerView: RecyclerView? = null
    private var user: FirebaseUser? = null
    private var postAdapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_post, container, false)
        recyclerView = rootView.findViewById(R.id.postList)
        (recyclerView?.getItemAnimator() as DefaultItemAnimator?)!!.supportsChangeAnimations = false
        recyclerView?.setLayoutManager(LinearLayoutManager(activity))
        user = FirebaseUtil.getFirebaseAuthentication().currentUser
        myId = if (user == null) Calculations.GUEST else user!!.uid
        userId = arguments?.getString("userId")
        loadPost()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        user = FirebaseUtil.getFirebaseAuthentication().currentUser
        myId = if (user == null) Calculations.GUEST else user!!.uid
        if (postAdapter != null) postAdapter!!.setUserId(myId)
    }

    private fun loadPost() {
        val TAG = "PostFragment"
        Log.i(TAG, "loadPost: ")
        val query = FirebaseUtil.getFirebaseFirestore().collection("posts").orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", userId)
        val response = FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()
        postAdapter = PostAdapter(response, myId, context, false)
        recyclerView!!.adapter = postAdapter
        if (postAdapter != null) {
            Log.i(TAG, "loadPost: started listening")
            postAdapter!!.startListening()
        }
    }
}