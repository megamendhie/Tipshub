package com.sqube.tipshub.fragments

import com.sqube.tipshub.adapters.NewsAdapter
import com.sqube.tipshub.adapters.PeopleRecAdapter
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.sqube.tipshub.databinding.FragmentRecommendedBinding
import com.sqube.tipshub.models.News
import com.sqube.tipshub.models.UserNetwork
import com.sqube.tipshub.network.NewsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.sqube.tipshub.utils.*
import java.util.*

class RecommendedFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentRecommendedBinding? = null
    private val binding get() = _binding!!
    private val _tag = "RecFragment"
    private var userId: String? = null
    private var alreadyLoaded = false
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(context)
        db = dbHelper!!.readableDatabase
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentRecommendedBinding.inflate(inflater, container, false)
        binding.peopleList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.btnInvite.setOnClickListener(this)
        binding.btnInviteWhatsapp.setOnClickListener(this)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        userId = user!!.uid
        alreadyLoaded = false
        loadNews()
        return binding.root
    }
    fun scrollToTop(){
        binding.nestRec.smoothScrollTo(0,0)
    }

    override fun onStart() {
        super.onStart()
        if (alreadyLoaded) return
        loadPeople()
        alreadyLoaded = true
    }

    private fun loadPeople() {
        val recReference = FirebaseUtil.firebaseFirestore?.collection("recommended")?.document(userId!!)?.collection("rec")
        recReference!!.orderBy("count", Query.Direction.DESCENDING).limit(10).get()
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.result == null || task.result!!.isEmpty) {
                        loadPeopleFromProfile()
                        return@OnCompleteListener
                    }
                    if (task.result!!.documents.size > 6) {
                        val list = ArrayList<String>()
                        for (snapshot in task.result!!.documents) {
                            list.add(snapshot.id)
                        }
                        Collections.shuffle(list)
                        binding.peopleList.adapter = PeopleRecAdapter(userId!!, list)
                    } else loadPeopleFromProfile()
                })
    }

    private fun loadPeopleFromProfile() {
        FirebaseUtil.firebaseFirestore?.collection("profiles")?.orderBy("c2_score",
                Query.Direction.DESCENDING)!!.limit(30).get().addOnCompleteListener { task ->
            if (task.result != null && !task.result!!.isEmpty) {
                val list = ArrayList<String>()
                for (snapshot in task.result!!.documents) {
                    val ref = snapshot.id
                    if (ref == userId) continue
                    if (UserNetwork.following != null && UserNetwork.following.contains(ref)) continue
                    if (list.size >= 12) break
                    list.add(ref)
                }
                Collections.shuffle(list)
                binding.peopleList.adapter = PeopleRecAdapter(userId!!, list)
            }
        }
    }

    private fun loadNews() {
        binding.newsList.layoutManager = LinearLayoutManager(context)
        val newsJson = dbHelper?.getTip(db, NEWS)
        val news = gson.fromJson(newsJson, News::class.java)
        if(news?.status?.equals("ok") == true)binding.newsList.adapter = NewsAdapter(news.articles!!)
        NewsApi.retrofitService.getNews().enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                val newsBody: News? = response.body()
                Log.i(_tag, "onResponse: ${response.body()}")
                if(newsBody?.status?.equals("ok") == true){
                    binding.newsList.adapter = NewsAdapter(newsBody.articles!!)
                    //Save news to database
                    val xml = gson.toJson(newsBody, News::class.java)
                    dbHelper!!.updateTip(db, NEWS, xml)
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.i(_tag, "onFailure: ${t.message}")
            }
        })

    }

    override fun onClick(view: View) {
        val invite = """
            Tipshub is a very fantastic sports social com.sqube.tipshub.network. Join now to connect with fans and also get sure predictions from good tipsters.
            
            App here: http://bit.ly/tipshub
            """.trimIndent()
        val invitationIntent = Intent(Intent.ACTION_SEND)
        invitationIntent.type = "text/plain"
        invitationIntent.putExtra(Intent.EXTRA_TEXT, invite)
        invitationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        when (view) {
            binding.btnInvite -> startActivity(Intent.createChooser(invitationIntent, "Invite via:"))
            binding.btnInviteWhatsapp -> {
                invitationIntent.setPackage("com.whatsapp")
                try {
                    startActivity(invitationIntent)
                } catch (e: Throwable) {
                    Toast.makeText(context, "No whatsapp installed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}