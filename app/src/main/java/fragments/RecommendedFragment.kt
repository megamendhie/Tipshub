package fragments

import adapters.NewsAdapter
import adapters.PeopleRecAdapter
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.FragmentRecommendedBinding
import models.News
import models.UserNetwork
import network.NewsApi
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
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
                    if (UserNetwork.getFollowing() != null && UserNetwork.getFollowing().contains(ref)) continue
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
        val newsJson = dbHelper?.getTip(db, Calculations.NEWS)
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
                    dbHelper!!.updateTip(db, Calculations.NEWS, xml)
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.i(_tag, "onFailure: ${t.message}")
            }
        })

    }

    override fun onClick(view: View) {
        val invite = """
            Tipshub is a very fantastic sports social network. Join now to connect with fans and also get sure predictions from good tipsters.
            
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