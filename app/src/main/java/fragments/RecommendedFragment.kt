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
import com.sqube.tipshub.R
import models.UserNetwork
import org.json.JSONException
import org.json.JSONObject
import utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RecommendedFragment : Fragment(), View.OnClickListener {
    private var userId: String? = null
    private var peopleList: RecyclerView? = null
    private var newsList: RecyclerView? = null
    private var alreadyLoaded = false
    private var dbHelper: DatabaseHelper? = null
    private var db: SQLiteDatabase? = null
    private val dataList = ArrayList<HashMap<String, String>>()
    private var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(context)
        db = dbHelper!!.readableDatabase
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment only if its null
        if (rootView == null) rootView = inflater.inflate(R.layout.fragment_recommended, container, false)
        peopleList = rootView!!.findViewById(R.id.peopleList)
        peopleList?.setLayoutManager(LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false))
        newsList = rootView!!.findViewById(R.id.newsList)
        newsList?.setLayoutManager(LinearLayoutManager(activity))
        val btnInvite = rootView!!.findViewById<Button>(R.id.btnInvite)
        btnInvite.setOnClickListener(this)
        val btnInviteWhatsapp = rootView!!.findViewById<Button>(R.id.btnInviteWhatsapp)
        btnInviteWhatsapp.setOnClickListener(this)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        userId = user!!.uid
        alreadyLoaded = false
        loadNews()
        //execute();
        return rootView
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
                        val list = ArrayList<String?>()
                        for (snapshot in task.result!!.documents) {
                            list.add(snapshot.id)
                        }
                        Collections.shuffle(list)
                        peopleList!!.adapter = PeopleRecAdapter(context, userId, list)
                    } else loadPeopleFromProfile()
                })
    }

    private fun loadPeopleFromProfile() {
        FirebaseUtil.firebaseFirestore?.collection("profiles")?.orderBy("c2_score",
                Query.Direction.DESCENDING)!!.limit(30).get().addOnCompleteListener { task ->
            if (task.result != null && !task.result!!.isEmpty) {
                val list = ArrayList<String?>()
                for (snapshot in task.result!!.documents) {
                    val ref = snapshot.id
                    if (ref == userId) continue
                    if (UserNetwork.getFollowing() != null && UserNetwork.getFollowing().contains(ref)) continue
                    if (list.size >= 12) break
                    list.add(ref)
                }
                Collections.shuffle(list)
                peopleList!!.adapter = PeopleRecAdapter(context, userId, list)
            }
        }
    }

    private fun loadNews() {
        val newsTask = DownloadNews()
        if (!Reusable.getNetworkAvailability(requireContext().applicationContext)) Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()
        newsTask.execute()
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
        when (view.id) {
            R.id.btnInvite -> startActivity(Intent.createChooser(invitationIntent, "Invite via:"))
            R.id.btnInviteWhatsapp -> {
                invitationIntent.setPackage("com.whatsapp")
                try {
                    startActivity(invitationIntent)
                } catch (e: Throwable) {
                    Toast.makeText(context, "No whatsapp installed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private inner class DownloadNews : AsyncTask<String, Void, String>() {
        var myAPI_Key = "417444c0502047d69c1c2a9dcc1672cd"
        var KEY_AUTHOR = "author"
        var KEY_TITLE = "title"
        var KEY_DESCRIPTION = "description"
        var KEY_URL = "url"
        var KEY_URLTOIMAGE = "urlToImage"
        var KEY_PUBLISHEDAT = "publishedAt"
        var BASE_URL = "https://newsapi.org/v2/everything?"
        var DOMAIN_NAME = "domains"
        var LANGUAGE = "language"
        var PAGE_SIZE = "pageSize"
        var API_KEY = "apiKey"
        override fun onPreExecute() {
            super.onPreExecute()
            val xml = dbHelper!!.getTip(db, Calculations.NEWS)
            //if (xml != null && !xml.isEmpty())
            //    onPostExecute(xml);
        }

        protected override fun doInBackground(vararg args: String): String? {
            val httpConnection = HttpConFunction()
            val builtURI = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(DOMAIN_NAME, "goal.com")
                    .appendQueryParameter(LANGUAGE, "en")
                    .appendQueryParameter(PAGE_SIZE, "29")
                    .appendQueryParameter(API_KEY, myAPI_Key)
                    .build()
            return httpConnection.executeGet(builtURI.toString(), "RECOMMEND")
        }

        override fun onPostExecute(xml: String?) {
            if (xml == null || xml.isEmpty()) {
                return
            }
            if (xml.length > 10) { // Just checking if not empty
                dataList.clear()
                try {
                    val jsonResponse = JSONObject(xml)
                    val jsonArray = jsonResponse.optJSONArray("articles")
                    val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
                    oldFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val newFormatter = SimpleDateFormat("dd MMM", Locale.ENGLISH)
                    newFormatter.timeZone = TimeZone.getDefault()
                    var dt: Date?
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val map = HashMap<String, String>()
                        map[KEY_AUTHOR] = jsonObject.optString(KEY_AUTHOR)
                        map[KEY_TITLE] = jsonObject.optString(KEY_TITLE)
                        map[KEY_DESCRIPTION] = jsonObject.optString(KEY_DESCRIPTION)
                        map[KEY_URL] = jsonObject.optString(KEY_URL).toString()
                        map[KEY_URLTOIMAGE] = jsonObject.optString(KEY_URLTOIMAGE)
                        dt = oldFormat.parse(jsonObject.optString(KEY_PUBLISHEDAT))
                        val newsDate = newFormatter.format(dt)
                        map[KEY_PUBLISHEDAT] = newsDate
                        dataList.add(map)
                    }

                    //Save news to database
                    dbHelper!!.updateTip(db, Calculations.NEWS, xml)
                } catch (e: JSONException) {
                    val TAG = "RecFragment"
                    Log.i(TAG, "onPostExecute: " + e.message)
                    Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show()
                } catch (e: ParseException) {
                    val TAG = "RecFragment"
                    Log.i(TAG, "onPostExecute: " + e.message)
                    Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show()
                }
                val adapter = NewsAdapter(activity, dataList)
                newsList!!.layoutManager = LinearLayoutManager(context)
                newsList!!.adapter = adapter
            } else {
                Toast.makeText(context, "No news found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}